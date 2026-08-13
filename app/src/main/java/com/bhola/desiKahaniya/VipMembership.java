package com.bhola.desiKahaniya;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VipMembership extends AppCompatActivity implements DrawsUnderStatusBar {


    AlertDialog dialog;
    private BillingClient billingClient;
    LinearLayout progressBar;
    TextView buyNowTimer, offerTimer;

    private BroadcastReceiver timerUpdateReceiver, timerUpdateReceiverCheck;
    private boolean isTimerRunning = false;
    int backpressCount = 0;
    // Initialised here, not just in createListView(): that runs from the async
    // billing callback, so pressing back before billing responds used to hit a
    // NullPointerException in onBackPressed().
    ArrayList<ProductDetails> mlist_offer = new ArrayList<>();


    PurchasesUpdatedListener purchaseUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                boolean purchaseDone = false;
                for (Purchase purchase : purchases) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                        //first this is triggerd than onResume is called
                        AcknowledgePurchase(purchase);
                        purchaseDone = true;

                    }
                }
                if (!purchaseDone) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(VipMembership.this, "Payment failed! try again", Toast.LENGTH_SHORT).show();

                        }
                    }, 5000);
                }

            } else {
                // Handle any other error codes.
                Toast.makeText(VipMembership.this, "Something went wrong try again!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    private void AcknowledgePurchase(Purchase purchase) {

        //in This first consume is called than Achnowledged is called

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                Log.d("handlePurchase", "Achnowledged Done: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Same mapping the restore path uses, so the two cannot drift.
                        // Guarded: an empty product list would throw here, and this
                        // runs *after* the user has already paid - a crash on this
                        // line would take payment without ever recording membership.
                        String productId = purchase.getProducts().isEmpty()
                                ? null : purchase.getProducts().get(0);
                        int Validity_period = validityDaysFor(productId);

                        savePurchaseDetails_inSharedPreference(purchase.getPurchaseToken(), Validity_period, purchase.getPurchaseTime());



                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(VipMembership.this, SplashScreen.class));
                            }
                        }, 2000);


                    }
                });

            }
        };

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_membership);
        actionBar();
        progressBar = findViewById(R.id.progressBar);
        offerTimer = findViewById(R.id.offerTimer);

        fullscreenMode();



        // While the app is in update mode the benefits list is hidden, which also
        // frees the vertical space the plan cards need.
        View benefitsCard = findViewById(R.id.benefitsCard);
        if (benefitsCard != null && "active".equals(SplashScreen.App_updating)) {
            benefitsCard.setVisibility(View.GONE);
        }

        checkTimeRunning();
        billingfunction();


    }
    private void billingfunction() {

        //Initialize
        billingClient = BillingClient.newBuilder(VipMembership.this)
                .setListener(purchaseUpdatedListener)
                .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                                .enableOneTimeProducts()
                                .build())
                .build();


        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                //start the connection after initializing the billing client
                connectGooglePlayBilling();
            }
        });
    }




    void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (isTimerRunning) {
                        getProductDetails("with offer");
                    } else {
                        getProductDetails("no offer");
                    }
                    // Re-grant membership from Google Play if this device lost its
                    // local record (e.g. after a reinstall or "clear data").
                    restorePurchases();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    private void getProductDetails(String offer) {

        List<String> productIds = new ArrayList<>();
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();

        productIds.add("vip_1");
        productIds.add("vip_3");
        productIds.add("vip_12");
        productIds.add("vip_1_offer");
        productIds.add("vip_3_offer");
        productIds.add("vip_12_offer");

        productIds.add("vip_lifetime_offer");
        productIds.add("vip_lifetime");

// Add more product IDs as needed

        QueryProductDetailsParams.Builder queryBuilder = QueryProductDetailsParams.newBuilder();

        for (String productId : productIds) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.INAPP).build();
            list.add(product);
        }
        queryBuilder.setProductList(list);


        QueryProductDetailsParams queryProductDetailsParams = queryBuilder.build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            public void onProductDetailsResponse(BillingResult billingResult, QueryProductDetailsResult queryProductDetailsResult) {
                // Handle the product details response for multiple products
                List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();

                ((Activity) VipMembership.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(SplashScreen.TAG, "productDetailsList: "+productDetailsList.size());
                        createListView(productDetailsList, offer);
                        ProgressBar coinsItem_progressbar=findViewById(R.id.coinsItem_progressbar);
                        coinsItem_progressbar.setVisibility(View.GONE);

                    }
                });
            }
        });


    }

    private void createListView(List<ProductDetails> productDetailsList, String offer) {

        ArrayList<ProductDetails> mlist = new ArrayList<ProductDetails>();
        mlist_offer = new ArrayList<ProductDetails>();

        for (ProductDetails productDetails : productDetailsList) {
            if (productDetails.getProductId().equals("vip_1")) {
                mlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("vip_1_offer")) {
                mlist_offer.add(productDetails);
            }
        }

        for (ProductDetails productDetails : productDetailsList) {
            if (productDetails.getProductId().equals("vip_3")) {
                mlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("vip_3_offer")) {
                mlist_offer.add(productDetails);
            }
        }

        for (ProductDetails productDetails : productDetailsList) {
            if (productDetails.getProductId().equals("vip_12")) {
                mlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("vip_12_offer")) {
                mlist_offer.add(productDetails);
            }
        }

        for (ProductDetails productDetails : productDetailsList) {
            if (productDetails.getProductId().equals("vip_lifetime")) {
                mlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("vip_lifetime_offer")) {
                mlist_offer.add(productDetails);
            }

        }


        ListView listView = findViewById(R.id.pricelist);
        Vip_CustomAdapter vipMembershipAdapter = new Vip_CustomAdapter(VipMembership.this, mlist, billingClient, progressBar, offer, mlist_offer);
        listView.setAdapter(vipMembershipAdapter);

        vipMembershipAdapter.notifyDataSetChanged();

    }



    private void savePurchaseDetails_inSharedPreference(String purchaseToken, int validity_period, long purchaseTime) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("purchaseToken", purchaseToken);
        myEdit.putInt("validity_period", validity_period);

        // Use Google Play's own purchase time, NOT today's date. This method was
        // handed purchaseTime but ignored it, which meant a restore (or any re-save)
        // silently reset the validity clock and handed out free extra days.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        myEdit.putString("purchase_date", dateFormat.format(new Date(purchaseTime)));

        // The date string above is kept for older installs to read, but it loses
        // the time of day: a purchase at 23:50 used to expire at 00:00 on the
        // final day, hours before the reminder that was scheduled off the exact
        // timestamp. The precise value is stored alongside it and preferred.
        myEdit.putLong("purchase_time_millis", purchaseTime);
        myEdit.commit();

        long expiryMillis = purchaseTime + (validity_period * 86400000L);
        MembershipReminderScheduler.schedule(getApplicationContext(), expiryMillis);
    }

    /**
     * Membership length implied by a product id.
     *
     * Matched longest-first, and lifetime is named rather than assumed. The
     * previous order tested "vip_1" before "vip_12", and since "vip_12"
     * contains "vip_1" every twelve-month purchase was granted 30 days. The
     * final fallback also meant any unrecognised id silently became lifetime.
     */
    private static int validityDaysFor(String productId) {
        if (productId == null) return 0;
        // "Lifetime" runs 18 months, set deliberately.
        if (productId.contains("vip_lifetime")) return 548;
        if (productId.contains("vip_12")) return 365;
        if (productId.contains("vip_3")) return 90;
        if (productId.contains("vip_1")) return 30;
        return 0; // unknown product: grants nothing rather than a decade
    }

    /**
     * Re-grants membership from Google Play.
     *
     * The membership record lives in SharedPreferences, which Android deletes on
     * uninstall - so a paying member who reinstalled lost access even with validity
     * remaining. The purchases themselves survive: they are acknowledged but never
     * consumed, so they stay owned by the Google account permanently and can be
     * queried back. Play also reports the original purchase time, so the remaining
     * validity is restored accurately rather than being reset.
     *
     * Runs whenever billing connects, so simply opening this screen recovers it.
     */
    private void restorePurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK
                            || purchases == null || purchases.isEmpty()) {
                        return;
                    }

                    String bestToken = null;
                    int bestValidity = 0;
                    long bestPurchaseTime = 0L;
                    long bestExpiry = 0L;

                    for (Purchase p : purchases) {
                        if (p.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
                        if (p.getProducts().isEmpty()) continue;

                        // A purchase made on another device may not be acknowledged yet.
                        if (!p.isAcknowledged()) {
                            billingClient.acknowledgePurchase(
                                    AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(p.getPurchaseToken()).build(),
                                    r -> Log.d(SplashScreen.TAG, "restore: acknowledged"));
                        }

                        int days = validityDaysFor(p.getProducts().get(0));
                        long expiry = p.getPurchaseTime() + (days * 86400000L);
                        // Keep whichever owned plan runs longest.
                        if (expiry > bestExpiry) {
                            bestExpiry = expiry;
                            bestToken = p.getPurchaseToken();
                            bestValidity = days;
                            bestPurchaseTime = p.getPurchaseTime();
                        }
                    }

                    if (bestToken == null || bestExpiry <= System.currentTimeMillis()) {
                        return; // nothing owned, or everything already expired
                    }

                    SharedPreferences sp = getSharedPreferences("UserInfo", MODE_PRIVATE);
                    boolean wasMissing = !bestToken.equals(sp.getString("purchaseToken", "not set"));

                    savePurchaseDetails_inSharedPreference(bestToken, bestValidity, bestPurchaseTime);
                    // Re-derive Vip_Member / Login_Times / DB_TABLE_NAME from the
                    // record we just wrote, so the app reflects it immediately.
                    SplashScreen.restoreSessionState(getApplicationContext());

                    if (wasMissing) {
                        // Restart rather than just flipping the flag: the home grid and
                        // tabs were already built for a non-member and would keep showing
                        // locked, censored content until the app was killed by hand.
                        runOnUiThread(() -> {
                            Toast.makeText(VipMembership.this,
                                    "Membership restored", Toast.LENGTH_LONG).show();
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> SplashScreen.relaunchApp(VipMembership.this), 1200);
                        });
                    }
                });
    }




    private void exit_dialog() {

        if (mlist_offer == null || mlist_offer.size() == 0) {
            return;
        }

        getProductDetails("with offer");
        AlertDialog dialog;

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(VipMembership.this);
        LayoutInflater inflater = LayoutInflater.from(VipMembership.this);
        View promptView = inflater.inflate(R.layout.membership_exit_dialog, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        LottieAnimationView lottie = promptView.findViewById(R.id.lottie);
        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lottie.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        buyNowTimer = promptView.findViewById(R.id.buyNowTimer);

        TextView closeDialog = promptView.findViewById(R.id.closeDialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        dialog = builder.create();
        dialog.show();

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        dialog.getWindow().setBackgroundDrawable(inset);


        if (isTimerRunning) {
            backpressCount = 1;
        } else {
            // Timer is not running, start the service
            startOfferTimer();
        }

        ProductDetails productDetails = mlist_offer.get(0);
        LinearLayout clickForPayment = promptView.findViewById(R.id.clickForPayment);
        TextView buyNowTimer = promptView.findViewById(R.id.buyNowTimer);
        TextView price = promptView.findViewById(R.id.price);
        TextView productName = promptView.findViewById(R.id.productName);

        productName.setText(productDetails.getTitle());
        price.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
        buyNowTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        clickForPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });


    }

    private void checkTimeRunning() {
        isTimerRunning = isServiceRunning(TimerService.class);
        Log.d(SplashScreen.TAG, "checkTimeRunning: " + isTimerRunning);
        if (isTimerRunning) {
            backpressCount = 1;
            timerUpdateReceiverCheck = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long remainingTime = intent.getLongExtra("remainingTime", 0);
                    updateTimerTextView(remainingTime);
                }
            };


            IntentFilter filter = new IntentFilter();
            filter.addAction("timer-update");
            filter.addAction("timer-finish");


            // NOT_EXPORTED: only our own TimerService sends these. Exported, any
            // installed app could broadcast "timer-finish" and kill the offer.
            ContextCompat.registerReceiver(this, timerUpdateReceiverCheck, filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED);

        }


    }


    private void startOfferTimer() {


        timerUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long remainingTime = intent.getLongExtra("remainingTime", 0);
                updateTimerTextView(remainingTime);
            }
        };


        IntentFilter filter = new IntentFilter();
        filter.addAction("timer-update");
        filter.addAction("timer-finish");

        // Was also gated on API 26 rather than 33, which was the wrong constant.
        ContextCompat.registerReceiver(this, timerUpdateReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);



        Intent intent = new Intent(this, TimerService.class);
        startService(intent);

    }


    private void updateTimerTextView(long remainingTime) {

        long minutes = (remainingTime / (1000 * 60)) % 60;
        long seconds = (remainingTime / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);

        if (timeLeftFormatted.equals("00:10")) {
            offerTimer.setText("Timer Finished!");
            offerTimer.setVisibility(View.GONE);
            unregisterReceiver(timerUpdateReceiver);
        }

        if (!isTimerRunning) {
            buyNowTimer.setText("BUY NOW " + timeLeftFormatted.toString());
        }
        offerTimer.setText("Offer ends in " + timeLeftFormatted);
        offerTimer.setVisibility(View.VISIBLE);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void actionBar() {
        // Close control in the hero. Routed through onBackPressed() rather than
        // finish() so the exit offer still gets its chance to appear.
        View close = findViewById(R.id.vipCloseBtn);
        if (close != null) {
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // The "User Agreement" and "Privacy Policy" labels under the plan list had
        // no click handlers at all, so they were dead text. Both now open the
        // in-app documents.
        TextView terms = findViewById(R.id.terms);
        if (terms != null) {
            terms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(VipMembership.this, TermsAndConditions.class));
                }
            });
        }

        TextView privacy = findViewById(R.id.privacy);
        if (privacy != null) {
            privacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(VipMembership.this, PrivacyPolicy.class));
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        if (backpressCount == 0 && mlist_offer != null && !mlist_offer.isEmpty()) {
            exit_dialog();
            backpressCount++;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerUpdateReceiver != null) {
            unregisterReceiver(timerUpdateReceiver);
        }
        if (timerUpdateReceiverCheck != null) {
            unregisterReceiver(timerUpdateReceiverCheck);
        }
    }


    private void fullscreenMode() {
        // Clear any fullscreen flags affecting both status bar and navigation bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Ensure the content fits the window
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Create WindowInsetsControllerCompat to manage system bars visibility
        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());

        // Hide only the status bar
        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());

        // Set the behavior for showing system bars transiently by swipe
        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Ensure the navigation bar remains visible
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Set the navigation bar color
//        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.vip_membership_goldcolor));

        // For devices with display cutouts, allow content to layout in cutout areas if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Handle older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Clear any previously set fullscreen flag
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // Hide status bar for older versions
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_FULLSCREEN | // Hide the status bar
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }


}