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
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import androidx.multidex.BuildConfig;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import com.google.android.play.core.review.ReviewException;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    final int PERMISSION_REQUEST_CODE = 112;
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
//        insertDataIN_Database();
//        checkForAppUpdate();
        if (SplashScreen.Login_Times < 3) {
//            getUserLocaitonUsingIP();
        }

        ImageView VipMembership = findViewById(R.id.VipLottie);

        if (SplashScreen.App_updating.equals("active")) {
            VipMembership.setVisibility(View.GONE);
        }
        VipMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SplashScreen.isInternetAvailable(Collection_GridView.this)) {
                    startActivity(new Intent(Collection_GridView.this, VipMembership.class));

                } else {
                    Toast.makeText(Collection_GridView.this, "Check Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void showAds() {

        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            mAdView = findViewById(R.id.adView);
            ADS_ADMOB.BannerAd(this, mAdView);

        } else {
            LinearLayout facebook_bannerAd_layput;
            facebook_bannerAd_layput = findViewById(R.id.banner_container);
            if (!SplashScreen.homepageAdShown) {
                ADS_FACEBOOK.bannerAds(this, facebook_adView, facebook_bannerAd_layput, getString(R.string.Facebook_BannerAdUnit));
            }
        }
    }


    @Override
    public void onBackPressed() {
        exit_dialog();
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(Collection_GridView.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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


    private void tabview() {
        tabLayout = findViewById(R.id.tablayout1);
        tabItem1 = findViewById(R.id.tab1);
        tabItem2 = findViewById(R.id.tab2);
        viewPager = findViewById(R.id.vpager);

        if (SplashScreen.App_updating.equals("active")) {
            tabLayout.removeTabAt(1); // Remove the second tab
        }


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

    private void checkForAppUpdate() {

        if (SplashScreen.Firebase_Version_Code != SplashScreen.currentApp_Version) {

            Button updateBtn;
            TextView yourVersion, latestVersion;
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Collection_GridView.this);
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View promptView = inflater.inflate(R.layout.appupdate, null);
            builder.setView(promptView);
            builder.setCancelable(!SplashScreen.update_Mandatory);


            updateBtn = promptView.findViewById(R.id.UpdateBtn);
            yourVersion = promptView.findViewById(R.id.currentVersion);
            yourVersion.setText("Your Version: " + BuildConfig.VERSION_CODE);
            latestVersion = promptView.findViewById(R.id.NewerVersion);
            latestVersion.setText("Latest Version: " + SplashScreen.Firebase_Version_Code);
            updateBtn = promptView.findViewById(R.id.UpdateBtn);

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(SplashScreen.apk_Downloadlink));
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
            });


            AlertDialog dialog2 = builder.create();
            dialog2.show();
        }
    }

    private void installsDB() {
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final boolean[] idMatched = {false};

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("ANDROID_ID", android_id);
        data.put("Location", SplashScreen.countryLocation);
        data.put("Date", new java.util.Date());

        firestore.collection("Devices").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (android_id.equals(document.getData().get("ANDROID_ID").toString())) {
                            idMatched[0] = true;
                        }
                    }
                    if (!idMatched[0]) {
                        firestore.collection("Devices").document(android_id).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Collection_GridView.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


    }

    private void getUserLocaitonUsingIP() {
        String API_URL = "https://api.db-ip.com/v2/free/self";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, API_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            SplashScreen.countryLocation = jsonObject.getString("countryName");
                            SplashScreen.countryCode = jsonObject.getString("countryCode");
                            installsDB(); // record device id in firestore using android id

                        } catch (JSONException e) {
                            e.printStackTrace();
                            installsDB(); // record device id in firestore using android id
                            Log.d(TAG, "JSONException: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(Collection_GridView.this);
        requestQueue.add(stringRequest);
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

        if (SplashScreen.Login_Times > 5) {
            TextView exitMSG;
            exitMSG = promptView.findViewById(R.id.exitMSG);
            exitMSG.setVisibility(View.VISIBLE);
            showRateApp();

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

        if (SplashScreen.Ads_State.equals("active")) {

        }
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SplashScreen.exit_Refer_appNavigation.equals("active") && SplashScreen.Login_Times < 3 && SplashScreen.Refer_App_url2.length() > 10) {

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nav = findViewById(R.id.navmenu);
        nav.setItemIconTintList(null);
        drawerLayout = findViewById(R.id.drawer);
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
                                ClipData clip = ClipData.newPlainText("label", "ukdevelopers007@gmail.com");
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

    public void showRateApp() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();

                Task<Void> flow = manager.launchReviewFlow(Collection_GridView.this, reviewInfo);
                flow.addOnCompleteListener(task2 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode int reviewErrorCode = ((ReviewException) task.getException()).getErrorCode();
            }
        });


    }



    private void insertDataIN_Database() {
        ArrayList<HashMap<String, String>> Category_List = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> m_li;

        try {

            JSONArray m_jArry = new JSONArray(loadJSONFromAsset());
            for (int i = 0; i < m_jArry.length(); i++) {


                JSONObject json_obj = m_jArry.getJSONObject(i);

                String Title = json_obj.getString("Title");
                String href = json_obj.getString("href");
                String date = json_obj.getString("date");
                int completeDate = json_obj.getInt("completeDate");
                String views = json_obj.getString("views");
                String description = json_obj.getString("description");
                String audiolink = json_obj.getString("audiolink");

                JSONObject categoryObject = json_obj.getJSONObject("category");
                String category = categoryObject.getString("title");

                JSONArray tagsArray = json_obj.getJSONArray("tagsArray");
                ArrayList<String> tagsList = new ArrayList();
                for (int j = 0; j < tagsArray.length(); j++) {
                    tagsList.add(tagsArray.getString(j));
                }
                String tags = String.join(", ", tagsList);


                JSONArray relatedStoriesLinks_Array = json_obj.getJSONArray("relatedStoriesLinks");
                ArrayList<String> relatedStoriesList = new ArrayList();
                for (int j = 0; j < relatedStoriesLinks_Array.length(); j++) {
                    JSONObject relatedStoriesLinksObject = (JSONObject) relatedStoriesLinks_Array.get(j);
                    relatedStoriesList.add(relatedStoriesLinksObject.getString("title"));
                }
                String relatedStories = String.join(", ", relatedStoriesList);

                JSONArray storiesInsideParagraph_Array = json_obj.getJSONArray("storiesLink_insideParagrapgh");
                ArrayList<String> storiesInsideParagraphList = new ArrayList();
                for (int j = 0; j < storiesInsideParagraph_Array.length(); j++) {
                    JSONObject obj = (JSONObject) storiesInsideParagraph_Array.get(j);
                    storiesInsideParagraphList.add(obj.getString("title"));
                }
                String storiesInsideParagraph = String.join(", ", storiesInsideParagraphList);

                //Add your values in your `ArrayList` as below:
                m_li = new HashMap<String, String>();
                m_li.put("Title", Title);
                m_li.put("href", href);
                m_li.put("date", date);
                m_li.put("views", views);
                m_li.put("description", description);
                m_li.put("audiolink", audiolink);
                m_li.put("category", category);
                m_li.put("tags", tags);
                m_li.put("relatedStories", relatedStories);
                m_li.put("completeDate", String.valueOf(completeDate));
                m_li.put("storiesInsideParagraph", storiesInsideParagraph);
                Category_List.add(m_li);


                DatabaseHelper insertRecord = new DatabaseHelper(getApplicationContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems");
                String res = insertRecord.addstories(m_li);
                Log.d(TAG, "INSERT DATA: " + res);
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Datebase Error: " + e.getMessage());

        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = Collection_GridView.this.getAssets().open("storymodels.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }


}

