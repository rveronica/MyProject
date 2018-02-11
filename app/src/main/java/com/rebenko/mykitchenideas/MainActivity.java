package com.rebenko.mykitchenideas;

// 27.11.17 added load json in MainActivity.onStart() + changes in DatabaseHelper, кликабельные ссылки в detailItem и ввела два размера Json

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    static int DISPLAY_WIDTH;
    static String language;
    static LinearLayout progressBar_layout;
    private static InterstitialAd interstitial;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        DISPLAY_WIDTH = size.x - (int) getResources().getDimension(R.dimen.md_keylines) * 2;

        language = Locale.getDefault().getDisplayLanguage();

        setContentView(R.layout.activity_main);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // hide notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        // display while the list loads
        progressBar_layout = (LinearLayout) findViewById(R.id.progressBar_layout);

        // find TabLayout and associate it with ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        RateApp.app_launched(this);

        if (!language.equals("English")) {

            if (tabLayout.getTabAt(0) != null) //noinspection ConstantConditions
                tabLayout.getTabAt(0).setIcon(R.drawable.tab_new);

            if (tabLayout.getTabAt(1) != null) //noinspection ConstantConditions
                tabLayout.getTabAt(1).setIcon(R.drawable.tab_favorite);
        }

        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.ad_ind_unit_id));
        requestInterstitial();

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {requestInterstitial();}
        });
    }

    private void requestInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public static void displayInterstitial() {
        // 50%
        Boolean showAds = ((new Random()).nextInt(4)+1)%2 == 0;
        if (showAds && interstitial.isLoaded())  interstitial.show();
    }


    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());

        adapter.addFragment(new PostListFragment(), getString(R.string.tab_1));
        adapter.addFragment(new FavoriteTileFragment(), getString(R.string.tab_2));

        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (language.equals("English")) {
                return mFragmentTitleList.get(position);
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_rate:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.appPlayMarketUrl)));
                startActivity(intent);
                return true;

            case R.id.share_app:
                Intent intentSend = new Intent(Intent.ACTION_SEND);
                intentSend.setType("text/plain");
                intentSend.putExtra(Intent.EXTRA_TEXT, getString(R.string.appPlayMarketUrl));
                startActivity(Intent.createChooser(intentSend, "Share " + getString(R.string.app_name)));
                return true;

            case R.id.more_apps:
                Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.devPlayMarketUrl)));
                startActivity(intent2);
                return true;
            //  exit case was deleted - bug with finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean haveInternet() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


    protected void onDestroy() {
        super.onDestroy();
        MyDatabaseHelper.DBclose();
    }


    @Override
    public void onStop() {
        super.onStop();
        AppSingleton.getInstance(this.getApplicationContext()).getRequestQueue().cancelAll(this);
    }
}