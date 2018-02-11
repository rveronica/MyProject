package com.rebenko.mykitchenideas;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.List;

import static android.os.Environment.DIRECTORY_PICTURES;


class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_POST_TYPE = 0;
    private static final int ITEM_AD_TYPE = 1;
    private static final int ITEM_BOTTOM_TYPE = 2;
    private final List<Object> postPhotos;
    private MyFacebookItem actualPost;
    private Boolean includeAds;
    private Boolean is_favorite;


    DetailAdapter(List<Object> post, MyFacebookItem actualPost, Boolean includeAds, Boolean is_favorite) {
        this.postPhotos = post;
        this.actualPost = actualPost;
        this.includeAds = includeAds;
        this.is_favorite = is_favorite;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView detailImageView;
        final ImageLoader mImageLoader;
        TextView textView;
        final Context myContext;

        MyViewHolder(View v) {
            super(v);
            myContext = v.getContext().getApplicationContext();
            mImageLoader = AppSingleton.getInstance(myContext).getImageLoader();

            detailImageView = (ImageView) v.findViewById(R.id.detailImageView);
            textView = (TextView) v.findViewById(R.id.detailText);

            if (!MainActivity.language.equals("English")) {
                textView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class NativeExpressAdViewHolder extends RecyclerView.ViewHolder {

        NativeExpressAdViewHolder(View view) {
            super(view);
        }
    }

    private class BannerAdViewHolder extends RecyclerView.ViewHolder {

        BannerAdViewHolder(View view) {
            super(view);
        }
    }

    private class BottomViewHolder extends RecyclerView.ViewHolder {

        Button bottom_favorite;

        BottomViewHolder(View view) {
            super(view);

            bottom_favorite = (Button) view.findViewById(R.id.bottom_favorite);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {

                        case R.id.bottom_save:
                            if (saveToSDCard(actualPost, v.getContext())) {
                                Snackbar.make(v, R.string.saveToSDcard, Snackbar.LENGTH_LONG).show();
                            }
                            break;

                        case R.id.bottom_share:
                            Intent intentSend = new Intent(Intent.ACTION_SEND);
                            intentSend.setType("text/plain");
                            intentSend.putExtra(Intent.EXTRA_TEXT, actualPost.link);
                            v.getContext().startActivity(Intent.createChooser(intentSend, ""));
                            break;
                    }
                }
            };

            view.findViewById(R.id.bottom_save).setOnClickListener(listener);
            view.findViewById(R.id.bottom_share).setOnClickListener(listener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(includeAds) {
            if (position < (getItemCount() - 2)) return ITEM_POST_TYPE;
            else if (position == (getItemCount() - 2)) return ITEM_AD_TYPE;
            else return ITEM_BOTTOM_TYPE;
        }
        else {
            if (position < (getItemCount() - 1)) return ITEM_POST_TYPE;
            else return ITEM_BOTTOM_TYPE;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case ITEM_POST_TYPE:
                View itemPostView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_detail, viewGroup, false);
                return new MyViewHolder(itemPostView);

            case ITEM_BOTTOM_TYPE:
                View itemBottomView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_bottom, viewGroup, false);
                return new BottomViewHolder(itemBottomView);

            case ITEM_AD_TYPE:
            default:
                View itemAdView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_ad_banner_card, viewGroup, false); //changed item_ad
                return new BannerAdViewHolder(itemAdView);

                /*View itemAdView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_ad_card, viewGroup, false); //changed item_ad
                return new NativeExpressAdViewHolder(itemAdView);*/
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ITEM_POST_TYPE:
                MyViewHolder myHolder = (MyViewHolder) holder;

                final MyFacebookPhoto actualPhoto = (MyFacebookPhoto) postPhotos.get(position);
                myHolder.textView.setText(actualPhoto.description);

                int height = MainActivity.DISPLAY_WIDTH * actualPhoto.photo_height / actualPhoto.photo_width;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MainActivity.DISPLAY_WIDTH, height);
                myHolder.detailImageView.setLayoutParams(layoutParams);

                // getImageListener handle onErrorResponse response
                myHolder.mImageLoader.get(actualPhoto.src, ImageLoader.getImageListener(myHolder.detailImageView,
                        R.drawable.placeholder, R.drawable.placeholder));
                break;

            case ITEM_BOTTOM_TYPE:
                final BottomViewHolder myBottomHolder = (BottomViewHolder) holder;
                myBottomHolder.bottom_favorite.setBackgroundResource((actualPost.is_favorite == 1) ?
                        R.drawable.ic_favorite_red : R.drawable.ic_favorite_white);

                myBottomHolder.bottom_favorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setBackgroundResource((actualPost.is_favorite == 0) ?
                                R.drawable.ic_favorite_red : R.drawable.ic_favorite_white);
                        MyDatabaseHelper.updateFavorite(actualPost, is_favorite);
                        if(is_favorite)myBottomHolder.bottom_favorite.setClickable(false);
                    }
                });
                break;

            case ITEM_AD_TYPE:
            default:

                BannerAdViewHolder bannerAdHolder = (BannerAdViewHolder) holder;
                AdView adView = (AdView) postPhotos.get(position);
                ViewGroup adsLayout = (ViewGroup) bannerAdHolder.itemView;

                if (adsLayout.getChildCount() > 0) {
                    adsLayout.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }
                adsLayout.addView(adView);


               /* NativeExpressAdViewHolder nativeExpressHolder =
                        (NativeExpressAdViewHolder) holder;
                NativeExpressAdView adView =
                        (NativeExpressAdView) postPhotos.get(position);
                ViewGroup adsLayout = (ViewGroup) nativeExpressHolder.itemView;

                if (adsLayout.getChildCount() > 0) {
                    adsLayout.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }
                adsLayout.addView(adView);*/
        }
    }

    @Override
    public int getItemCount() {
        return postPhotos.size();
    }

    private static boolean saveToSDCard(MyFacebookItem post, Context context) {

        //for sdk23 and more need Requesting Permissions at Run Time
        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent(context, SaveActivity.class);
            context.startActivity(intent);
            if (!SaveActivity.HAVE_PERMISSION) return false;
        }

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast toast = Toast.makeText(context, R.string.noMediaMounted, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        File direct = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "/100KitchenIdeas");

        Boolean sucsess = true;
        if (!direct.exists()) sucsess = direct.mkdirs();
        if (!sucsess) return false;

        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        for (int i = 0; i < post.photo.size(); i++) {
            Uri downloadUri = Uri.parse(post.photo.get(i).src);
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("")
                    .setDescription("")
                    .setDestinationInExternalPublicDir(DIRECTORY_PICTURES + "/100KitchenIdeas", String.valueOf(post.date) + i + ".jpg");

            mgr.enqueue(request);
        }
        return true;
    }
}
