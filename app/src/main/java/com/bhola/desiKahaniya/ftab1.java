package com.bhola.desiKahaniya;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ftab1 extends Fragment {
    Context context = getActivity();
    soup.neumorphism.NeumorphCardView collection1, collection2, collection3, collection4, collection5, collection6;

    public ftab1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ftab1, container, false);

        collectionGridItems(view);
        if (SplashScreen.Login_Times > 3 && SplashScreen.Sex_Story.equals("active")) {
            changeTitle_Textview(view);
        }


        return view;
    }

    private void collectionGridItems(View view) {
        collection1 = view.findViewById(R.id.collection1);
        collection1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Collection_detail.class);
                String Collection_DB_Table_Name = "Collection1";
                intent.putExtra("Collection_DB_Table_Name", Collection_DB_Table_Name);
                intent.putExtra("bhola2", "प्रेम कहानी-1");
                startActivity(intent);
            }
        });
        collection2 = view.findViewById(R.id.collection2);
        collection2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Collection_detail.class);
                String Collection_DB_Table_Name = "Collection2";
                intent.putExtra("Collection_DB_Table_Name", Collection_DB_Table_Name);
                intent.putExtra("bhola2", "प्रेम कहानी-2");
                startActivity(intent);
            }
        });
        collection3 = view.findViewById(R.id.collection3);
        collection3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Collection_detail.class);
                String Collection_DB_Table_Name = "Collection3";
                intent.putExtra("Collection_DB_Table_Name", Collection_DB_Table_Name);
                intent.putExtra("bhola2", "हीर रांझा लवस्टोरी");
                startActivity(intent);
            }
        });
        collection4 = view.findViewById(R.id.collection4);
        collection4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Collection_detail.class);
                String Collection_DB_Table_Name = "Collection4";
                intent.putExtra("Collection_DB_Table_Name", Collection_DB_Table_Name);
                intent.putExtra("bhola2", "देसी कहानी-1");
                startActivity(intent);
            }
        });
        collection5 = view.findViewById(R.id.collection5);
        collection5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Collection_detail.class);
                String Collection_DB_Table_Name = "Collection5";
                intent.putExtra("Collection_DB_Table_Name", Collection_DB_Table_Name);
                intent.putExtra("bhola2", "देसी कहानी-2");
                startActivity(intent);
            }
        });
        collection6 = view.findViewById(R.id.collection6);
        collection6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Collection_detail.class);
                String Collection_DB_Table_Name = "Collection6";
                intent.putExtra("Collection_DB_Table_Name", Collection_DB_Table_Name);
                intent.putExtra("bhola2", "प्रेम कहानी-3");
                startActivity(intent);
            }
        });
    }

    private void changeTitle_Textview(View view) {
        TextView Textview1, Textview2, Textview3, Textview4, Textview5, Textview6;

        Textview1 = view.findViewById(R.id.Textview1);
        Textview2 = view.findViewById(R.id.Textview2);
        Textview3 = view.findViewById(R.id.Textview3);
        Textview4 = view.findViewById(R.id.Textview4);
        Textview5 = view.findViewById(R.id.Textview5);
        Textview6 = view.findViewById(R.id.Textview6);
//
        Textview1.setText("देसी कहानी-1");
        Textview2.setText("देसी कहानी-2");
        Textview3.setText("देसी कहानी-3");
        Textview4.setText("देसी कहानी-4");
        Textview5.setText("देसी कहानी-5");
        Textview6.setText("देसी कहानी-6");


    }


}
