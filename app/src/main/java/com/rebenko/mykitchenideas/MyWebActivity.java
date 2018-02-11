package com.rebenko.mykitchenideas;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;


public class MyWebActivity extends AppCompatActivity{
    private MyWebPlayer mWebView;
    Button video_button_audio;
    Button video_button_favorite;
    Button video_button_share;

    public static final String VIDEO_POST_POSITION = "POST_POSITION";
    MyFacebookItem videoPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);

        int post_position = getIntent().getIntExtra(VIDEO_POST_POSITION, 0);
        videoPost = MyDatabaseHelper.FAVORITE_ITEMS.get(post_position);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        int height = size.x * videoPost.photo.get(0).photo_height / videoPost.photo.get(0).photo_width;
        ImageView videoThumb =  (ImageView)findViewById(R.id.videoThumb1);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        videoThumb.setLayoutParams(layoutParams);
        Context myContext = this.getApplicationContext();
        final ImageLoader mImageLoader = AppSingleton.getInstance(myContext).getImageLoader();
        mImageLoader.get(videoPost.photo.get(0).src, ImageLoader.getImageListener(videoThumb,
                R.drawable.placeholder, R.drawable.placeholder));

        mWebView = (MyWebPlayer) findViewById(R.id.fbVideoPlayer);
        initView();

        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                View progress = findViewById(R.id.video_Progress1);
                progress.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });
    }

    private void initView() {
        mWebView = (MyWebPlayer) findViewById(R.id.fbVideoPlayer);
        video_button_audio = (Button) findViewById(R.id.video_button_audio1);
        video_button_favorite = (Button) findViewById(R.id.video_button_favorite1);
        video_button_share = (Button) findViewById(R.id.video_button_share1);


        initWebView();
        initButton();

        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                View progress = findViewById(R.id.video_Progress1);
                progress.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });
    }

    private void initWebView() {
        mWebView.initialize(videoPost.link);
        mWebView.setAutoPlayerHeight(this);
    }

   private void initButton(){
       video_button_favorite.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                   video_button_favorite.setBackgroundResource(R.drawable.ic_favorite_white);
                   MyDatabaseHelper.updateFavorite(videoPost, true);
                   video_button_favorite.setClickable(false);
           }
       });

       video_button_share.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intentSend = new Intent(Intent.ACTION_SEND);
               intentSend.setType("text/plain");
               intentSend.putExtra(Intent.EXTRA_TEXT, videoPost.link);
               v.getContext().startActivity(Intent.createChooser(intentSend, ""));
           }
       });

       final boolean[] mute = {true};
       video_button_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mute[0]){
                    mWebView.unmute();
                    video_button_audio.setBackgroundResource(R.drawable.ic_volume_unmute);
                }
                else{
                    mWebView.mute();
                    video_button_audio.setBackgroundResource(R.drawable.ic_volume_mute);
                }
                mute[0] = !mute[0];
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        mWebView.pause();
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
}