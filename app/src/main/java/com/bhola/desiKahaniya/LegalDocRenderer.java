package com.bhola.desiKahaniya;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Renders an in-app legal document (privacy policy / terms) into a container by
 * walking a heading array and its matching body array from res/values/legal.xml.
 *
 * Keeping the copy in string arrays means the documents can be edited - or
 * translated - without touching any layout or Java.
 */
final class LegalDocRenderer {

    private LegalDocRenderer() {
    }

    /**
     * @param headingsArrayRes string-array of section headings
     * @param bodiesArrayRes   string-array of section bodies, same length and order
     */
    static void render(Activity activity, int containerId, int headingsArrayRes, int bodiesArrayRes) {
        LinearLayout container = activity.findViewById(containerId);
        if (container == null) return;

        String[] headings = activity.getResources().getStringArray(headingsArrayRes);
        String[] bodies = activity.getResources().getStringArray(bodiesArrayRes);

        // Never render past the shorter array, so a mismatched edit cannot crash.
        int count = Math.min(headings.length, bodies.length);

        LayoutInflater inflater = LayoutInflater.from(activity);
        container.removeAllViews();

        for (int i = 0; i < count; i++) {
            View section = inflater.inflate(R.layout.doc_section, container, false);
            ((TextView) section.findViewById(R.id.docSectionHeading)).setText(headings[i]);
            ((TextView) section.findViewById(R.id.docSectionBody)).setText(bodies[i]);
            container.addView(section);
        }
    }

    /**
     * Points a button at the canonical hosted copy of the document. The in-app
     * text stays the primary source so the document still opens offline; this is
     * the always-current version and the one registered with the store.
     *
     * @param urlStringRes one of legal_url_privacy / legal_url_terms / legal_url_home
     */
    static void wireViewOnline(final Activity activity, int buttonId, final int urlStringRes) {
        View button = activity.findViewById(buttonId);
        if (button == null) return;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(activity, activity.getString(urlStringRes));
            }
        });
    }

    /** Opens a URL in the user's browser, failing gracefully if none is present. */
    static void openUrl(Activity activity, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "No browser app found", Toast.LENGTH_SHORT).show();
        }
    }
}
