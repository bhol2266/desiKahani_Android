package com.bhola.desiKahaniya;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;


public class
Collection_GridView extends AppCompatActivity {
    String Ads_State;
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    AlertDialog dialog;

    com.facebook.ads.AdView facebook_adView;
    String TAG = "TAGA";
    AdView mAdView;

    ViewPager viewPager;
    TabLayout tabLayout;
    TabItem tabItem1, tabItem2;
    PageAdapter pageAdapter;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    RewardedInterstitialAd mRewardedInterstitial;
    final int PERMISSION_REQUEST_CODE = 100;
    private InAppUpdate inAppUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection__grid_view);

        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }
        checkForupdate();


        navigationDrawer();
        tabview();
        askForNotificationPermission(); //Android 13 and higher


    }

    private void tabview() {
        tabLayout = (TabLayout) findViewById(R.id.tablayout1);
        tabItem1 = (TabItem) findViewById(R.id.tab1);
        tabItem2 = (TabItem) findViewById(R.id.tab2);
        viewPager = (ViewPager) findViewById(R.id.vpager);

        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0 || tab.getPosition() == 1)
                    pageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //listen for scroll or page change

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
        exit_dialog();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (facebook_adView != null) {
            facebook_adView.destroy();
        }

        if (facebook_IntertitialAds != null) {
            facebook_IntertitialAds.destroy();

        }

    }

    private void exit_dialog() {


        Button exit, exit2;
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(nav.getContext());
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.exit_dialog, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        if (!(SplashScreen.Login_Times < 4)) {
            TextView exitMSG;
            exitMSG = promptView.findViewById(R.id.exitMSG);
            exitMSG.setVisibility(View.VISIBLE);
        }


        if ((SplashScreen.Ads_State.equals("active") && SplashScreen.Ad_Network_Name.equals("admob"))) {
            AdView mAdView2;
            mAdView2 = promptView.findViewById(R.id.adView2);
            ADS_ADMOB.BannerAd(this, mAdView2);
        }
        if ((SplashScreen.Ads_State.equals("active") && SplashScreen.Ad_Network_Name.equals("facebook"))) {
            LinearLayout facebook_bannerAd_layput;
            facebook_bannerAd_layput = promptView.findViewById(R.id.banner_container);
            ADS_FACEBOOK.bannerAds(this, facebook_adView, facebook_bannerAd_layput, getString(R.string.Facebook_BannerAdUnit));
        }

        exit = promptView.findViewById(R.id.exit_button2);
        exit2 = promptView.findViewById(R.id.exit_button1);


        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SplashScreen.exit_Refer_appNavigation.equals("active") && SplashScreen.Login_Times < 2 && SplashScreen.Refer_App_url2.length() > 10) {

                    Intent j = new Intent(Intent.ACTION_VIEW);
                    j.setData(Uri.parse(SplashScreen.Refer_App_url2));
                    try {
                        startActivity(j);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finishAffinity();
                    System.exit(0);
                    finish();
                    dialog.dismiss();

                } else {

                    finishAffinity();
                    finish();
                    System.exit(0);
                    finish();
                    dialog.dismiss();

                }
            }
        });

        exit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });


        dialog = builder.create();
        dialog.show();

    }


    private void navigationDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nav = (NavigationView) findViewById(R.id.navmenu);
        nav.setItemIconTintList(null);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.menu_downloads:
                        Intent intent = new Intent(getApplicationContext(), Download_Detail.class);
                        intent.putExtra("Ads_Status", Ads_State);
                        startActivity(intent);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_audio:
                        startActivity(new Intent(getApplicationContext(), OfflineAudioStory.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_contacts:
                        TextView whatsapp, email;
                        AlertDialog.Builder builder = new AlertDialog.Builder(Collection_GridView.this);
                        LayoutInflater inflater = LayoutInflater.from(Collection_GridView.this);
                        View promptView = inflater.inflate(R.layout.navigation_menu_contacts, null);
                        builder.setView(promptView);
                        builder.setCancelable(true);
                        whatsapp = promptView.findViewById(R.id.whatsappnumber);
                        whatsapp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", "+919108825914");
                                clipboard.setPrimaryClip(clip);
                                navigationDrawer();
                                Toast.makeText(v.getContext(), "COPIED NUMBER", Toast.LENGTH_SHORT).show();
                            }
                        });
                        email = promptView.findViewById(R.id.email);
                        email.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", "bhola2266@gmail.com");
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(v.getContext(), "COPIED EMAIL", Toast.LENGTH_SHORT).show();
                            }
                        });


                        dialog = builder.create();
                        dialog.show();
                        drawerLayout.closeDrawer(GravityCompat.START);

                        break;

                    case R.id.menu_rating:


                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(SplashScreen.Main_App_url1));
                        startActivity(i);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.menu_notificaton:
                        Intent intent2 = new Intent(getApplicationContext(), Notification_Story_Detail.class);
                        intent2.putExtra("Ads_Status", Ads_State);
                        startActivity(intent2);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_share_app:
                        String share_msg = "Hi I have downloaded Hindi Desi Kahani App.\n" +
                                "It is a best app for Real Desi Bed Stories.\n" +
                                "You should also try\n" +
                                SplashScreen.Main_App_url1;
                        Intent intent1 = new Intent();
                        intent1.setAction(Intent.ACTION_SEND);
                        intent1.putExtra(Intent.EXTRA_TEXT, share_msg);
                        intent1.setType("text/plain");
                        intent = Intent.createChooser(intent1, "Share By");
                        startActivity(intent);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_second_app:

                        if (SplashScreen.Refer_App_url2.length() > 10 && SplashScreen.exit_Refer_appNavigation.equals("active")) {

                            Intent j = new Intent(Intent.ACTION_VIEW);
                            j.setData(Uri.parse(SplashScreen.Refer_App_url2));
                            Log.d("dghsdfghs", "Refer_App_url2: " + SplashScreen.Refer_App_url2);
                            startActivity(j);
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        break;

                    case R.id.Privacy_Policy:

                        Intent i5 = new Intent(Intent.ACTION_VIEW);
                        i5.setData(Uri.parse("https://sites.google.com/view/desikhaniya"));
                        startActivity(i5);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.About_Us:

                        final androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(nav.getContext());
                        LayoutInflater inflater2 = LayoutInflater.from(getApplicationContext());
                        View promptView2 = inflater2.inflate(R.layout.about_us, null);
                        builder2.setView(promptView2);
                        builder2.setCancelable(true);


                        dialog = builder2.create();
                        dialog.show();

                        break;


                    case R.id.Terms_and_Condition:
                        Intent intent27 = new Intent(getApplicationContext(), TermsAndConditions.class);
                        intent27.putExtra("Ads_Status", Ads_State);
                        startActivity(intent27);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }

                return true;
            }
        });
    }

    private void checkForupdate() {
        inAppUpdate = new InAppUpdate(Collection_GridView.this);
        inAppUpdate.checkForAppUpdate();

    }


    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(Collection_GridView.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Collection_GridView.this, "Allow Notification for Daily new Stories ", Toast.LENGTH_LONG).show();
                    }
                }, 1000);
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }
}