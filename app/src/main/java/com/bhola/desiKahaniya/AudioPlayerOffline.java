package com.bhola.desiKahaniya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class AudioPlayerOffline extends AppCompatActivity {
    ImageView playBtn;
    LinearLayout playBtn_and_SeekbarLayout;
    int pausePosition = -1;
    String storyURL, storyName;
    int temp = 0;
    /** Downloaded stories play through AudioPlayerService too, so leaving this
     *  screen does not stop them - same behaviour as the streaming player. */
    private boolean playbackReady = false;
    private boolean isPlaying = false;
    private BroadcastReceiver playbackReceiver;
    SeekBar seekbar;
    Runnable runnable;
    Handler handler;
    TextView currentTime, storyTitle;
    LottieAnimationView lottie;
    // Ads Stuff
    AdView mAdView;
    RewardedInterstitialAd mRewardedInterstitial;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player_offline);

        loadAds();


        playBtn_and_SeekbarLayout = findViewById(R.id.playBtn_and_SeekbarLayout);
        storyTitle = findViewById(R.id.storyTitle);
        currentTime = findViewById(R.id.currentTime);
        seekbar = findViewById(R.id.seekbar);
        playBtn = findViewById(R.id.playBtn);
        playBtn.setBackgroundResource(R.drawable.play);
        lottie = findViewById(R.id.lottie);

        storyURL = getIntent().getStringExtra("storyURL");
        storyName = getIntent().getStringExtra("storyName");
        storyTitle.setText(storyName.replaceAll("_"," ").replace(".mp3",""));

        startPlayingAudio(); // This is the service class that will run in the background

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playbackReady) return;
                setPlayingUi(!isPlaying);
                sendToService("TOGGLE", null);
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && playbackReady) {
                    sendToService("SEEK", progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void startPlayingAudio() {
        registerPlaybackReceiver();
        handler = new Handler();

        Intent service = new Intent(this, AudioPlayerService.class);
        if (AudioPlayerService.isServiceRunning
                && storyURL != null
                && storyURL.equals(AudioPlayerService.CURRENT_AUDIO_URL)) {
            service.setAction("SYNC");
        } else {
            service.putExtra("storyURL", storyURL);
            service.putExtra("storyName", storyName);
            service.putExtra("title", storyName);
            service.putExtra("audioHref", "");
            service.putExtra("AudioDownloadState", "downloaded");
        }
        ContextCompat.startForegroundService(this, service);
        temp = 1;
    }

    private void sendToService(String action, Integer seekTo) {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(action);
        if (seekTo != null) intent.putExtra("seekTo", seekTo.intValue());
        ContextCompat.startForegroundService(this, intent);
    }

    private void registerPlaybackReceiver() {
        if (playbackReceiver != null) return;

        playbackReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;

                if ("PROGRESS_UPDATE".equals(action)) {
                    int current = intent.getIntExtra("current", 0);
                    onPlaybackReady(intent.getIntExtra("duration", 0));
                    pausePosition = current;
                    seekbar.setProgress(current);
                    setCurrentTime(current, seekbar.getMax());
                    setPlayingUi(intent.getBooleanExtra("isPlaying", false));
                } else if ("PAUSE_PLAY_BTN_UPDATE".equals(action)) {
                    onPlaybackReady(intent.getIntExtra("duration", 0));
                    setPlayingUi("PLAY".equals(intent.getStringExtra("PAUSE_PLAY_BTN_UPDATE")));
                } else if (AudioPlayerService.ACTION_COMPLETED.equals(action)) {
                    setPlayingUi(false);
                    Toast.makeText(AudioPlayerOffline.this, "Finished", Toast.LENGTH_SHORT).show();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("PROGRESS_UPDATE");
        filter.addAction("PAUSE_PLAY_BTN_UPDATE");
        filter.addAction(AudioPlayerService.ACTION_COMPLETED);

        ContextCompat.registerReceiver(this, playbackReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void onPlaybackReady(int duration) {
        if (playbackReady || duration <= 0) return;
        playbackReady = true;
        seekbar.setMax(duration);
    }

    private void setPlayingUi(boolean playing) {
        isPlaying = playing;
        playBtn.setBackgroundResource(playing ? R.drawable.pause : R.drawable.play);
        lottie.setVisibility(playing ? View.VISIBLE : View.INVISIBLE);
    }

    private void loadAds() {
        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }
    }

    /** Shows time remaining, as before - now fed by the service's progress ticks. */
    private void setCurrentTime(int currentMs, int durationMs) {
        int currentProgressinSeconds = currentMs / 1000;
        int totalTimeInSecond = durationMs / 1000 - currentProgressinSeconds;
        int minutes = totalTimeInSecond / 60;
        int seconds = totalTimeInSecond - (minutes * 60);


        currentTime.setText(minutes + ":" + seconds);
        if (minutes < 10) {
            currentTime.setText("0" + minutes + ":" + seconds);
        }
        if (seconds < 10) {
            currentTime.setText(minutes + ":" + "0" + seconds);
        }
        if (minutes < 10 && seconds < 10) {
            currentTime.setText("0" + minutes + ":" + "0" + seconds);
        }


    }

    public void backBtn(View view) {
        onBackPressed();
    }

    private void showAds() {


        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            mAdView = findViewById(R.id.adView);
            ADS_ADMOB.BannerAd(this, mAdView);

        } else {
            LinearLayout facebook_bannerAd_layput;
            facebook_bannerAd_layput = findViewById(R.id.banner_container);
            ADS_FACEBOOK.bannerAds(this, facebook_adView, facebook_bannerAd_layput, getString(R.string.Facebook_BannerAdUnit));
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (SplashScreen.Ads_State.equals("active")) {
            if (SplashScreen.Ad_Network_Name.equals("admob")) {
                ADS_ADMOB.Interstitial_Ad(this);

            } else {
                ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));

            }
        }


        // Playback deliberately continues after leaving this screen; the
        // notification is the control from here on.
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AudioPlayerService.isServiceRunning) sendToService("SYNC", null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        if (playbackReceiver != null) {
            unregisterReceiver(playbackReceiver);
            playbackReceiver = null;
        }
        // The service keeps playing on purpose - see onBackPressed().
    }

}