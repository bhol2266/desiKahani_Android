package com.bhola.desiKahaniya;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private Typeface font;
    public CustomSpinnerAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(getItem(position));

        int fontResourceId = convertView.getContext().getResources().getIdentifier(getItem(position), "font", convertView.getContext().getPackageName());

        // Load the selected font dynamically
        Typeface font = ResourcesCompat.getFont(convertView.getContext(), fontResourceId);
        textView.setTypeface(font);

        textView.setText("हिंदी देसी कहानिया"+"("+getItem(position)+")");

        return convertView;
    }
}
