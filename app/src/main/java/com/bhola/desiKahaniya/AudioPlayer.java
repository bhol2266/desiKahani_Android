package com.bhola.desiKahaniya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioPlayer extends AppCompatActivity {

    ImageView playBtn;
    LinearLayout progressbar, playBtn_and_SeekbarLayout;
    TextView loadingMessage;
    TextView currentTime;
    TextView storyTitle;
    TextView description;
    String AudioDownloadState;
    ProgressBar progressbarUnit;
    SeekBar seekbar;
    LottieAnimationView lottie;

    Handler handler;
    Runnable runnable;
    MediaPlayer mediaPlayer;

    String storyURL, storyName, title, audioHref;
    boolean isPlaying = true;


    // Ads
    com.google.android.gms.ads.AdView mAdView;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;

    // Download
    Button cancelbtn;
    AlertDialog dialog;
    ProgressBar progressbarDownload;
    DownloadFileFromURL downloadTask;
    ImageView downloadBtn;
    TextView downloadSize, progress_indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        initViews();

        // Extras arriving via the notification-resume path (AudioPlayerService -> SplashScreen ->
        // here) are already plain text, since AudioPlayer decrypted them once before ever handing
        // them to the service. Only a fresh launch (e.g. from ftab2.java) carries encrypted extras.
        if ("ComingFromAudioPlayer".equals(getIntent().getStringExtra("ComingFromAudioPlayer"))) {
            storyURL = getIntent().getStringExtra("storyURL");
            audioHref = getIntent().getStringExtra("audioHref");
            title = getIntent().getStringExtra("title");
        } else {
            storyURL = SplashScreen.decryption(getIntent().getStringExtra("storyURL"));
            audioHref = SplashScreen.decryption(getIntent().getStringExtra("audioHref"));
            title = SplashScreen.decryption(getIntent().getStringExtra("title"));
        }
        storyName = getIntent().getStringExtra("storyName");
        AudioDownloadState = getIntent().getStringExtra("AudioDownloadState");
        storyTitle.setText(storyName.replace("-", " ").trim());


        startPlayingAudio();
        setListeners();
        downloadAudio();
        updateStoryread();
        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }
    }


    private void initViews() {
        progressbar = findViewById(R.id.progressbar);
        playBtn_and_SeekbarLayout = findViewById(R.id.playBtn_and_SeekbarLayout);
        loadingMessage = findViewById(R.id.message);
        storyTitle = findViewById(R.id.storyTitle);
        currentTime = findViewById(R.id.currentTime);
        seekbar = findViewById(R.id.seekbar);
        playBtn = findViewById(R.id.playBtn);
        progressbarUnit = findViewById(R.id.progressbarUnit);
        lottie = findViewById(R.id.lottie);
        downloadBtn = findViewById(R.id.downloadBtn);


        playBtn.setImageResource(R.drawable.play);
    }

    private void setListeners() {
        View reportBtn = findViewById(R.id.reportBtn);
        if (reportBtn != null) {
            reportBtn.setOnClickListener(v ->
                    ReportDialog.show(AudioPlayer.this, ReportDialog.TYPE_AUDIO, storyName, audioHref));
        }

        playBtn.setOnClickListener(v -> {
            if (mediaPlayer == null) return;

            if (isPlaying) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
                startProgressUpdates();
            }
            isPlaying = !isPlaying;

            playBtn.setImageResource(isPlaying ? R.drawable.pause : R.drawable.play);
            lottie.setVisibility(isPlaying ? View.VISIBLE : View.INVISIBLE);
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
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

    // Plays directly in this Activity - no background/foreground service, so
    // playback stops as soon as the screen is left (see onPause()). Background
    // playback (AudioPlayerService) is disabled for now; see AndroidManifest.xml.
    private void startPlayingAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(storyURL);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                seekbar.setMax(mp.getDuration());
                progressbarUnit.setVisibility(View.INVISIBLE);
                progressbar.setVisibility(View.INVISIBLE);
                playBtn_and_SeekbarLayout.setVisibility(View.VISIBLE);
                lottie.setVisibility(View.VISIBLE);
                playBtn.setImageResource(R.drawable.pause);
                startProgressUpdates();
            });

            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                progressbarUnit.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.VISIBLE);
                progressbarUnit.setProgress(percent);
                loadingMessage.setText(percent + " % bufferring");
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playBtn.setImageResource(R.drawable.play);
                lottie.setVisibility(View.INVISIBLE);
            });

            // Fallback: if the primary URL fails, retry with the URL built from audioHref.
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                try {
                    mp.reset();
                    mp.setDataSource(SplashScreen.databaseURL + "Sexstory_Audiofiles/" + audioHref + ".mp3");
                    mp.setOnPreparedListener(mp2 -> {
                        mp2.start();
                        isPlaying = true;
                        seekbar.setMax(mp2.getDuration());
                        progressbarUnit.setVisibility(View.INVISIBLE);
                        progressbar.setVisibility(View.INVISIBLE);
                        playBtn_and_SeekbarLayout.setVisibility(View.VISIBLE);
                        lottie.setVisibility(View.VISIBLE);
                        playBtn.setImageResource(R.drawable.pause);
                        startProgressUpdates();
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
    }

    // Ticks the seekbar/currentTime once a second while playing.
    private void startProgressUpdates() {
        if (handler == null) handler = new Handler();
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int current = mediaPlayer.getCurrentPosition();
                    seekbar.setProgress(current);
                    currentTime.setText(format(current));
                    currentTime.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Leaving the screen stops playback - there's no background service to
        // keep it going, by design (see startPlayingAudio()). The activity isn't
        // destroyed by Home/backgrounding, so playBtn/lottie must be resynced here
        // too, or they're left showing "playing" when the user comes back.
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            playBtn.setImageResource(R.drawable.play);
            lottie.setVisibility(View.INVISIBLE);
        }
        if (handler != null) handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacks(runnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public void backBtn(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        try {
            // Show interstitial ad if enabled
            if ("active".equals(SplashScreen.Ads_State)) {
                if ("admob".equals(SplashScreen.Ad_Network_Name)) {
                    ADS_ADMOB.Interstitial_Ad(this);
                } else {
                    ADS_FACEBOOK.interstitialAd(
                            this,
                            facebook_IntertitialAds,
                            getString(R.string.Facebook_InterstitialAdUnit)
                    );
                }
            }

            // Remove any pending callbacks
            if (handler != null) handler.removeCallbacks(runnable);

            // If the user came from AudioPlayer, redirect to Collection_GridView
            if ("ComingFromAudioPlayer".equals(getIntent().getStringExtra("ComingFromAudioPlayer"))) {
                Intent intent = new Intent(getApplicationContext(), Collection_GridView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Optional: finish current activity
                return;   // Prevent calling super.onBackPressed()
            }

        } catch (Exception e) {
            Log.d("TAGA", "onBackPressed Exception: " + e.getMessage());
        }

        // Default back behavior
        super.onBackPressed();
    }


    private String format(int millis) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    private void downloadAudio() {
        ImageView downloadBtn;
        downloadBtn = findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadDialog();


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
                    @SuppressLint("RestrictedApi") Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

                    TextView gotoDownloads = customSnackView.findViewById(R.id.gotoDownloads);
                    gotoDownloads.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
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

    private void downloadDialog() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(AudioPlayer.this);
        LayoutInflater inflater = LayoutInflater.from(AudioPlayer.this);
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


        int position = getIntent().getIntExtra("position", 0); // defaultValue is the value to be used if the key doesn't exist

        if (position != -1) {
            try {

                ftab2.adapter2.notifyItemChanged(position);
            } catch (Exception e) {
                // sometimes it throws exception
            }
            new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").updateStoryRead(title, 1);
        }
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
