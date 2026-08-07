package com.bhola.desiKahaniya;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * No in-app screen: opens the hosted terms page directly in the browser and
 * finishes immediately, so there is only one copy of the document to keep
 * accurate rather than an in-app mirror that can drift from it.
 */
public class TermsAndConditions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LegalDocRenderer.openUrl(this, getString(R.string.legal_url_terms));
        finish();
    }
}
