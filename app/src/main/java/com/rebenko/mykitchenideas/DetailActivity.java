package com.rebenko.mykitchenideas;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;



/**
 * Provides UI for the Detail page with Collapsing Toolbar.
 */
public class DetailActivity extends AppCompatActivity {

    public static final String POST_POSITION = "POST_POSITION";
    public static final String I_FAVORITE_POST = "FAVORITE_POST";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // hide notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclView);

        int post_position = getIntent().getIntExtra(POST_POSITION, 0);
        boolean is_favorite = getIntent().getBooleanExtra(I_FAVORITE_POST, false);

        List<Object> myPhotos = new ArrayList<>();
        MyFacebookItem actualPost;

        // data
        if (is_favorite) {
            for (Object obj : MyDatabaseHelper.FAVORITE_ITEMS.get(post_position).photo)
                myPhotos.add(obj);
            actualPost = MyDatabaseHelper.FAVORITE_ITEMS.get(post_position);
        } else {
            for (Object obj : MyDatabaseHelper.ALL_ITEMS.get(post_position).photo)
                myPhotos.add(obj);
            actualPost = MyDatabaseHelper.ALL_ITEMS.get(post_position);
        }

        // ads
        //probably can throw IndexOutOfBoundsException then ads not loaded

        //Boolean includeAds = (new Random()).nextInt(3)%2 == 0;
        Boolean includeAds = true;
        if (includeAds) {
            final AdView ad_View = new AdView(DetailActivity.this);
            myPhotos.add(ad_View);

            ad_View.setAdSize(AdSize.MEDIUM_RECTANGLE);
            ad_View.setAdUnitId(getString(R.string.ad_banner_unit_id));

            ad_View.loadAd(new AdRequest.Builder().build());
        }

        //reserve this item for bottom navigation
        myPhotos.add(null);

        // initialize Firebase Analytics
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(DetailActivity.this);

        // log Firebase events
        Bundle params = new Bundle();
        params.putString(Param.ITEM_ID, actualPost.name);
        params.putInt(Param.CONTENT_TYPE, actualPost.is_favorite);
        analytics.logEvent(Event.SELECT_CONTENT, params);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DetailAdapter adapter = new DetailAdapter(myPhotos, actualPost, includeAds);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.detail_action_rate:
                Intent intent_action_rate = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.appPlayMarketUrl)));
                startActivity(intent_action_rate);
                return true;

            case R.id.detail_share_app:
                Intent intent_share_app = new Intent(Intent.ACTION_SEND);
                intent_share_app.setType("text/plain");
                intent_share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.appPlayMarketUrl));
                startActivity(Intent.createChooser(intent_share_app, "Share" + getString(R.string.app_name)));
                return true;

            case R.id.detail_more_apps:
                Intent intent_more_apps = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.devPlayMarketUrl)));
                startActivity(intent_more_apps);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

   /* @Override
    public void onStop() {
        super.onStop();

        if (!includeAds) MainActivity.displayInterstitial();
    }*/

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        if (!includeAds) MainActivity.displayInterstitial();
    }*/

}