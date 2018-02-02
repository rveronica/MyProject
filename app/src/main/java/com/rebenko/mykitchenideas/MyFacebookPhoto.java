package com.rebenko.mykitchenideas;

class MyFacebookPhoto {
    String src;
    int photo_width;
    int photo_height;
    String description;

    MyFacebookPhoto() {
        this.description = "";
    }

    @Override
    public String toString() {
        return src;
    }
}