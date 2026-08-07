package com.bhola.desiKahaniya;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Themed "report this content" dialog, shared by the story reader, the audio
 * player and the navigation drawer.
 *
 * Submissions are written to the Firestore <code>Reports</code> collection so
 * they can be reviewed alongside the other admin data. Nothing personally
 * identifying is attached - only what was reported, why, and enough build
 * information to reproduce a technical fault.
 */
public final class ReportDialog {

    public static final String TYPE_STORY = "story";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_GENERAL = "general";

    private ReportDialog() {
    }

    /**
     * @param contentType one of TYPE_STORY / TYPE_AUDIO / TYPE_GENERAL
     * @param title       title of the reported item, or null for a general report
     * @param reference   href / category / audio id, or null
     */
    public static void show(final Activity activity, final String contentType,
                            final String title, final String reference) {
        if (activity == null || activity.isFinishing()) return;

        // Inflate with the activity so the dialog picks up the app theme.
        View view = LayoutInflater.from(activity).inflate(R.layout.report_dialog, null);

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null) {
            // Let the card's own rounded corners show instead of the default
            // opaque dialog background.
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // The optional details field would otherwise take focus on open and
            // raise the keyboard, pushing the action buttons off screen.
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        TextView subject = view.findViewById(R.id.reportSubject);
        if (TextUtils.isEmpty(title)) {
            subject.setText("General feedback about the app");
        } else {
            subject.setText(title);
        }

        final RadioGroup reasons = view.findViewById(R.id.reportReasonGroup);
        final TextInputEditText details = view.findViewById(R.id.reportDetails);
        MaterialButton cancel = view.findViewById(R.id.reportCancelBtn);
        final MaterialButton submit = view.findViewById(R.id.reportSubmitBtn);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit.setEnabled(false);
                submit.setText("Sending...");

                Map<String, Object> report = new HashMap<>();
                report.put("type", contentType);
                report.put("title", title == null ? "" : title);
                report.put("reference", reference == null ? "" : reference);
                report.put("reason", reasonFor(activity, reasons.getCheckedRadioButtonId()));
                report.put("details",
                        details.getText() == null ? "" : details.getText().toString().trim());
                report.put("appVersion", BuildConfig.VERSION_NAME);
                report.put("appVersionCode", BuildConfig.VERSION_CODE);
                report.put("androidVersion", Build.VERSION.RELEASE);
                report.put("deviceModel", Build.MANUFACTURER + " " + Build.MODEL);
                report.put("contentTable", SplashScreen.DB_TABLE_NAME);
                report.put("vipMember", SplashScreen.Vip_Member);
                report.put("status", "new");
                report.put("createdAt", com.google.firebase.Timestamp.now());

                FirebaseFirestore.getInstance()
                        .collection("Reports")
                        .add(report)
                        .addOnSuccessListener(documentReference -> {
                            if (!activity.isFinishing()) {
                                Toast.makeText(activity,
                                        "Thanks — your report has been sent",
                                        Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (!activity.isFinishing()) {
                                Toast.makeText(activity,
                                        "Could not send report. Check your connection.",
                                        Toast.LENGTH_SHORT).show();
                                submit.setEnabled(true);
                                submit.setText("Submit report");
                            }
                        });
            }
        });

        dialog.show();
    }

    private static String reasonFor(Activity activity, int checkedId) {
        if (checkedId == R.id.reasonCopyright) return "Copyright or ownership issue";
        if (checkedId == R.id.reasonBroken) return "Not loading or broken";
        if (checkedId == R.id.reasonMisleading) return "Incorrect or misleading";
        if (checkedId == R.id.reasonOther) return "Something else";
        return "Inappropriate content";
    }
}
