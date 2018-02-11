package com.rebenko.mykitchenideas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PostListFragment extends Fragment {
    static PostListAdapter adapter;
    static LinearLayoutManager layoutManagerPostList;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);

        layoutManagerPostList = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManagerPostList);
        adapter = new PostListAdapter(MyDatabaseHelper.NEW_ITEMS);
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }
}





