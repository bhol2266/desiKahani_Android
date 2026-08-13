package com.bhola.desiKahaniya;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class SplashScreen extends AppCompatActivity {

    TextView textView;
    LottieAnimationView lottie;
    public static String TAG = "TAGA";
    public static String Notification_Intent_Firebase = "inactive";
    public static String Main_App_url1 = "https://play.google.com/store/apps/details?id=com.bhola.desiKahaniya";
    public static String Refer_App_url2 = "https://play.google.com/store/apps/developer?id=Marveltech+Apps";
    public static String Ads_State = "inactive";
    public static String Ad_Network_Name = "admob";
    public static String DB_NAME = "desikahaniya";
    public static String exit_Refer_appNavigation = "inactive";
    public static String App_updating = "active";
    public static String databaseURL = "https://bucket2266.s3.ap-south-1.amazonaws.com/"; //default

    public static String Notification_ImageURL = "https://hotdesipics.co/wp-content/uploads/2022/06/Hot-Bangla-Boudi-Ki-Big-Boobs-Nangi-Selfies-_002.jpg";
    DatabaseReference url_mref;
    public static int Login_Times = 0;
    public static boolean homepageAdShown = false;
    public static int Native_Ad_Interval = 4;

    com.facebook.ads.InterstitialAd facebook_IntertitialAds;

    public static int DB_VERSION = 1;//manual set
    public static int currentApp_Version = 2;//manual set
    public static int Firebase_Version_Code = 1;//manual set
    public static int DB_VERSION_INSIDE_TABLE = 6; //manual set
    Handler handlerr;

    public static String apk_Downloadlink = "";
    public static String countryLocation = "";
    public static String countryCode = "";
    public static boolean update_Mandatory = false;
    public static String DB_TABLE_NAME = "";  //This is a table name "StoryItems or LoveStory"
    public static String API_URL = "https://clownfish-app-jn7w9.ondigitalocean.app/";
    private FirebaseAnalytics mFirebaseAnalytics;
    public static boolean Vip_Member = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullscreenMode();
        setContentView(R.layout.splash_screen);


        textView = findViewById(R.id.textView_splashscreenn);
        lottie = findViewById(R.id.lottie);

        // The title is drawn where it sits, with no entrance animation.
        //
        // It used to slide up from +600% via setAnimation(). That never ran cleanly:
        // setAnimation() only attaches the animation for some later draw, so the text
        // was painted once at its resting position and then jumped back down to start
        // the slide, and the startup work on this screen held the main thread through
        // the frames the slide needed. Removing it is what actually makes the title
        // appear cleanly - there is no animation left to drop frames.

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        copyDatabase();
        allUrl();
        sharedPrefrences();
        if (SplashScreen.Login_Times > 5) {
            updateStoriesInDB();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LinearLayout progressbar = findViewById(R.id.progressbar);
                progressbar.setVisibility(View.VISIBLE);
//                readStoryFromJson();

            }
        }, 1500);


        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        // Stays inline: it only reads intent extras, and it sets the flag the
        // navigation below branches on. Deferring it would send a notification tap to
        // the home grid instead of the story it was opened for.
        generateFCMToken();

        // The topic subscription is a Firebase Messaging call on the main thread and
        // nothing on this screen depends on it, so it is kept off the frames that draw
        // the splash.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                generateNotification();
            }
        }, 1600);

    }


    private void copyDatabase() {


//      Check For Database is Available in Device or not
        DatabaseHelper databaseHelper = new DatabaseHelper(this, DB_NAME, DB_VERSION, "StoryItems");
        try {
            databaseHelper.CheckDatabases();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void readStoryFromJson() {

        try {

            String json = loadJSONFromAsset("audiostories_c.json");
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String title = decryption(jsonObject.getString("Title"));
                String audiolink = decryption(jsonObject.getString("audiolink"));
                String href = decryption(jsonObject.getString("href")).replace(".mp3","");

                trasferData(title, href, audiolink);
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "readStoryFromJson: " + e.getMessage());
        }


    }


    private void trasferData(String title, String href, String audiolink) {

        Map<String, String> mapObj = new HashMap<>();
        mapObj.put("Title", title);
        mapObj.put("story", "");
        mapObj.put("href", href);
        mapObj.put("date", "04-02-2023");
        mapObj.put("views", "6541");
        mapObj.put("description", "");
        mapObj.put("audiolink",audiolink);
        mapObj.put("category", DatabaseHelper.TABLE_AUDIO_ADULT);
        mapObj.put("tags", "");
        mapObj.put("completeDate", "20230204");
        mapObj.put("storiesInsideParagraph", "");
        mapObj.put("relatedStories", "");


        // Dev-only importer (the caller is commented out); writes into the adult
        // narration table now that audio no longer lives in LoveStory.
        String res = new DatabaseHelper(SplashScreen.this, DB_NAME, DB_VERSION,
                DatabaseHelper.TABLE_AUDIO_ADULT).addstories((HashMap<String, String>) mapObj);
        Log.d(TAG, "onSuccess: " + res);

    }


    private void allUrl() {
        if (!isInternetAvailable(SplashScreen.this)) {

            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Offline: keep the cached App_updating restored in
                    // MyApplication. Clearing it here would let a failed config
                    // read unlock real content, which must never outrank update mode.
                    if (Login_Times > 5) {
                        Ads_State = "active";
                        Ad_Network_Name = "admob";
                    }
                    handler_forIntent();
                }
            }, 2000);

            return;
        } else {
            handlerr = new Handler();
            handlerr.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler_forIntent();
                }
            }, 9000);

        }


        url_mref = FirebaseDatabase.getInstance().getReference().child("Sexy_Desi_Kahani");
        url_mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Every field is read defensively, falling back to the value already
                // restored from the local cache. A partially-populated - or entirely
                // empty - snapshot must never stop the app from opening.
                //
                // This used to unbox version_code/update_Mandatory straight out of
                // getValue(), so an empty snapshot threw NullPointerException here,
                // on the main thread, while the splash screen was still showing: the
                // app died before it ever opened. Google Play rejected v41 for exactly
                // that ("Broken Functionality: your app doesn't open or load").
                try {
                    Refer_App_url2 = cfgString(snapshot, "Refer_App_url2", Refer_App_url2);
                    exit_Refer_appNavigation =
                            cfgString(snapshot, "switch_Exit_Nav", exit_Refer_appNavigation);
                    Ads_State = cfgString(snapshot, "Ads", Ads_State);
                    App_updating =
                            cfgString(snapshot, "updatingApp_on_PLatStore", App_updating);
                    Notification_ImageURL =
                            cfgString(snapshot, "Notification_ImageURL", Notification_ImageURL);
                    Ad_Network_Name = cfgString(snapshot, "Ad_Network", Ad_Network_Name);

                    Firebase_Version_Code =
                            cfgInt(snapshot, "version_code", Firebase_Version_Code);
                    apk_Downloadlink =
                            cfgString(snapshot, "apk_Downloadlink", apk_Downloadlink);
                    update_Mandatory =
                            cfgBool(snapshot, "update_Mandatory", update_Mandatory);
                    databaseURL = cfgString(snapshot, "databaseURL", databaseURL);
                    API_URL = cfgString(snapshot, "API_URL", API_URL);

                    // Keep a local copy so these survive the process being killed.
                    cacheRemoteConfig(SplashScreen.this);
                } catch (Exception e) {
                    // Last-resort net: whatever happens while parsing config, the app
                    // still has to open. Cached/default values stay in effect.
                    Log.d(TAG, "onDataChange config parse failed: " + e.getMessage());
                }

                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handlerr.removeCallbacksAndMessages(null);
                        handler_forIntent();
                    }
                }, 1500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // As above: do not clear App_updating on a failed read.
                if (Login_Times > 5) {
                    Ads_State = "active";
                    Ad_Network_Name = "admob";
                }
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }

        });


    }


    /* ---- Null-safe remote-config readers -------------------------------
       Firebase hands back null for any key that is missing, and can deliver a
       completely empty snapshot from a cold disk cache before the server has
       answered. Each of these returns the caller's existing value in that case
       rather than null (or an NPE on unboxing).                             */

    private static String cfgString(DataSnapshot snapshot, String key, String fallback) {
        Object value = snapshot.child(key).getValue();
        return value == null ? fallback : String.valueOf(value);
    }

    private static int cfgInt(DataSnapshot snapshot, String key, int fallback) {
        Integer value = snapshot.child(key).getValue(Integer.class);
        return value == null ? fallback : value;
    }

    private static boolean cfgBool(DataSnapshot snapshot, String key, boolean fallback) {
        Boolean value = snapshot.child(key).getValue(Boolean.class);
        return value == null ? fallback : value;
    }

    private void generateNotification() {


        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    String msg;
                    if (!task.isSuccessful()) {
                        msg = "Failed";
                        Toast.makeText(SplashScreen.this,
                                msg,
                                Toast.LENGTH_SHORT).show();
                    }


                });
    }


    private void handler_forIntent() {
        lottie.cancelAnimation();

        if (SplashScreen.Vip_Member) {
            vipMemberPrivileges();
        }

        // Coming from the audio notification's PendingIntent (app was relaunched from a killed
        // state) — route straight into AudioPlayer with the extras carried by the notification.
        if ("ComingFromAudioPlayer".equals(getIntent().getStringExtra("ComingFromAudioPlayer"))) {
            Intent intent = new Intent(getApplicationContext(), AudioPlayer.class);
            intent.putExtra("storyURL", getIntent().getStringExtra("storyURL"));
            intent.putExtra("storyName", getIntent().getStringExtra("storyName"));
            intent.putExtra("title", getIntent().getStringExtra("title"));
            intent.putExtra("audioHref", getIntent().getStringExtra("audioHref"));
            intent.putExtra("AudioDownloadState", getIntent().getStringExtra("AudioDownloadState"));
            intent.putExtra("ComingFromAudioPlayer", getIntent().getStringExtra("ComingFromAudioPlayer"));
            startActivity(intent);
            finish();
            return;
        }

        if (Notification_Intent_Firebase.equals("active")) {
            Intent intent = new Intent(getApplicationContext(), Notification_Story_Detail.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), Collection_GridView.class);
            startActivity(intent);
        }
        finish();
    }


    private void generateFCMToken() {

        if (getIntent() != null && getIntent().hasExtra("KEY1")) {
            if (getIntent().getExtras().getString("KEY1").equals("Notification_Story")) {
                Notification_Intent_Firebase = "active";
            }
        }
    }

    static boolean isInternetAvailable(Context context) {
        if (context == null) return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut", "Network is available : FALSE ");
        return false;
    }


    private void sharedPrefrences() {

        SharedPreferences sh = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        //Reading Login Times, then writing the incremented value back.
        int a = sh.getInt("loginTimes", 0);
        Login_Times = a + 1;
        sh.edit().putInt("loginTimes", a + 1).commit();

        if (!hasPurchaseRecord(sh)) {
            return;
        }

        Vip_Member = isMembershipActive(sh);
        if (!Vip_Member) {
            Toast.makeText(this, "Your Membership has expried", Toast.LENGTH_SHORT).show();
        }
    }

    /** SharedPreferences file holding both the login counter and the cached config. */
    private static final String PREFS_NAME = "UserInfo";

    /** Whether a purchase has ever been recorded on this device. */
    static boolean hasPurchaseRecord(SharedPreferences sp) {
        return !"not set".equals(sp.getString("purchaseToken", "not set"))
                && sp.getInt("validity_period", 0) != 0;
    }

    /**
     * Epoch millis this device's stored purchase expires, or -1 if there's no
     * parseable purchase record. Shared by isMembershipActive(), the home-grid
     * renewal banner, and the pre-expiry reminder scheduling.
     */
    static long expiryMillis(SharedPreferences sp) {
        if (!hasPurchaseRecord(sp)) return -1;

        int validity_period = sp.getInt("validity_period", 0);

        // Preferred: the exact purchase timestamp from Google Play, so expiry
        // keeps the time of day it was bought. Records written before this key
        // existed fall back to the date string below, which lands at midnight.
        long purchaseMillis = sp.getLong("purchase_time_millis", 0L);
        if (purchaseMillis > 0L) {
            return purchaseMillis + (validity_period * 86400000L);
        }

        String purchase_date = sp.getString("purchase_date", "not set");
        if ("not set".equals(purchase_date)) return -1;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date originalDate = dateFormat.parse(purchase_date);
            if (originalDate == null) return -1;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(originalDate);
            calendar.add(Calendar.DAY_OF_MONTH, validity_period);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * True when the stored purchase is still inside its validity window.
     * Pure check with no UI, so it is safe to call during process restore.
     */
    static boolean isMembershipActive(SharedPreferences sp) {
        long expiry = expiryMillis(sp);
        if (expiry == -1) return false;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date now = new Date();
        Log.d(TAG, "Membership Expiry Date: " + dateFormat.format(new Date(expiry)));

        // Expiring today counts as expired - matches the original behaviour.
        if (dateFormat.format(new Date(expiry)).equals(dateFormat.format(now))) return false;
        return expiry > now.getTime();
    }

    /**
     * Days since membership lapsed, or -1 if membership is still active or there's
     * no record at all. Used to show the home-grid renewal banner for a short
     * window after expiry rather than nagging forever.
     */
    static int daysSinceExpiry(SharedPreferences sp) {
        if (isMembershipActive(sp)) return -1;
        long expiry = expiryMillis(sp);
        if (expiry == -1) return -1;

        long diff = System.currentTimeMillis() - expiry;
        if (diff < 0) return -1;
        return (int) (diff / 86400000L);
    }

    /**
     * Which SQLite table the current gating state implies. Single source of truth,
     * so ftab1 and the process-restore path below cannot drift apart.
     */
    public static String resolveContentTable() {
        if ("active".equals(App_updating)) return "LoveStory";   // update mode wins
        return (Login_Times >= 6) ? "StoryItems" : "LoveStory";
    }

    /**
     * Relaunches the app from the splash screen and kills the current process.
     *
     * Membership state is read once into statics and then baked into screens that are
     * already built - the home grid, the tabs, every adapter. Flipping Vip_Member on
     * its own therefore leaves a restored member looking at the locked, censored
     * version of the app until they happen to kill it themselves. A clean restart is
     * the only way to re-derive all of it at once.
     */
    public static void relaunchApp(Context ctx) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (intent == null) return;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    /** How many stories at the top of each category are readable without a membership. */
    public static final int FREE_STORIES_PER_CATEGORY = 2;

    /**
     * The per-story membership gate: the first two stories of a category are open,
     * the rest need a membership.
     *
     * Applies in update mode only. The Mixed (1-3 logins) and Censored (4-5) tiers are
     * deliberately left completely unlocked so new users get an unrestricted run at the
     * app, and the Full tier monetises through the per-category locks in ftab1 instead,
     * so a second lock on top of those would be redundant.
     *
     * positionInCategory is 0-based and must be the story's index in the same order
     * the list is built in; both callers derive it that way.
     */
    public static boolean isStoryLocked(int positionInCategory) {
        if (Vip_Member) return false;
        if (!"active".equals(App_updating)) return false;
        return positionInCategory >= FREE_STORIES_PER_CATEGORY;
    }

    /** Persists the remote config so it survives the process being killed. */
    static void cacheRemoteConfig(Context ctx) {
        SharedPreferences.Editor e =
                ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        if (App_updating != null) e.putString("cfg_App_updating", App_updating);
        if (Ads_State != null) e.putString("cfg_Ads_State", Ads_State);
        if (Ad_Network_Name != null) e.putString("cfg_Ad_Network_Name", Ad_Network_Name);
        if (databaseURL != null) e.putString("cfg_databaseURL", databaseURL);
        if (exit_Refer_appNavigation != null)
            e.putString("cfg_exitRefer", exit_Refer_appNavigation);
        if (Refer_App_url2 != null) e.putString("cfg_ReferUrl2", Refer_App_url2);
        e.apply();
    }

    /**
     * Re-derives every piece of gating state from disk.
     *
     * All of these flags are statics that were previously written only by
     * SplashScreen. When Android kills a backgrounded process and later restores
     * the task, the top activity is recreated WITHOUT SplashScreen running, so the
     * statics fell back to their declared defaults: DB_TABLE_NAME became ""
     * (every story query hit a non-existent table and returned an empty list) and
     * Vip_Member became false (a paying member silently saw placeholder content
     * and ads).
     *
     * MyApplication.onCreate() always runs on process start - including that
     * restore path - so this is invoked from there. It deliberately does NOT touch
     * the stored loginTimes counter; only SplashScreen may increment it.
     */
    public static void restoreSessionState(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Login_Times = sp.getInt("loginTimes", 0);

        // Last known remote config. Defaults leave update mode ON, which is the
        // fail-safe direction (placeholder content rather than real content).
        App_updating = sp.getString("cfg_App_updating", App_updating);
        Ads_State = sp.getString("cfg_Ads_State", Ads_State);
        Ad_Network_Name = sp.getString("cfg_Ad_Network_Name", Ad_Network_Name);
        databaseURL = sp.getString("cfg_databaseURL", databaseURL);
        exit_Refer_appNavigation = sp.getString("cfg_exitRefer", exit_Refer_appNavigation);
        Refer_App_url2 = sp.getString("cfg_ReferUrl2", Refer_App_url2);

        Vip_Member = isMembershipActive(sp);
        if (Vip_Member) {
            // Mirrors vipMemberPrivileges(): no ads, skip the login-count staging.
            // App_updating is intentionally left alone - update mode outranks VIP.
            Ads_State = "inactive";
            if (Login_Times < 10) Login_Times = 10;

            // Keep the pre-expiry reminder re-armed on every process start, not
            // just at purchase/restore time - cheap, idempotent (REPLACE policy),
            // and a safety net in case WorkManager's own reboot-rescheduling ever
            // misses this device.
            MembershipReminderScheduler.schedule(ctx, expiryMillis(sp));
        }

        DB_TABLE_NAME = resolveContentTable();
        Log.d(TAG, "restoreSessionState: table=" + DB_TABLE_NAME
                + " login=" + Login_Times + " vip=" + Vip_Member
                + " updateMode=" + App_updating);
    }

    private void vipMemberPrivileges() {
        // NOTE: App_updating is deliberately NOT cleared here. Update mode is an
        // absolute override - while it is active everyone, VIP included, sees the
        // placeholder catalogue with the audio section hidden. VIP still skips the
        // login-count staging and ads.
        Ads_State = "inactive";
        Login_Times = 10;
    }

    private void updateStoriesInDB() {

        int completeDate = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").readLatestStoryDate();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference storiesRef = db.collection("storymodels");
        Log.d("dfdsfasdfsadf", "Im here");

        storiesRef.whereGreaterThan("completeDate", completeDate)
                .orderBy("completeDate", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            Log.d("dfdsfasdfsadf", "onComplete: "+task.getResult().isEmpty());
                            Log.d("dfdsfasdfsadf", "onComplete: "+completeDate);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Access your document data here

                                Map<String, Object> data = document.getData();
                                HashMap<String, String> m_li = Utils.FirebaseObject_TO_HashMap(data);


                                DatabaseHelper insertRecord = new DatabaseHelper(getApplicationContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems");
                                String res = insertRecord.addstories(m_li);
                                Log.d(TAG, "INSERT DATA: " + res);


                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private String encryption(String text) {

        int key = 5;
        char[] chars = text.toCharArray();
        String encryptedText = "";
        String decryptedText = "";

        //Encryption
        for (char c : chars) {
            c += key;
            encryptedText = encryptedText + c;
        }

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return encryptedText;
    }

//    private void readJSON(String Filename, String collectionName) {
//        try {
//            JSONArray array = new JSONArray(loadJSONFromAsset(Filename));
//            ArrayList<String> titlelist = new ArrayList<String>();
//            ArrayList<String> storylist = new ArrayList<String>();
//            ArrayList<String> authorList = new ArrayList<String>();
//            ArrayList<String> dateList = new ArrayList<String>();
//
//            ArrayList<String> data = new ArrayList<String>();
//
//
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject obj = (JSONObject) array.get(i);
//                titlelist.add(obj.getString("title"));
//                authorList.add(obj.getString("author"));
//                dateList.add(obj.getString("date"));
//
//                //Story is a array
//                JSONArray story_array = obj.getJSONArray("story");
//                String paragrapg = "";
//                for (int g = 0; g < story_array.length(); g++) {
//                    paragrapg = paragrapg + "\n" + story_array.get(g).toString() + "\n\r";
//                }
//                storylist.add(paragrapg);
//            }
//
//
//            for (int i = 0; i < titlelist.size(); i++) {
//                if (titlelist.get(i).trim().length() >= 1) {
//                    DatabaseHelper insertRecord = new DatabaseHelper(getApplicationContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, collectionName);
//                    String res = insertRecord.addstories(dateList.get(i) + " by " + authorList.get(i), encryption(storylist.get(i)), titlelist.get(i));
//                    Log.d(TAG, "INSERT DATA: " + res);
//                }
//            }
//        } catch (JSONException e) {
//            Log.d(TAG, "getMessage: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (facebook_IntertitialAds != null) {
            facebook_IntertitialAds.destroy();

        }
    }

    public static String decryption(String encryptedText) {

        int key = 5;

        // Shifts in place over a char[]. The previous version rebuilt the whole
        // string on every character, which is quadratic - a 20k-character story
        // cost ~200 million char copies, and a list page of them blocked the main
        // thread long enough to ANR. Output is byte-for-byte identical.
        char[] chars = encryptedText.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] -= key;
        }
        return new String(chars);
    }

    private void fullscreenMode() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());
        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

}