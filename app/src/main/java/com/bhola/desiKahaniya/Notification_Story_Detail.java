package com.bhola.desiKahaniya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Notification_Story_Detail extends AppCompatActivity {

    List<Object> collectonData;
    StoryDetails_Adapter adapter2;
    DatabaseReference mref;

    ImageView back, share_ap;
    ProgressBar progressBar2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);


        actionBar();
        progressBar2 = findViewById(R.id.progressBar2);
        mref = FirebaseDatabase.getInstance().getReference().child("Notification");
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectonData = new ArrayList<Object>();


        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {

                    FirebaseData firebaseData = ds.getValue(FirebaseData.class);
                    collectonData.add(firebaseData);
                }

                if (!SplashScreen.Sex_Story.equals("active") && !SplashScreen.Sex_Story_Switch_Open.equals("active")) {
                    collectonData.clear();
                }
                adapter2 = new StoryDetails_Adapter(collectonData, Notification_Story_Detail.this);
                recyclerView.setAdapter(adapter2);
                adapter2.notifyDataSetChanged();
                progressBar2.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void actionBar() {
        TextView title = findViewById(R.id.title_collection);
        title.setText("Notifications");

        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Collection_GridView.class)));
        share_ap = findViewById(R.id.share_app);
        share_ap.setOnClickListener(v -> {
            share_ap.setImageResource(R.drawable.favourite_active);
            startActivity(new Intent(getApplicationContext(), Download_Detail.class));
        });
    }

}
