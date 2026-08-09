package com.bhola.desiKahaniya;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Locale;

public class AudioPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private String audioUrl, title, audioHref, storyName, AudioDownloadState;
    public static final String CHANNEL_ID = "audio_channel";

    private MediaSessionCompat mediaSession;
    public static String CURRENT_AUDIO_URL = null;
    public static boolean isServiceRunning = false;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        // Initialize audio metadata if available
        if (intent.getStringExtra("storyURL") != null) {
            audioUrl = intent.getStringExtra("storyURL");
            storyName = intent.getStringExtra("storyName");
            title = intent.getStringExtra("title");
            audioHref = intent.getStringExtra("audioHref");
            AudioDownloadState = intent.getStringExtra("AudioDownloadState");
            CURRENT_AUDIO_URL = audioUrl;


        }


        // Initialize MediaSession if null
        if (mediaSession == null) {
            mediaSession = new MediaSessionCompat(this, "AudioServiceSession");
            mediaSession.setActive(true);
            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        updateMediaSession();
                        showNotification(true);
                        sendPlayPauseState();
                    }
                }

                @Override
                public void onPause() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        updateMediaSession();
                        showNotification(false);
                        sendPlayPauseState();


                    }
                }

                @Override
                public void onSeekTo(long pos) {
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo((int) pos);
                    }
                }
            });
        }

        // Initialize and start media player
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioUrl);
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    updateMediaSession();
                    showNotification(true);
                    sendProgress();
                });

                mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                    Intent bufferIntent = new Intent("BUFFER_UPDATE");
                    bufferIntent.setPackage(getPackageName());
                    bufferIntent.putExtra("percent", percent);
                    sendBroadcast(bufferIntent);
                });

                // Fallback: if the primary URL fails, retry with the URL built from audioHref.
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    try {
                        mp.reset();
                        mp.setDataSource(SplashScreen.databaseURL + "Sexstory_Audiofiles/" + audioHref + ".mp3");
                        mp.setOnPreparedListener(mp2 -> {
                            mp2.start();
                            updateMediaSession();
                            showNotification(true);
                            sendProgress();
                        });
                        mp.prepareAsync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (action != null) {
            switch (action) {
                case "PLAY":
                    mediaPlayer.start();
                    updateMediaSession();
                    showNotification(true);
                    break;

                case "PAUSE":
                    mediaPlayer.pause();
                    updateMediaSession();
                    showNotification(false);
                    break;

                case "SYNC":
                    sendPlayPauseState();
                    break;

                case "TOGGLE":
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        showNotification(false);
                    } else {
                        mediaPlayer.start();
                        showNotification(true);
                    }
                    updateMediaSession();
                    break;

                case "SEEK":
                    int pos = intent.getIntExtra("seekTo", -1);
                    if (pos >= 0) mediaPlayer.seekTo(pos);
                    break;
            }
        }

        return START_STICKY;
    }

    private void sendPlayPauseState() {

        //this is broadcasted when the play pause btn is clicked in notification media control

        // Scoped to our own package: the receivers are NOT_EXPORTED, and an
        // implicit broadcast would otherwise be readable by any installed app.
        Intent intent = new Intent("PAUSE_PLAY_BTN_UPDATE");
        intent.setPackage(getPackageName());
        intent.putExtra("PAUSE_PLAY_BTN_UPDATE", mediaPlayer != null && mediaPlayer.isPlaying() ? "PLAY" : "PAUSE");
        intent.putExtra("current", mediaPlayer.getCurrentPosition());
        intent.putExtra("duration", mediaPlayer.getDuration());


        sendBroadcast(intent);
    }

    private void updateMediaSession() {
        if (mediaPlayer == null) return;

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
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

    // This service is currently unregistered in AndroidManifest.xml (background
    // playback disabled pending the Play Console foreground-service-permissions
    // declaration - see AudioPlayer.java), so lint can no longer see a
    // foregroundServiceType for it. Suppressed rather than reworked, since the
    // manifest entry (and this call) come back together when re-enabled.
    @android.annotation.SuppressLint("ForegroundServiceType")
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
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_IMMUTABLE);


        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent playPauseIntent = new Intent(this, AudioPlayerService.class);
        playPauseIntent.setAction("TOGGLE");

        PendingIntent actionIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            actionIntent = PendingIntent.getForegroundService(
                    this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE
            );
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Now Playing")
                .setContentText("Desi Kahani Audio Player")
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)

                .addAction(isPlaying ? R.drawable.pause : R.drawable.play,
                        isPlaying ? "Pause" : "Play", actionIntent)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0))
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    private void sendProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
        isServiceRunning = false;

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
    }
}
