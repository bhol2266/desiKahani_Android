package com.bhola.desiKahaniya;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * No in-app screen: opens the hosted privacy policy directly in the browser
 * and finishes immediately, so there is only one copy of the document to keep
 * accurate rather than an in-app mirror that can drift from it.
 */
public class PrivacyPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LegalDocRenderer.openUrl(this, getString(R.string.legal_url_privacy));
        finish();
    }
}
