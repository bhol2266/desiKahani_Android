package com.bhola.desiKahaniya;

import android.app.Activity;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ProductDetails;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import soup.neumorphism.NeumorphCardView;

public class Vip_CustomAdapter extends BaseAdapter {

    VipMembership context;
    List<ProductDetails> productDetailsList;
    List<ProductDetails> mlist_offer;
    LayoutInflater inflater;
    BillingClient billingClient;
    LinearLayout progressBar;
    String offer;


    public Vip_CustomAdapter(VipMembership ctx, List<ProductDetails> productDetailsList, BillingClient billingClient, LinearLayout progressBar, String offer, ArrayList<ProductDetails> mlist_offer) {
        this.context = ctx;
        this.productDetailsList = productDetailsList;
        this.mlist_offer = mlist_offer;
        this.billingClient = billingClient;
        inflater = LayoutInflater.from(ctx);
        this.progressBar = progressBar;
        this.offer = offer;

    }

    @Override
    public int getCount() {

        return productDetailsList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.vip_grid_layout, null);
        TextView title = view.findViewById(R.id.title);
        TextView price = view.findViewById(R.id.price);
        TextView mrp = view.findViewById(R.id.mrp);
        TextView membershipType = view.findViewById(R.id.membershipType);


        ProductDetails productDetails = productDetailsList.get(i);
        if (offer.equals("with offer")) {
            productDetails = mlist_offer.get(i);
        }


        price.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
        if (i == 0) {
            membershipType.setText("limited peroid");
            title.setText("VIP 1 Month");
        } else if (i == 1) {
            membershipType.setText("popular");
            title.setText("VIP 3 Months");

        } else if (i == 2) {
            membershipType.setText("special offer");
            title.setText("VIP 9 Months");

        } else {
            membershipType.setText("most valuable");
            title.setText("VIP Lifetime");

        }

        if (offer.equals("with offer")) {
            ProductDetails productDetails1 = productDetailsList.get(i);
            mrp.setVisibility(View.VISIBLE);
            mrp.setText(productDetails1.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
            mrp.setPaintFlags(mrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }


        NeumorphCardView lauchBilling = view.findViewById(R.id.launchBilling);
        ProductDetails finalProductDetails = productDetails;
        lauchBilling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // An activity reference from which the billing flow will be launched.
                Activity activity = context;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(finalProductDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}
