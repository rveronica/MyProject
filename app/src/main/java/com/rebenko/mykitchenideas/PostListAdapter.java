package com.rebenko.mykitchenideas;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.Date;
import java.util.List;

class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    private final List<MyFacebookItem> posts;

    PostListAdapter(List<MyFacebookItem> posts) {
        this.posts = posts;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final Context myContext;
        ImageLoader mImageLoader;

        private CardView cardView;
        TextView dateView;
        TextView titleView;
        ImageView cardImageView;
        Button favoriteButton;
        ImageButton shareButton;


        ViewHolder(CardView v) {
            super(v);

            myContext = v.getContext();
            mImageLoader = AppSingleton.getInstance(myContext.getApplicationContext()).getImageLoader();

            cardView = v;

            dateView = (TextView) cardView.findViewById(R.id.info_date);
            titleView = (TextView) cardView.findViewById(R.id.info_text);

            if (!MainActivity.language.equals("English")) {
                titleView.setVisibility(View.GONE);
            }

            cardImageView = (ImageView) cardView.findViewById(R.id.cardImageView);

            favoriteButton = (Button) cardView.findViewById(R.id.favorite_button);
            shareButton = (ImageButton) cardView.findViewById(R.id.share_button);
        }
    }


    @Override
    public PostListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(cv);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final MyFacebookItem actualPost = posts.get(position);

        Date time = new Date(actualPost.date * 1000);
        holder.dateView.setText(DateUtils.getRelativeDateTimeString(holder.myContext, time.getTime(),
                DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));

        holder.titleView.setText(actualPost.name);

        // here is a IndexOutOfBoundsException
        //if (!actualPost.photo.get(0).src.equals(""))
            holder.mImageLoader.get(actualPost.photo.get(0).src, ImageLoader.getImageListener(holder.cardImageView,
                    R.drawable.placeholder, R.drawable.placeholder));


        holder.favoriteButton.setText(String.valueOf(actualPost.likes_amount));

        holder.favoriteButton.setBackgroundResource((actualPost.is_favorite == 1) ?
                R.drawable.ic_favorite_red : R.drawable.ic_favorite_white);


        holder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDatabaseHelper.updateFavorite(actualPost);
            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSend = new Intent(Intent.ACTION_SEND);
                intentSend.setType("text/plain");
                //intentSend.putExtra(Intent.EXTRA_TEXT, actualPost.link);
                intentSend.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.rebenko.mykitchenideas");
                v.getContext().startActivity(Intent.createChooser(intentSend, ""));
            }
        });



        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.displayInterstitial();

                if (actualPost.type.equals("video")) {
                    Intent intent = new Intent(holder.myContext, MyMediaActivity.class);
                    intent.putExtra(MyMediaActivity.VIDEO_POST_POSITION, position);
                    intent.putExtra(MyMediaActivity.I_FAVORITE_VIDEO_POST, false);
                    holder.myContext.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(holder.myContext, DetailActivity.class);
                    intent.putExtra(DetailActivity.POST_POSITION, position);
                    intent.putExtra(DetailActivity.I_FAVORITE_POST, false);
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

//    private void refreshEmptyView() {
//        if (emptyView != null) {
//            emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
//        }
//    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
