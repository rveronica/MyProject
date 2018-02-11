package com.rebenko.mykitchenideas;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;

// work fine

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;

public class MyMediaActivity extends AppCompatActivity implements OnPreparedListener,SurfaceHolder.Callback, MediaController.MediaPlayerControl {
    public static final String VIDEO_POST_POSITION = "POST_POSITION";
    public static final String I_FAVORITE_VIDEO_POST = "FAVORITE_POST";
    MyFacebookItem videoPost;

    private MediaPlayer mediaPlayer;
    MediaController controller;
    ImageView videoThumb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media);

        int post_position = getIntent().getIntExtra(VIDEO_POST_POSITION, 0);
        final boolean is_favorite = getIntent().getBooleanExtra(I_FAVORITE_VIDEO_POST, false);
        if (is_favorite) {
            videoPost = MyDatabaseHelper.FAVORITE_ITEMS.get(post_position);
        } else {
            videoPost = MyDatabaseHelper.NEW_ITEMS.get(post_position);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        int height = size.x * videoPost.photo.get(0).photo_height / videoPost.photo.get(0).photo_width;

        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView mPreview = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder holder = mPreview.getHolder();
        holder.setFixedSize(width, height);
        holder.addCallback(this);
        holder.setKeepScreenOn(true);

        mediaPlayer = new MediaPlayer();
        controller = new MediaController(this);

        mediaPlayer.setOnPreparedListener(this);

        videoThumb =  (ImageView)findViewById(R.id.videoThumb);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        videoThumb.setLayoutParams(layoutParams);
        Context myContext = this.getApplicationContext();
        final ImageLoader mImageLoader = AppSingleton.getInstance(myContext).getImageLoader();
        mImageLoader.get(videoPost.photo.get(0).src, ImageLoader.getImageListener(videoThumb,
                R.drawable.placeholder, R.drawable.placeholder));


        final Button video_button_audio = (Button) findViewById(R.id.video_button_audio);
        final Button video_button_favorite = (Button) findViewById(R.id.video_button_favorite);
        final Button video_button_share = (Button) findViewById(R.id.video_button_share);

        video_button_favorite.setBackgroundResource((videoPost.is_favorite == 1) ?
                R.drawable.ic_favorite_red : R.drawable.ic_favorite_white);

        final boolean[] unmute = {true};
        video_button_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unmute[0]) {
                    mediaPlayer.setVolume(0, 0);
                    video_button_audio.setBackgroundResource(R.drawable.ic_volume_mute);
                }
                else {
                    mediaPlayer.setVolume(1, 1);
                    video_button_audio.setBackgroundResource(R.drawable.ic_volume_unmute);
                }
                unmute[0] = !unmute[0];
            }
        });

        video_button_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSend = new Intent(Intent.ACTION_SEND);
                intentSend.setType("text/plain");
                //intentSend.putExtra(Intent.EXTRA_TEXT, actualPost.link);
                intentSend.putExtra(Intent.EXTRA_TEXT, videoPost.link);
                v.getContext().startActivity(Intent.createChooser(intentSend, ""));
            }
        });

        video_button_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                video_button_favorite.setBackgroundResource((videoPost.is_favorite == 0) ?
                        R.drawable.ic_favorite_red : R.drawable.ic_favorite_white);
                MyDatabaseHelper.updateFavorite(videoPost, is_favorite);
                if(is_favorite)video_button_favorite.setClickable(false);
            }
        });

        final RelativeLayout ln = (RelativeLayout) findViewById(R.id.mediaView);
        ln.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {mediaPlayer.pause();}
                else mediaPlayer.start();
                controller.show(3000);
            }
        });

        MobileAds.initialize(this, getString(R.string.ad_banner2_unit_id));
        AdView mAdView = (AdView)findViewById(R.id.adVideoView);
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        View progress = findViewById(R.id.video_Progress);
        progress.setVisibility(View.GONE);
        videoThumb.setVisibility(View.GONE);

        controller.setMediaPlayer(this);
        controller.setAnchorView(this.findViewById(R.id.surfaceView));
        controller.setEnabled(true);
       // controller.show(1000);
        mediaPlayer.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(videoPost.source);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setDisplay(holder);
        mediaPlayer.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (controller.isShowing()) {
            controller.hide();
        } else {
            controller.show(3000);
        }
        return false;
    }

    @Override
    public void start() {mediaPlayer.start();}

    @Override
    public void pause() {
        if (mediaPlayer.isPlaying()) {mediaPlayer.pause();}
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.hide();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
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




    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }
    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }
    @Override
    public void seekTo(int pos) {mediaPlayer.seekTo(pos);}
    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    @Override
    public int getBufferPercentage() {
        return 0;
    }
    @Override
    public boolean canPause() {
        return true;
    }
    @Override
    public boolean canSeekBackward() {
        return true;
    }
    @Override
    public boolean canSeekForward() {
        return true;
    }
    @Override
    public int getAudioSessionId() {
        return 0;
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
}
