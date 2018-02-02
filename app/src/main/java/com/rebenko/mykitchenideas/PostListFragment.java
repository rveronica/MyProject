package com.rebenko.mykitchenideas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

        //MyDatabaseHelper.getDBItems();

        adapter = new PostListAdapter(MyDatabaseHelper.ALL_ITEMS);
        recyclerView.setAdapter(adapter);

        //MyDatabaseHelper.loadJSON();

        String dbFile;
        String[] dbNames = this.getActivity().databaseList(); // or ContextWrapper
        for (int i = 0; i < dbNames.length; i++) {
            dbFile = this.getActivity().getDatabasePath(dbNames[i]).toString();
            Log.i("MY", dbFile);
        }


        return recyclerView;
    }
}





