package com.bhola.desiKahaniyaAdult2;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class AudioPlayerOffline extends AppCompatActivity {
    ImageView playBtn;
    LinearLayout progressbar;
    LinearLayout playBtn_and_SeekbarLayout;
    TextView loadingMessage;
    MediaPlayer mediaPlayer;
    int pausePosition = -1;
    String storyURL, storyName;
    int temp = 0;
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


        progressbar = findViewById(R.id.progressbar);
        playBtn_and_SeekbarLayout = findViewById(R.id.playBtn_and_SeekbarLayout);
        loadingMessage = findViewById(R.id.message);
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
                if (!mediaPlayer.isPlaying()) {  //  PLAY
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mediaPlayer.seekTo(pausePosition - 500);
                    mediaPlayer.start();
                    playBtn.setBackgroundResource(R.drawable.pause);
                    Toast.makeText(AudioPlayerOffline.this, "Resumed", Toast.LENGTH_SHORT).show();
                    lottie.setVisibility(View.VISIBLE);

                } else if (mediaPlayer.isPlaying()) { //   PAUSE
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mediaPlayer.pause();
                    playBtn.setBackgroundResource(R.drawable.play);
                    pausePosition = mediaPlayer.getCurrentPosition();
                    playBtn.setBackgroundResource(R.drawable.play);
                    Toast.makeText(AudioPlayerOffline.this, "Paused", Toast.LENGTH_SHORT).show();
                    lottie.setVisibility(View.INVISIBLE);

                }

            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    seekBar.setProgress(progress);
                    mediaPlayer.seekTo(progress);
                    setCurrentTime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playBtn.setBackgroundResource(R.drawable.play);
                Toast.makeText(AudioPlayerOffline.this, "Finished", Toast.LENGTH_SHORT).show();
                try {
                    onBackPressed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
    private void startPlayingAudio() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                handler = new Handler();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(storyURL);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        try {
                            seekbar.setMax(mediaPlayer.getDuration());
                            updateSeekbar();
                            setCurrentTime();
                            mp.start();
                            lottie.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


                Toast.makeText(AudioPlayerOffline.this, "Playing", Toast.LENGTH_SHORT).show();
                playBtn.setBackgroundResource(R.drawable.pause);
            }
            temp = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAds() {
        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }
    }

    private void setCurrentTime() {
        int currentProgressinSeconds = mediaPlayer.getCurrentPosition() / 1000;
        int totalTimeInSecond = mediaPlayer.getDuration() / 1000 - currentProgressinSeconds;
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

    private void updateSeekbar() {
        pausePosition = mediaPlayer.getCurrentPosition();
        seekbar.setProgress(pausePosition);
        runnable = new Runnable() {
            @Override
            public void run() {
                updateSeekbar();
                setCurrentTime();
            }
        };
        handler.postDelayed(runnable, 1000);
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


        try {
            loadAds();
            handler.removeCallbacks(runnable);
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                Toast.makeText(AudioPlayerOffline.this, "Stopped", Toast.LENGTH_SHORT).show();
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.d("TAGA", "onBackPressed: " + e.getMessage());
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            playBtn.performClick();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Toast.makeText(AudioPlayerOffline.this, "Stopped", Toast.LENGTH_SHORT).show();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

}