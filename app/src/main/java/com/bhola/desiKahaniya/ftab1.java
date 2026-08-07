package com.bhola.desiKahaniya;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.google.android.material.card.MaterialCardView;

public class ftab1 extends Fragment {
    Context context = getActivity();
    MaterialCardView collection1, collection2, collection3, collection4, collection5, collection6;
    private String TAG = "TAGA";

    /** Gutter between grid cards, and between the cards and the screen edges. */
    private static final float GRID_GAP_DP = 14f;

    public ftab1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ftab1, container, false);


        gridItems(view);
        return view;
    }


    private void gridItems(View view) {
        // Update mode is an absolute override - it outranks the login-count staging
        // and VIP alike.
        if ("active".equals(SplashScreen.App_updating)) {
            //fake content while upadting app
            String[] Category_List = {"प्रेम कहानी 1", "प्रेम कहानी 2", "प्रेम कहानी 3", "प्रेम कहानी 4", "प्रेम कहानी 5", "प्रेम कहानी 6"};
            createGridItems(Category_List, view, "fake content while upadting app");
        } else {

            if (SplashScreen.Login_Times < 4) {
                //Mixed content
                String[] Category_List = {"प्रेम कहानी 1", "प्रेम कहानी 2", "प्रेम कहानी 3", "देसी कहानी 1", "देसी कहानी 2", "प्रेम कहानी 6"};
                createGridItems(Category_List, view, "Mixed content");

            } else if (SplashScreen.Login_Times < 6) {
                // censored Content
                String[] Category_List = {"देसी कहानी 1", "देसी कहानी 2", "देसी कहानी 3", "देसी कहानी 4", "देसी कहानी 5", "देसी कहानी 6"};
                createGridItems(Category_List, view, "censored Content");

            } else {
                // full Content
                String[] Category_List = {"Latest Stories", "Aunty Sex Story", "Bhabhi Sex", "Desi Kahani", "Family Sex Stories", "First Time Sex", "Gay Sex Stories", "Group Sex Stories", "Indian Sex Stories", "Sali Sex", "Teacher Sex", "Teenage Girl", "XXX Kahani", "अन्तर्वासना", "हिंदी सेक्स स्टोरीज"};
                createGridItems(Category_List, view, "full Content");

            }
        }

        // Derived from the same rules the process-restore path uses, so the two can
        // never disagree about which table to read.
        SplashScreen.DB_TABLE_NAME = SplashScreen.resolveContentTable();
    }

    private void createGridItems(String[] Category_List, View view, String contentType) {


        GridLayout gridLayout = view.findViewById(R.id.gridlayout);
        for (int i = 0; i < Category_List.length; i++) {


            String category = Category_List[i].replace("Sex", "").replace("सेक्स", "").replace("XXX", "");

            View vieww = getLayoutInflater().inflate(R.layout.homepage_griditem, null);
            TextView categoryTextView = vieww.findViewById(R.id.Textview1);
            ImageView auntyImageView = vieww.findViewById(R.id.auntyImageView);
            MaterialCardView cardView = vieww.findViewById(R.id.cardview);
            LinearLayout lockLayout = vieww.findViewById(R.id.lockLayout);
            TextView itemIndexBadge = vieww.findViewById(R.id.itemIndexBadge);
            itemIndexBadge.setText(String.format(Locale.getDefault(), "%02d", i + 1));

            if (contentType.equals("full Content") && !SplashScreen.Vip_Member) {
                if (i != 0) {
                    lockLayout.setVisibility(View.VISIBLE);
                }
            }

            categoryTextView.setText(category);

            switch (i) {
                case 0:
                    auntyImageView.setImageResource(R.drawable.aunty1);
                    break;
                case 1:
                    auntyImageView.setImageResource(R.drawable.aunty2);
                    break;
                case 2:
                    auntyImageView.setImageResource(R.drawable.aunty3);
                    break;
                case 3:
                    auntyImageView.setImageResource(R.drawable.aunty4);
                    break;
                case 4:
                    auntyImageView.setImageResource(R.drawable.aunty5);
                    break;
                case 5:
                    auntyImageView.setImageResource(R.drawable.aunty6);
                    break;
                case 6:
                    auntyImageView.setImageResource(R.drawable.aunty7);
                    break;
                case 7:
                    auntyImageView.setImageResource(R.drawable.aunty8);
                    break;
                case 8:
                    auntyImageView.setImageResource(R.drawable.aunty9);
                    break;
                case 9:
                    auntyImageView.setImageResource(R.drawable.aunty10);
                    break;
                case 10:
                    auntyImageView.setImageResource(R.drawable.aunty11);
                    break;
                case 11:
                    auntyImageView.setImageResource(R.drawable.aunty12);
                    break;
                case 12:
                    auntyImageView.setImageResource(R.drawable.aunty13);
                    break;
                case 13:
                    auntyImageView.setImageResource(R.drawable.aunty14);
                    break;
                case 14:
                    auntyImageView.setImageResource(R.drawable.aunty15);
                    break;
            }


            // Equal gutters: with a half-gap of padding on the GridLayout and a
            // half-gap margin on every card, the outer edges and the centre seam
            // all end up exactly GAP wide.
            //   width = (screen - 3 * GAP) / 2
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int gap = Math.round(GRID_GAP_DP * displayMetrics.density);
            int halfGap = gap / 2;
            int requiredWidth = (displayMetrics.widthPixels - (3 * gap)) / 2;
            int requiredHeight = Math.round(requiredWidth * 1.18f);

            // Must be GridLayout.LayoutParams - using FrameLayout.LayoutParams here
            // silently discarded the margins declared in homepage_griditem.xml.
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = requiredWidth;
            params.height = requiredHeight;
            params.setMargins(halfGap, halfGap, halfGap, halfGap);
            cardView.setLayoutParams(params);

            int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (lockLayout.getVisibility() == View.VISIBLE) {
                        Toast.makeText(view.getContext(), "Become a member to access premium content", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(view.getContext(), VipMembership.class));
                        return;
                    }
                    Intent intent = new Intent(getActivity(), Collection_detail.class);
                    intent.putExtra("category", category);
                    intent.putExtra("href", Category_List[finalI]);
                    startActivity(intent);
                }
            });

            gridLayout.addView(vieww);
        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("GridItems.json");
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

}
