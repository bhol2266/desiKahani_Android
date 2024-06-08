package com.bhola.desiKahaniya;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class admin_panel extends AppCompatActivity {
    public static int counter = 0;

    DatabaseReference mref, notificationMref;  TextView Users_Counters;
    Button   Refer_App_url_BTN, databaseBtn;
    Switch switch_Exit_Nav, switch_Activate_Ads, switch_App_Updating;
    Button Ad_Network;
    static String uncensored_title = "";
    FirebaseFirestore firestore;
    TextView totalInstallsAllTime, totalInstallsToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpanel);


        initViews();
        appControl();
        Ad_Network_Selection();
        deleteNotification_Stories();
//        totalInstallsAlltime();

    }


    private void initViews() {

        mref = FirebaseDatabase.getInstance().getReference().child("Sexy_Desi_Kahani");
        notificationMref = FirebaseDatabase.getInstance().getReference();
        Ad_Network = findViewById(R.id.Ad_Network);
        switch_Activate_Ads = findViewById(R.id.Activate_Ads);
        switch_App_Updating = findViewById(R.id.App_updating_Switch);
        switch_Exit_Nav = findViewById(R.id.switch_Exit_Nav);
        Refer_App_url_BTN = findViewById(R.id.Refer_App_url_BTN);
        databaseBtn = findViewById(R.id.databaseBtn);


        firestore = FirebaseFirestore.getInstance();
        totalInstallsAllTime = findViewById(R.id.totalInstallsAllTime);
        totalInstallsToday = findViewById(R.id.totalInstallsToday);




    }

    private void totalInstallsAlltime() {
        firestore.collection("Devices").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> keywords = new ArrayList<>();
                    int totalInstallsAlltimeCount = 0;
                    int totalInstallsTodaycount = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        int date_fromDb = 0;
                        int month_fromDb = 0;
                        totalInstallsAlltimeCount = totalInstallsAlltimeCount + 1;
                        Timestamp timestamp = (Timestamp) document.getData().get("Date");
                        Date date = new Date(String.valueOf(timestamp.toDate()));
                        date_fromDb = date.getDate();
                        month_fromDb = date.getMonth() + 1;

                        Date todaDate = new java.util.Date();
                        int currentDate= todaDate.getDate() ;
                        int currentMonth= todaDate.getMonth() + 1;

                        if(date_fromDb==currentDate && month_fromDb==currentMonth){
                            totalInstallsTodaycount = totalInstallsTodaycount + 1;
                        }

                    }
                    totalInstallsAllTime.setText("Total Installs All Time:    " + totalInstallsAlltimeCount);
                    totalInstallsToday.setText("Total Installs Today:       "+totalInstallsTodaycount);
                } else {
                    Log.d(SplashScreen.TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void Ad_Network_Selection() {


        Ad_Network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Ad_Network.getText().toString().equals("admob")) {
                    mref.child("Ad_Network").setValue("facebook");
                    Ad_Network.setBackgroundColor(Color.parseColor("#D11A1A"));

                } else {
                    mref.child("Ad_Network").setValue("admob");
                    Ad_Network.setBackgroundColor(Color.parseColor("#4267B2"));
                }


            }
        });


    }




    private void deleteNotification_Stories() {

        Button deleteNotificationStories;
        deleteNotificationStories = findViewById(R.id.deleteNotificationStories);
        deleteNotificationStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notificationMref.child("Notification").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String keys = ds.getKey();
                            notificationMref.child("Notification").child(keys).removeValue();
                            Toast.makeText(admin_panel.this, "Deleted all Stories", Toast.LENGTH_SHORT).show();

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                });

            }
        });


    }


    private void appControl() {
        checkButtonState();
        EditText Refer_App_url2;

        Refer_App_url2 = findViewById(R.id.Refer_App_url2);
        Refer_App_url_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Refer_App_url2.length() > 2) {
                    mref.child("Refer_App_url2").setValue(Refer_App_url2.getText().toString());
                    Toast.makeText(admin_panel.this, "Refer_App_url2 ADDED", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(admin_panel.this, "Field is Empty", Toast.LENGTH_SHORT).show();
            }

        });


        EditText  databaseEdittext = findViewById(R.id.databaseEdittext);
        databaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (databaseEdittext.length() > 2) {
                    mref.child("databaseURL").setValue(databaseEdittext.getText().toString());
                    Toast.makeText(admin_panel.this, "Databaseurl ADDED", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(admin_panel.this, "Field is Empty", Toast.LENGTH_SHORT).show();
            }

        });

        switch_Exit_Nav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    mref.child("switch_Exit_Nav").setValue("active");
                } else {
                    mref.child("switch_Exit_Nav").setValue("inactive");
                }

            }
        });

        switch_Activate_Ads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mref.child("Ads").setValue("active");

                } else {
                    mref.child("Ads").setValue("inactive");
                }

            }
        });

        switch_App_Updating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mref.child("updatingApp_on_PLatStore").setValue("active");
                    mref.child("Send_Notification").setValue("inactive");

                } else {
                    mref.child("updatingApp_on_PLatStore").setValue("inactive");
                    mref.child("Send_Notification").setValue("active");

                }

            }
        });

    }


    private void checkButtonState() {

        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String match = (String) snapshot.child("switch_Exit_Nav").getValue().toString().trim();

                if (match.equals("active")) {
                    switch_Exit_Nav.setChecked(true);

                } else {

                    switch_Exit_Nav.setChecked(false);
                }

                String Ads = (String) snapshot.child("Ads").getValue().toString().trim();

                if (Ads.equals("active")) {
                    switch_Activate_Ads.setChecked(true);

                } else {
                    switch_Activate_Ads.setChecked(false);
                }

                if (snapshot.child("updatingApp_on_PLatStore").getValue().toString().trim().equals("active")) {
                    switch_App_Updating.setChecked(true);
                } else {
                    switch_App_Updating.setChecked(false);
                }

                String Ad_Network_name = (String) snapshot.child("Ad_Network").getValue().toString().trim();

                Ad_Network.setText(Ad_Network_name);
                if (snapshot.child("Ad_Network").getValue().toString().trim().equals("admob")) {
                    Ad_Network.setBackgroundColor(Color.parseColor("#D11A1A"));
                } else {
                    Ad_Network.setBackgroundColor(Color.parseColor("#4267B2"));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String decryption(String encryptedText) {

        int key = 5;
        String decryptedText = "";

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return decryptedText;
    }


}