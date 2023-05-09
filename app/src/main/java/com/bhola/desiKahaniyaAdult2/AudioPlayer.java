package com.bhola.desiKahaniyaAdult2;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.airbnb.lottie.LottieAnimationView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class AudioPlayer extends AppCompatActivity {
    ImageView playBtn;
    LinearLayout progressbar;
    LinearLayout playBtn_and_SeekbarLayout;
    TextView loadingMessage;
    MediaPlayer mediaPlayer;
    int pausePosition = -1;
    String storyURL, storyName,title;
    SeekBar seekbar;
    Runnable runnable;
    Handler handler;
    TextView currentTime, storyTitle;
    LottieAnimationView lottie;
    ProgressBar progressbarUnit;
    boolean URL_notWorking = false;

    // Ads Stuff
    AdView mAdView;
    RewardedInterstitialAd mRewardedInterstitial;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;
    final boolean[] isPlayingBoolean = {true};

    //Story Download stuffs
    DownloadManager manager;
    private ProgressDialog mdialog;
    public static final int progress_bar_type = 0;
    AlertDialog dialog;
    TextView description, progress_indicator;
    Button cancelbtn;
    ProgressBar progressbarDownload;
    DownloadFileFromURL downloadTask;
    TextView downloadSize;
    CoordinatorLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        loadAds();
        downloadAudio();

        progressbar = findViewById(R.id.progressbar);
        playBtn_and_SeekbarLayout = findViewById(R.id.playBtn_and_SeekbarLayout);
        loadingMessage = findViewById(R.id.message);
        storyTitle = findViewById(R.id.storyTitle);
        currentTime = findViewById(R.id.currentTime);
        seekbar = findViewById(R.id.seekbar);
        playBtn = findViewById(R.id.playBtn);
        playBtn.setBackgroundResource(R.drawable.play);
        progressbarUnit = findViewById(R.id.progressbarUnit);
        lottie = findViewById(R.id.lottie);

        storyURL = SplashScreen.decryption(getIntent().getStringExtra("storyURL"));
        title = SplashScreen.decryption(getIntent().getStringExtra("title"));
        storyName = getIntent().getStringExtra("storyName");
        storyTitle.setText(storyName.replace("-", " ").trim());

        startPlayingAudio(); // This is the service class that will run in the background

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {  //  PLAY
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mediaPlayer.seekTo(pausePosition - 500);
                    mediaPlayer.start();
                    playBtn.setBackgroundResource(R.drawable.pause);
                    Toast.makeText(AudioPlayer.this, "Resumed", Toast.LENGTH_SHORT).show();
                    isPlayingBoolean[0] = true;

                } else if (mediaPlayer.isPlaying()) { //   PAUSE
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mediaPlayer.pause();
                    playBtn.setBackgroundResource(R.drawable.play);
                    pausePosition = mediaPlayer.getCurrentPosition();
                    playBtn.setBackgroundResource(R.drawable.play);
                    Toast.makeText(AudioPlayer.this, "Paused", Toast.LENGTH_SHORT).show();
                    isPlayingBoolean[0] = false;
                    lottie.setVisibility(View.INVISIBLE);

                }

            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    progressbar.setVisibility(View.VISIBLE);
                    progressbarUnit.setVisibility(View.VISIBLE);
                    lottie.setVisibility(View.INVISIBLE);
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


        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (percent >= 5) {
                    if (isPlayingBoolean[0]) {
                        mp.start();
                        lottie.setVisibility(View.VISIBLE);
                        progressbarUnit.setVisibility(View.GONE);
                    }
                    playBtn_and_SeekbarLayout.setVisibility(View.VISIBLE);
                }

                loadingMessage.setText(Integer.toString(percent) + "% buffered");

                if (percent == 100) {
                    progressbar.setVisibility(View.INVISIBLE);
                } else {
                    progressbar.setVisibility(View.VISIBLE);

                }
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                URL_notWorking = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingMessage.setText("Audio link not working, Please try another story");
                        loadingMessage.setTextSize(20);
                        progressbarUnit.setVisibility(View.GONE);
                    }
                },1000);

                mp.stop();

                return false;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playBtn.setBackgroundResource(R.drawable.play);
                if (!URL_notWorking) {
                    Toast.makeText(AudioPlayer.this, "Finished", Toast.LENGTH_SHORT).show();
                    try {
                        onBackPressed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        updateStoryread();
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
                            Toast.makeText(AudioPlayer.this, "Playing", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                });


                playBtn.setBackgroundResource(R.drawable.pause);
            }
        } catch (Exception e) {
            Toast.makeText(this, "LINK BROKEN", Toast.LENGTH_SHORT).show();
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


    private void downloadAudio() {
        ImageView downloadBtn;
        downloadBtn = findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadDialog();
                if (mediaPlayer.isPlaying()) {
                    playBtn.performClick();
                }

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("Download", Context.MODE_PRIVATE);
                File file = new File(directory, storyName.replaceAll(" ", "_") + ".mp3");

                if (!file.exists()) {
                    downloadTask = new DownloadFileFromURL();
                    downloadTask.execute(storyURL);
                } else {

                    final Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_LONG);
                    View customSnackView = getLayoutInflater().inflate(R.layout.custom_snackbar_view, null);
                    // now change the layout of the snackbar
                    Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

                    TextView gotoDownloads = customSnackView.findViewById(R.id.gotoDownloads);
                    gotoDownloads.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(AudioPlayer.this, OfflineAudioStory.class));
                        }
                    });

                    // add the custom snack bar layout to snackbar layout
                    snackbarLayout.addView(customSnackView, 0);
                    snackbar.show();
                }


            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            loadAds();
            handler.removeCallbacks(runnable); // Seekbar handler
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                Toast.makeText(AudioPlayer.this, "Stopped", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(AudioPlayer.this, "Stopped", Toast.LENGTH_SHORT).show();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    private void showAds() {


        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            mAdView = findViewById(R.id.adView);
            ADS_ADMOB.BannerAd(this, mAdView);

            ADS_ADMOB.Interstitial_Ad(this);

        } else {
            LinearLayout facebook_bannerAd_layput;
            facebook_bannerAd_layput = findViewById(R.id.banner_container);
            ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));
            ADS_FACEBOOK.bannerAds(this, facebook_adView, facebook_bannerAd_layput, getString(R.string.Facebook_BannerAdUnit));
        }


    }


    private void downloadDialog() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(AudioPlayer.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.download_dialog, null);
        builder.setView(promptView);
        builder.setCancelable(false);

        description = promptView.findViewById(R.id.description);
        description.setText(storyName + ".mp3 downloading...");
        progress_indicator = promptView.findViewById(R.id.progress_indicator);
        downloadSize = promptView.findViewById(R.id.downloadSize);
        cancelbtn = promptView.findViewById(R.id.cancelbtn);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AudioPlayer.this, "Download Cancelled", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                downloadTask.cancel(true);
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("Download", Context.MODE_PRIVATE);
                File file = new File(directory, storyName.replaceAll(" ", "_") + ".mp3");
                if (file.exists()) {
                    file.delete();
                }
            }
        });

        progressbarDownload = promptView.findViewById(R.id.seekbar);
        dialog = builder.create();
    }

    private void updateStoryread() {
        new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").updateStoryRead(title, 1);
    }

//Background Async Task to download story

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        int lenghtOfFile;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                lenghtOfFile = connection.getContentLength();


                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("Download", Context.MODE_PRIVATE);
                File file = new File(directory, storyName.replaceAll(" ", "_") + ".mp3");


                // Output stream
                OutputStream output = new FileOutputStream(file);
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }


                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            if (mediaPlayer.isPlaying()) {
                playBtn.performClick();
            }
            // setting progress percentage
            progressbarDownload.setProgress(Integer.parseInt(progress[0]));
            progress_indicator.setText(progress[0] + "%");
            int fileSize_inMB = (lenghtOfFile / 1024) / 1024;
            int progress_percent = Integer.parseInt(progress[0]);
            int progress_inMB = progress_percent * fileSize_inMB;
            downloadSize.setText("(" + progress_inMB / 100 + "MB/" + fileSize_inMB + "MB)");
            downloadSize.setVisibility(View.VISIBLE);

        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dialog.cancel();
            Toast.makeText(AudioPlayer.this, "Download Completed", Toast.LENGTH_SHORT).show();

        }
    }
}