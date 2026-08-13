package com.bhola.desiKahaniya;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Plays narration in a foreground service so it keeps going when the user leaves
 * AudioPlayer - switches app, presses Home, or turns the screen off. The activity
 * is only a remote control: it sends actions in and renders the broadcasts that
 * come back out, so the two can go out of sync only for as long as one tick.
 *
 * Requires FOREGROUND_SERVICE_MEDIA_PLAYBACK, which also needs a matching
 * foreground-service-permissions declaration in the Play Console.
 */
public class AudioPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler();
    private String audioUrl, title, audioHref, storyName, AudioDownloadState;
    public static final String CHANNEL_ID = "audio_channel";

    /** Broadcast sent once the track reaches its end, so the UI can reset. */
    public static final String ACTION_COMPLETED = "AUDIO_COMPLETED";

    private MediaSessionCompat mediaSession;
    public static String CURRENT_AUDIO_URL = null;
    public static boolean isServiceRunning = false;

    /** True between prepareAsync() and onPrepared - getDuration() is invalid until then. */
    private boolean prepared = false;
    private boolean progressTicking = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY hands us a null intent when the system restarts the service.
        // There is nothing to resume at that point, so shut down rather than sit in
        // the shade as a foreground service playing silence.
        if (intent == null) {
            stopPlaybackAndSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (mediaSession == null) {
            mediaSession = new MediaSessionCompat(this, "AudioServiceSession");
            mediaSession.setActive(true);
            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    play();
                }

                @Override
                public void onPause() {
                    pause();
                }

                @Override
                public void onSeekTo(long pos) {
                    if (mediaPlayer != null && prepared) mediaPlayer.seekTo((int) pos);
                }
            });
        }

        String requestedUrl = intent.getStringExtra("storyURL");
        if (requestedUrl != null) {
            boolean isNewTrack = mediaPlayer == null || !requestedUrl.equals(CURRENT_AUDIO_URL);

            audioUrl = requestedUrl;
            storyName = intent.getStringExtra("storyName");
            title = intent.getStringExtra("title");
            audioHref = intent.getStringExtra("audioHref");
            AudioDownloadState = intent.getStringExtra("AudioDownloadState");

            // Foreground status has to be claimed within a few seconds of
            // startForegroundService(), and a stream can take longer than that to
            // prepare - so post the notification now, before preparing, and refresh
            // it once playback actually starts.
            CURRENT_AUDIO_URL = audioUrl;
            showNotification(false);

            if (isNewTrack) {
                startTrack(audioUrl);
                return START_STICKY;
            }
        } else {
            // An action-only intent (from the notification) still has to satisfy the
            // startForeground deadline if the system recreated us.
            showNotification(mediaPlayer != null && mediaPlayer.isPlaying());
        }

        if (action != null) {
            switch (action) {
                case "PLAY":
                    play();
                    break;
                case "PAUSE":
                    pause();
                    break;
                case "SYNC":
                    sendPlayPauseState();
                    break;
                case "TOGGLE":
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) pause();
                    else play();
                    break;
                case "STOP":
                    stopPlaybackAndSelf();
                    return START_NOT_STICKY;
                case "SEEK":
                    int pos = intent.getIntExtra("seekTo", -1);
                    if (pos >= 0 && mediaPlayer != null && prepared) mediaPlayer.seekTo(pos);
                    break;
            }
        }

        return START_STICKY;
    }

    private void startTrack(String url) {
        prepared = false;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            mediaPlayer.setDataSource(url);

            mediaPlayer.setOnPreparedListener(mp -> {
                prepared = true;
                mp.start();
                updateMediaSession();
                showNotification(true);
                sendPlayPauseState();
                startProgressTicks();
            });

            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                Intent bufferIntent = new Intent("BUFFER_UPDATE");
                bufferIntent.setPackage(getPackageName());
                bufferIntent.putExtra("percent", percent);
                sendBroadcast(bufferIntent);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                updateMediaSession();
                showNotification(false);

                // Nothing is playing any more, so give up foreground status - a
                // finished story must not leave a permanent service in the shade.
                // DETACH keeps the notification around so the user can still hit
                // play, which puts us back in the foreground.
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);

                Intent done = new Intent(ACTION_COMPLETED);
                done.setPackage(getPackageName());
                sendBroadcast(done);
            });

            // Fallback: if the primary URL fails, retry with the URL built from audioHref.
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(SplashScreen.TAG, "audio failed (" + what + "/" + extra + "), retrying via href");
                try {
                    prepared = false;
                    mp.reset();
                    mp.setDataSource(SplashScreen.databaseURL + "Sexstory_Audiofiles/" + audioHref + ".mp3");
                    mp.setOnPreparedListener(mp2 -> {
                        prepared = true;
                        mp2.start();
                        updateMediaSession();
                        showNotification(true);
                        sendPlayPauseState();
                        startProgressTicks();
                    });
                    mp.prepareAsync();
                } catch (Exception e) {
                    Log.e(SplashScreen.TAG, "audio fallback failed", e);
                }
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(SplashScreen.TAG, "could not start audio", e);
        }
    }

    private void play() {
        if (mediaPlayer == null || !prepared || mediaPlayer.isPlaying()) return;
        mediaPlayer.start();
        updateMediaSession();
        showNotification(true);
        sendPlayPauseState();
        startProgressTicks();
    }

    private void pause() {
        if (mediaPlayer == null || !prepared || !mediaPlayer.isPlaying()) return;
        mediaPlayer.pause();
        updateMediaSession();
        showNotification(false);

        // Nothing is being played while paused, so stop being a foreground
        // service - otherwise a story paused and forgotten holds one open
        // indefinitely. DETACH leaves the notification (now dismissible) in
        // place, and hitting play puts us back in the foreground.
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);

        sendPlayPauseState();
    }

    private void stopPlaybackAndSelf() {
        handler.removeCallbacksAndMessages(null);
        progressTicking = false;
        CURRENT_AUDIO_URL = null;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
                // stop() on a player that never prepared - nothing to stop.
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void sendPlayPauseState() {
        // Scoped to our own package: the receivers are NOT_EXPORTED, and an
        // implicit broadcast would otherwise be readable by any installed app.
        if (mediaPlayer == null || !prepared) return;

        Intent intent = new Intent("PAUSE_PLAY_BTN_UPDATE");
        intent.setPackage(getPackageName());
        intent.putExtra("PAUSE_PLAY_BTN_UPDATE", mediaPlayer.isPlaying() ? "PLAY" : "PAUSE");
        intent.putExtra("current", mediaPlayer.getCurrentPosition());
        intent.putExtra("duration", mediaPlayer.getDuration());
        sendBroadcast(intent);
    }

    private void updateMediaSession() {
        if (mediaPlayer == null || !prepared) return;

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, storyName != null ? storyName : title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration())
                .build());

        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SEEK_TO |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
                .setState(
                        mediaPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        mediaPlayer.getCurrentPosition(),
                        1.0f
                )
                .build();

        mediaSession.setPlaybackState(playbackState);
    }

    private void showNotification(boolean isPlaying) {

        Intent notifIntent = new Intent(this, SplashScreen.class);
        notifIntent.putExtra("storyURL", audioUrl);
        notifIntent.putExtra("storyName", storyName);
        notifIntent.putExtra("title", title);
        notifIntent.putExtra("audioHref", audioHref);
        notifIntent.putExtra("AudioDownloadState", AudioDownloadState);
        notifIntent.putExtra("ComingFromAudioPlayer", "ComingFromAudioPlayer");

        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent deleteIntent = new Intent(this, NotificationDismissedReceiver.class);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                this, 0, deleteIntent, PendingIntent.FLAG_IMMUTABLE);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent playPauseIntent = new Intent(this, AudioPlayerService.class);
        playPauseIntent.setAction("TOGGLE");

        PendingIntent actionIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            actionIntent = PendingIntent.getForegroundService(
                    this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            actionIntent = PendingIntent.getService(
                    this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE
            );
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(storyName != null ? storyName : "Now Playing")
                .setContentText(getString(R.string.app_name))
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .addAction(isPlaying ? R.drawable.pause : R.drawable.play,
                        isPlaying ? "Pause" : "Play", actionIntent)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0))
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying)
                .build();

        // Typed startForeground is what Android 14+ checks the
        // FOREGROUND_SERVICE_MEDIA_PLAYBACK permission against.
        ServiceCompat.startForeground(this, 1, notification,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        : 0);
    }

    /** One repeating tick drives both the seekbar broadcast and the media session. */
    private void startProgressTicks() {
        if (progressTicking) return;
        progressTicking = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && prepared) {
                    Intent updateIntent = new Intent("PROGRESS_UPDATE");
                    updateIntent.setPackage(getPackageName());
                    updateIntent.putExtra("current", mediaPlayer.getCurrentPosition());
                    updateIntent.putExtra("duration", mediaPlayer.getDuration());
                    updateIntent.putExtra("title", title);
                    updateIntent.putExtra("isPlaying", mediaPlayer.isPlaying());
                    sendBroadcast(updateIntent);

                    updateMediaSession();
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        progressTicking = false;

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }

        CURRENT_AUDIO_URL = null;
        isServiceRunning = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
    }
}
