package com.rebenko.mykitchenideas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


class MyDatabaseHelper extends SQLiteOpenHelper {

    private static MyDatabaseHelper helperInstance;
    private static Context myContext;

    private static final String DB_NAME = "_MY_KITCHEN_FEBR";
    private static final int DB_VERSION = 1;

    private static final String DB_TABLE_POST = "POSTS";
    private static final String DB_TABLE_PHOTO = "PHOTOS";
    private static final int MAX_PAGES = 5;
    private static final int MAX_FAVORITE_POSTS = 50;
    private static final int MIN_LIKES = 350;
    private static final int MAX_LIKES = 990;

    private static int maxJsonLoad;
    private static long dataDbDate;
    private static boolean initialLoad;
    private static String jsonUrl;
    private static List<MyFacebookItem> DB_ITEMS = new ArrayList<>();
    static List<MyFacebookItem> NEW_ITEMS = new ArrayList<>();
    static List<MyFacebookItem> FAVORITE_ITEMS = new ArrayList<>();

    static synchronized MyDatabaseHelper getInstance(Context context) {
        if (helperInstance == null) {
            helperInstance = new MyDatabaseHelper(context.getApplicationContext());
        }
        return helperInstance;
    }

    static void DBopen(Context context) {
        if (helperInstance == null) MyDatabaseHelper.getInstance(context);
        dataInitiate();
        getDBFavoriteItems();
        loadNewJSON();
    }

     static void dataInitiate() {
         maxJsonLoad = 4;
         dataDbDate = 0;
         initialLoad = true;
         jsonUrl = "https://graph.facebook.com/1095768017105669?fields=posts.limit(25)%7Battachments%7Bsubattachments%2Cmedia%2Curl%7D%2Cmessage%2Csource%2Ctype%2Ccreated_time%7D&access_token=957520847620436|iy0WgujxxvPUbZ7yN4fKamDfPKI&date_format=U";
         DB_ITEMS.clear();
         NEW_ITEMS.clear();
         FAVORITE_ITEMS.clear();
     }

    static void DBclose() {
        if (helperInstance != null) {
            helperInstance.close();
            helperInstance = null;
        }
    }

    private MyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE POSTS ("
                    + "POST_DATE INTEGER, "
                    + "POST_MESS TEXT, "
                    + "POST_LINK TEXT, "
                    + "POST_TYPE TEXT);");

            db.execSQL("CREATE TABLE PHOTOS ("
                    + "POST_LINK TEXT, "
                    + "PHOTO_SRC TEXT, "
                    + "PHOTO_DESC TEXT, "
                    + "PHOTO_WIDTH INTEGER, "
                    + "PHOTO_HEIGHT INTEGER);");
        } catch (SQLiteException e) {
            Log.e("CREATE error ", e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    static void deletePost(MyFacebookItem post) {
        String fieldValue = post.link;
        try {
            SQLiteDatabase DB = helperInstance.getWritableDatabase();
            String str = "POST_LINK" + " = \"" + fieldValue + "\";";
            DB.delete(DB_TABLE_POST, str, null);
            DB.delete(DB_TABLE_PHOTO, str, null);
        } catch (SQLiteException e) {
            Log.e("INFO", "Error deleting DB data " + e.toString());
        }
    }


    // fill  DataBase
    static private void addDBItem(MyFacebookItem actualItem) {

        try {
            SQLiteDatabase DB = helperInstance.getWritableDatabase();
            ContentValues postItem = new ContentValues();
            postItem.put("POST_DATE", actualItem.date);
            postItem.put("POST_MESS", actualItem.name);
            postItem.put("POST_LINK", actualItem.link);
            postItem.put("POST_TYPE", actualItem.type);
            DB.insert(DB_TABLE_POST, null, postItem);

            ContentValues photoItem = new ContentValues();
            for (int i = 0; i < actualItem.photo.size(); i++) {
                photoItem.put("POST_LINK", actualItem.link);
                photoItem.put("PHOTO_SRC", actualItem.photo.get(i).src);
                photoItem.put("PHOTO_DESC", actualItem.photo.get(i).description);
                photoItem.put("PHOTO_WIDTH", actualItem.photo.get(i).photo_width);
                photoItem.put("PHOTO_HEIGHT", actualItem.photo.get(i).photo_height);
                DB.insert(DB_TABLE_PHOTO, null, photoItem);
            }

        } catch (Exception e) {
            Log.e("INFO", e.toString());
            throw e;
        }
    }

    static int findIndex(List<MyFacebookItem> posts, MyFacebookItem item){
        int index;
        for (index = 0; index <= (posts.size() - 1); index++) {
            if (posts.get(index).link.equals(item.link)) return index;
            else {
                int k = 4;
            }
        }
        return -1;
    }

    // если в favorite fragment в detail activity или media activity сначала убрать из избранных, то потом добавить -  была ошибка.
    static void updateFavorite(MyFacebookItem post, boolean favoriteItems) {
        if(favoriteItems){
            int i = findIndex(FAVORITE_ITEMS, post);
            FAVORITE_ITEMS.remove(i);
            FavoriteTileFragment.adapter.notifyItemRemoved(i);
            FavoriteTileFragment.adapter.notifyItemRangeChanged(i, FavoriteTileFragment.adapter.getItemCount());
            deletePost(post);

            int k = findIndex(NEW_ITEMS, post);
            if ( k >= 0) {
                MyFacebookItem postNew = NEW_ITEMS.get(k);
                postNew.is_favorite = 0;
                postNew.likes_amount--;
                PostListFragment.adapter.notifyItemChanged(k);
            }
        }
        else {
            if (post.is_favorite == 0) {
                post.is_favorite = 1;
                post.likes_amount++;

                FAVORITE_ITEMS.add(post);
                addDBItem(post);
                FavoriteTileFragment.adapter.notifyItemInserted(FAVORITE_ITEMS.size());
            } else {
                post.is_favorite = 0;
                post.likes_amount--;

                int i = findIndex(FAVORITE_ITEMS, post);
                FAVORITE_ITEMS.remove(i);
                FavoriteTileFragment.adapter.notifyItemRemoved(i);
                FavoriteTileFragment.adapter.notifyItemRangeChanged(i, FavoriteTileFragment.adapter.getItemCount());
                deletePost(post);
            }
            PostListFragment.adapter.notifyItemChanged(NEW_ITEMS.indexOf(post));
        }
    }

    static void getDBFavoriteItems() {
        int post_amount = 0;
        Cursor cursor_post = null;
        Cursor cursor_photo = null;

        boolean getDate = false;

        try {
            SQLiteDatabase DB = helperInstance.getWritableDatabase();
            String key;

            cursor_post = DB.query(DB_TABLE_POST,
                    new String[]{"POST_DATE", "POST_MESS", "POST_LINK", "POST_TYPE"},
                    null, null, null, null, "POST_DATE DESC");

            if (cursor_post.moveToFirst()) {
                do {
                    MyFacebookItem actual_item = new MyFacebookItem();
                    actual_item.date = cursor_post.getInt(0);
                    actual_item.name = cursor_post.getString(1);
                    actual_item.link = cursor_post.getString(2);
                    actual_item.type = cursor_post.getString(3);
                    actual_item.is_favorite = 1;

                    key = actual_item.link;
                    if (!getDate) {
                        dataDbDate = actual_item.date;
                        getDate = true;
                    }

                    cursor_photo = DB.query(DB_TABLE_PHOTO,
                            new String[]{"PHOTO_SRC", "PHOTO_DESC", "PHOTO_WIDTH", "PHOTO_HEIGHT"},
                            "POST_LINK = ?", new String[]{key}, null, null, null);

                    if (cursor_photo.moveToFirst()) {
                        do {
                            MyFacebookPhoto image = new MyFacebookPhoto();
                            image.src = cursor_photo.getString(0);
                            image.description = cursor_photo.getString(1);
                            image.photo_width = cursor_photo.getInt(2);
                            image.photo_height = cursor_photo.getInt(3);

                            actual_item.photo.add(image);

                        } while (cursor_photo.moveToNext());
                    }
                    if (post_amount++ < MAX_FAVORITE_POSTS) {
                        FAVORITE_ITEMS.add(actual_item);
                    }
                    else {
                        deletePost(actual_item);
                    }
                } while (cursor_post.moveToNext());
            }

        } catch (SQLiteException e) {
            Log.e("INFO", "Error reading DB data " + e.toString());
        } finally {
            if (cursor_post != null) cursor_post.close();
            if (cursor_photo != null) cursor_photo.close();
        }
    }


    private static void jsonToNewItems(String actual_json) throws Exception {
        JSONObject posts;

        String language = Locale.getDefault().getDisplayLanguage();
        Boolean languageENGLISH = false;
        if (language.equals("English")) {
            languageENGLISH = true;
        }
        try {
            if (initialLoad){
                posts = new JSONObject(actual_json).getJSONObject("posts");
            }
            else{
                posts = new JSONObject(actual_json);
            }
            JSONArray posts_detail = posts.getJSONArray("data");

            for (int i = 0; i < posts_detail.length(); i++) {
                JSONObject post = posts_detail.getJSONObject(i);
                MyFacebookItem item = new MyFacebookItem();

                item.date = post.getLong("created_time");
                if (post.has("message")) {
                    item.name = post.getString("message");
                    //seek for tags
                    String strValues[] = item.name.split("#");
                    item.name = strValues[0];
                   // if (strValues.length > 1) item.tag = strValues[1];
                }

                item.type = post.getString("type");
                if (post.has("source"))
                    item.source = post.getString("source");

                    // if post has not photos
                  if (!post.has("attachments")) continue;

                JSONObject attach = post.getJSONObject("attachments").getJSONArray("data").getJSONObject(0);
                item.link = attach.getString("url");

                if (attach.has("subattachments")) {

                    JSONArray subattach = attach.getJSONObject("subattachments").getJSONArray("data");

                    //get photos amount not more then MAX_PAGES value in slide activity
                    for (int k = 0; k < subattach.length() && k < MAX_PAGES; k++) {
                        MyFacebookPhoto image = new MyFacebookPhoto();
                        JSONObject media = subattach.getJSONObject(k);

                        if (media.has("description") & languageENGLISH) {
                            image.description = media.getString("description");
                        }
                        JSONObject imageInfo = media.getJSONObject("media").getJSONObject("image");
                        image.src = imageInfo.getString("src");
                        image.photo_height = imageInfo.getInt("height");
                        image.photo_width = imageInfo.getInt("width");

                        item.photo.add(image);
                    }

                } else {

                    MyFacebookPhoto image = new MyFacebookPhoto();
                    if (attach.has("description") & languageENGLISH) {
                        image.description = attach.getString("description");
                    }
                    JSONObject imageInfo = attach.getJSONObject("media").getJSONObject("image");
                    image.src = imageInfo.getString("src");
                    image.photo_height = imageInfo.getInt("height");
                    image.photo_width = imageInfo.getInt("width");
                    item.photo.add(image);
                }

                Random rand = new Random();
                item.likes_amount = rand.nextInt((MAX_LIKES - MIN_LIKES) + 1) + MIN_LIKES;

                // не работает

                if (FAVORITE_ITEMS.size() > 0 && item.date <= dataDbDate) {
                    int k = findIndex(FAVORITE_ITEMS, item);
                    if (k >= 0) {
                        item.is_favorite = 1;
                        if (item.type.equals("video")) FAVORITE_ITEMS.get(k).source = item.source;
                    } else
                        item.is_favorite = 0;
                }

                DB_ITEMS.add(item);
            }
            if (DB_ITEMS.size() != 0) {
                NEW_ITEMS.addAll(DB_ITEMS);
             }
            if (posts.has("paging")){
                JSONObject page = posts.getJSONObject("paging");
                if(page.has("next"))
                jsonUrl = page.getString("next");
            }
            else {
                jsonUrl = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("INFO", e.toString());
            throw e;
        }
    }

    static void loadNewJSON()  {
        final Context context = myContext.getApplicationContext();
        StringRequest stringRequest = new StringRequest(jsonUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        DownloadJsonTask mt = new DownloadJsonTask();
                        mt.execute(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MainActivity.progressBar_layout.setVisibility(View.INVISIBLE);

                        Toast toast = Toast.makeText(context, R.string.checkInternet, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout toastContainer = (LinearLayout) toast.getView();
                        ImageView catImageView = new ImageView(context);
                        catImageView.setImageResource(R.drawable.cat);
                        toastContainer.addView(catImageView, 0);
                        toast.show();

                        Log.e("INFO", "Error parsing data " + error.toString());
                    }
                });
        AppSingleton.getInstance(context).addToRequestQueue(stringRequest);
    }


    private static class DownloadJsonTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... response) {
                try {
                    MyDatabaseHelper.jsonToNewItems(response[0]);
                } catch (Exception e){
                    e.printStackTrace();
                    Log.e("INFO", e.toString());
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (DB_ITEMS.size() != 0) {
                if(initialLoad) {
                    initialLoad = false;
                    MainActivity.progressBar_layout.setVisibility(View.GONE);
                }
                PostListFragment.adapter.notifyItemRangeInserted(PostListFragment.adapter.getItemCount()-1, DB_ITEMS.size());
                DB_ITEMS.clear();

                if (maxJsonLoad-- != 0)loadNewJSON();
            }
        }
    }


}
