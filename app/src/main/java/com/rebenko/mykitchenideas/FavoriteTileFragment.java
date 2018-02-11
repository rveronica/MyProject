package com.rebenko.mykitchenideas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FavoriteTileFragment extends Fragment {
    private final static int HOURS_UNTIL_REFRESH = 2;
    //private final static int MINUTES_UNTIL_REFRESH = 2;
    Long dataLoadedTime = 0L;


    static FavoriteTileAdapter adapter;

    public FavoriteTileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);

        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        adapter = new FavoriteTileAdapter(MyDatabaseHelper.FAVORITE_ITEMS);
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(System.currentTimeMillis() >= dataLoadedTime + (HOURS_UNTIL_REFRESH * 60 * 60 * 1000)){
            dataLoadedTime = System.currentTimeMillis();
            MyDatabaseHelper.DBopen(getActivity());
            PostListFragment.adapter.notifyDataSetChanged();
            FavoriteTileFragment.adapter.notifyDataSetChanged();
        }
    }
}