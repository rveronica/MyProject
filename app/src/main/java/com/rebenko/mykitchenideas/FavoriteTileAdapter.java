package com.rebenko.mykitchenideas;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;


class FavoriteTileAdapter extends RecyclerView.Adapter<FavoriteTileAdapter.ViewHolder> {
    private final List<MyFacebookItem> posts;

    FavoriteTileAdapter(List<MyFacebookItem> posts) {
        this.posts = posts;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView tileImageView;
        final ImageLoader mImageLoader;
        View view;
        final Context myContext;

        ViewHolder(View v) {
            super(v);
            view = v;

            myContext = v.getContext();
            mImageLoader = AppSingleton.getInstance(myContext.getApplicationContext()).getImageLoader();

            tileImageView = (ImageView) v.findViewById(R.id.tileImageView);

            int size = MainActivity.DISPLAY_WIDTH / 2;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
            tileImageView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public FavoriteTileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cv = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tile, parent, false);
        return new ViewHolder(cv);
    }


    // developer-android.unlimited-translate.org/training/volley/request.html
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final MyFacebookItem favorite_actualPost = posts.get(position);

        // getImageListener handle onErrorResponse response
        holder.mImageLoader.get(favorite_actualPost.photo.get(0).src, ImageLoader.getImageListener(holder.tileImageView,
                R.drawable.placeholder, R.drawable.placeholder));

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int p = holder.getAdapterPosition();
                MyDatabaseHelper.updateFavorite(posts.get(p));
                return true;
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.displayInterstitial();

                if (favorite_actualPost.type.equals("video")) {
                    Intent intent = new Intent(holder.myContext, MyMediaActivity.class);
                    intent.putExtra(MyMediaActivity.VIDEO_POST_POSITION, position);
                    intent.putExtra(MyMediaActivity.I_FAVORITE_VIDEO_POST, true);
                    holder.myContext.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(holder.myContext, DetailActivity.class);
                    intent.putExtra(DetailActivity.POST_POSITION, position);
                    intent.putExtra(DetailActivity.I_FAVORITE_POST, true);
                    holder.myContext.startActivity(intent);
                }
            }
        });
    }

    // hide progressBar layout
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (posts.size() != 0) MainActivity.progressBar_layout.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}