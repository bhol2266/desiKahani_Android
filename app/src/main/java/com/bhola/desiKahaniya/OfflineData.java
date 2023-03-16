package com.bhola.desiKahaniya;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class OfflineData extends Application {

    FirebaseData ref;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
