package com.rebenko.mykitchenideas;

import java.util.ArrayList;
import java.util.List;


class MyFacebookItem {
    long date;
    String name;
    String link;
    String tag;
    String type;
    String source;
    int likes_amount;
    int is_favorite;
    final List<MyFacebookPhoto> photo;

    MyFacebookItem() {
        this.name = "";
        this.tag = "";
        this.photo = new ArrayList<>();
    }

    @Override
    public String toString() {
        return link;
    }
}