package com.rebenko.mykitchenideas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import android.support.design.widget.Snackbar;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.content.ContentValues.TAG;

// посты повторяются

class MyDatabaseHelper extends SQLiteOpenHelper {

    private static MyDatabaseHelper helperInstance;
    private static Context myContext;

    private static final String DB_NAME = "_MY_KITCHEN_105";
    private static final int DB_VERSION = 1;

    private static final String DB_TABLE_POST = "POSTS";
    private static final String DB_TABLE_PHOTO = "PHOTOS";
    private static final int MAX_PAGES = 5;
    private static final int MAX_POSTS = 300;

    private static final int MIN_LIKES = 350;
    private static final int MAX_LIKES = 990;

    private static long LAST_DATA = 0;
    private static Boolean initialLoad = false;

    static List<MyFacebookItem> ALL_ITEMS = new ArrayList<>();
    private static List<MyFacebookItem> DB_ITEMS = new ArrayList<>();
    static List<MyFacebookItem> FAVORITE_ITEMS = new ArrayList<>();

    private static boolean createDb = false;
    private static boolean upgradeDb = false;

    private static boolean test = false;



    static synchronized MyDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (helperInstance == null) {
            helperInstance = new MyDatabaseHelper(context.getApplicationContext());
        }
        return helperInstance;
    }

    static void DBopen(Context context) {
        if (helperInstance == null) MyDatabaseHelper.getInstance(context);
    }

    static void DBclose() {
        if (helperInstance != null) {
            helperInstance.close();
            // DID NOT FIX THE BUG. static variable value remains till the application remains alive
            helperInstance = null;
        }
    }

    private MyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        myContext = context;
    }

    /**
     * Copy packaged database from assets folder to the database created in the
     * application package context.
     *
     * @param db
     *            The target database in the application package context.
     */
    private static void copyDatabaseFromAssets(SQLiteDatabase db) {
        Log.i(TAG, "copyDatabase");
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            // Open db packaged as asset as the input stream
            myInput = myContext.getAssets().open(DB_NAME);

            // Open the db in the application package context:
            myOutput = new FileOutputStream(db.getPath());

            // Transfer db file contents:
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();

            // Set the version of the copied database to the current
            // version:
            SQLiteDatabase copiedDb = myContext.openOrCreateDatabase(
                    DB_NAME, 0, null);
            copiedDb.execSQL("PRAGMA user_version = " + DB_VERSION);
            copiedDb.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(TAG + " Error copying database");
        } finally {
            // Close the streams
            try {
                if (myOutput != null) {
                    myOutput.close();
                }
                if (myInput != null) {
                    myInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error(TAG + " Error closing streams");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE POSTS ("
                    + "POST_DATE INTEGER, "
                    + "POST_MESS TEXT, "
                    + "POST_LINK TEXT, "
                    + "POST_TAG TEXT, "
                    + "POST_TYPE TEXT, "
                    + "POST_SOURCE TEXT, "
                    + "POST_LIKES INTEGER, "
                    + "POST_ISFAV INTEGER);");

            db.execSQL("CREATE TABLE PHOTOS ("
                    + "POST_LINK TEXT, "
                    + "PHOTO_SRC TEXT, "
                    + "PHOTO_DESC TEXT, "
                    + "PHOTO_WIDTH INTEGER, "
                    + "PHOTO_HEIGHT INTEGER);");


        } catch (SQLiteException e) {
            Log.e("CREATE error ", e.toString());
        }

        createDb = true;
       /*  a version with initial DB load
        try {
            jsonToDB(loadJSONFromAsset());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("INFO", e.toString());
        }*/
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeDb = true;
    }

    // for initial db from asset
    /*@Override
    public void onOpen(SQLiteDatabase db) {
        Log.i(TAG, "onOpen db");
        if (createDb) {// The db in the application package
            // context is being created.
            // So copy the contents from the db
            // file packaged in the assets
            // folder:
            createDb = false;
            copyDatabaseFromAssets(db);

        }
        if (upgradeDb) {// The db in the application package
            // context is being upgraded from a lower to a higher version.
            upgradeDb = false;
            // Your db upgrade logic here:
        }
    }*/


    static void deletePost(String fieldValue) {
        try {
            SQLiteDatabase DB = helperInstance.getWritableDatabase();
            String str = "POST_LINK" + " = \"" + fieldValue + "\";";
            DB.delete(DB_TABLE_POST, str, null);
            int delCount = DB.delete(DB_TABLE_PHOTO, str, null);
        } catch (SQLiteException e) {
            Log.e("INFO", "Error deleting DB data " + e.toString());
        }
    }


    // fill  DataBase
    static private void addDBItem(MyFacebookItem actualItem) throws Exception {

        try {
            SQLiteDatabase DB = helperInstance.getWritableDatabase();
            ContentValues postItem = new ContentValues();
            postItem.put("POST_DATE", actualItem.date);
            postItem.put("POST_MESS", actualItem.name);
            postItem.put("POST_LINK", actualItem.link);
            postItem.put("POST_TAG", actualItem.tag);
            postItem.put("POST_TYPE", actualItem.type);
            postItem.put("POST_SOURCE", actualItem.source);
            postItem.put("POST_LIKES", actualItem.likes_amount);
            postItem.put("POST_ISFAV", actualItem.is_favorite);
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

        } catch (SQLiteException e) {
            Log.e("INFO", e.toString());
            throw e;
        }
    }


    static void updateFavorite(MyFacebookItem post) {

        if (post.is_favorite == 0) {
            post.is_favorite = 1;
            post.likes_amount++;

            FAVORITE_ITEMS.add(post);
            FavoriteTileFragment.adapter.notifyItemInserted(FAVORITE_ITEMS.size());
        } else {
            post.is_favorite = 0;
            post.likes_amount--;

            int i = FAVORITE_ITEMS.indexOf(post);
            FAVORITE_ITEMS.remove(i);
            FavoriteTileFragment.adapter.notifyItemRemoved(i);
            FavoriteTileFragment.adapter.notifyItemRangeChanged(i, FavoriteTileFragment.adapter.getItemCount());
        }

        PostListFragment.adapter.notifyItemChanged(ALL_ITEMS.indexOf(post));

        try {
            SQLiteDatabase DB = helperInstance.getWritableDatabase();
            ContentValues postFavorite = new ContentValues();
            postFavorite.put("POST_ISFAV", post.is_favorite);

            DB.update(DB_TABLE_POST, postFavorite, "POST_LINK = ?", new String[]{post.link});


        } catch (SQLiteException e) {
            Log.e("INFO", e.toString());
        }
    }


    // prepare ALL_ITEMS and FAVORITE_ITEMS
    static void getDBItems() {
        // need to clear as static variable remains till the application remains alive
        if (ALL_ITEMS.size() != 0) ALL_ITEMS.clear();
        if (FAVORITE_ITEMS.size() != 0) FAVORITE_ITEMS.clear();

        LAST_DATA = 0;
        int post_amount = MAX_POSTS;

        Cursor cursor_post = null;
        Cursor cursor_photo = null;

        try {
            SQLiteDatabase DB = helperInstance.getWritableDatabase();


            String key;

            cursor_post = DB.query(DB_TABLE_POST,
                    new String[]{"POST_DATE", "POST_MESS", "POST_LINK", "POST_TAG", "POST_TYPE", "POST_SOURCE", "POST_LIKES", "POST_ISFAV"},
                    null, null, null, null, "POST_DATE DESC");

            if (cursor_post.moveToFirst()) {
                LAST_DATA = cursor_post.getInt(0);

                do {
                    MyFacebookItem actual_item = new MyFacebookItem();
                    actual_item.date = cursor_post.getInt(0);
                    actual_item.name = cursor_post.getString(1);
                    actual_item.link = cursor_post.getString(2);
                    actual_item.tag = cursor_post.getString(3);
                    actual_item.type = cursor_post.getString(4);
                    actual_item.source = cursor_post.getString(5);
                    actual_item.likes_amount = cursor_post.getInt(6);
                    actual_item.is_favorite = cursor_post.getInt(7);

                    key = actual_item.link;

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

                    // adding to arrays
                    if (actual_item.is_favorite == 1) {
                        ALL_ITEMS.add(actual_item);
                        FAVORITE_ITEMS.add(actual_item);
                    }
                    else {
                        if (post_amount-- > 0) ALL_ITEMS.add(actual_item);
                        else
                            try {
                                String str = "POST_LINK" + " = \"" + key + "\";";
                                DB.delete(DB_TABLE_POST, str, null);
                                DB.delete(DB_TABLE_PHOTO, str, null);
                            } catch (SQLiteException e) {
                                Log.e("INFO", "Error deleting DB data " + e.toString());
                            }
                    }

                } while (cursor_post.moveToNext());

            }
            if (ALL_ITEMS.size() != 0)
                MainActivity.progressBar_layout.setVisibility(View.INVISIBLE);
        }
        catch (SQLiteException e) {
            Log.e("INFO", "Error reading DB data " + e.toString());
        }
        finally {
            if (cursor_post != null) cursor_post.close();
            if (cursor_photo != null) cursor_photo.close();
        }
    }


    private static void jsonToDB(String actual_json) throws Exception {
        long newData = 0;

        // need to clear as static variable remains till the application remains alive
        if (DB_ITEMS.size() != 0) DB_ITEMS.clear();

        // not show any text if device not use English language
        String language = Locale.getDefault().getDisplayLanguage();
        Boolean languageENGLISH = false;
        if (language.equals("English")) {
            languageENGLISH = true;
        }

        try {
            JSONObject posts = new JSONObject(actual_json).getJSONObject("posts");
            JSONArray posts_detail = posts.getJSONArray("data");

            for (int i = 0; i < posts_detail.length(); i++) {
                JSONObject post = posts_detail.getJSONObject(i);
                MyFacebookItem item = new MyFacebookItem();

                item.date = post.getLong("created_time");

                if (item.date == LAST_DATA) break;

                if (i == 0) newData = item.date;

                // get the post-name for non-English users for Analytics
                if (post.has("message")) {
                    item.name = post.getString("message");
                    //seek for tags
                    String strValues[] = item.name.split("#");
                    item.name = strValues[0];
                    if (strValues.length > 1) item.tag = strValues[1];
                }

                item.link = post.getString("permalink_url");
                item.type = post.getString("type");
                if (post.has("source"))
                    item.source = post.getString("source");

                    // if post has not photos
                  if (!post.has("attachments")) continue;

                JSONObject attach = post.getJSONObject("attachments").getJSONArray("data").getJSONObject(0);

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

                item.is_favorite = 0;

                // delete this row if I want to skip attempt with error
                addDBItem(item);

                DB_ITEMS.add(item);
            }
            if (DB_ITEMS.size() != 0) {
                ALL_ITEMS.addAll(0, DB_ITEMS);

                LAST_DATA = newData;

                // add this row if I want to skip attempt with error
                // for (MyFacebookItem new_item : DB_ITEMS) addDBItem(new_item);

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("INFO", e.toString());
            throw e;
        }
    }

    /*static String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = myContext.getAssets().open("start.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }*/


    static void loadJSON()  {
        final Context context = myContext.getApplicationContext();


        //String jsonUrlInit = "https://graph.facebook.com/1095768017105669?fields=posts.limit(100)%7Battachments%7Bsubattachments%2Cmedia%7D%2Cmessage%2Ccreated_time%2Cpermalink_url%7D&access_token=957520847620436|iy0WgujxxvPUbZ7yN4fKamDfPKI&date_format=U";
        // String jsonUrlNew = "https://graph.facebook.com/1095768017105669?fields=posts.limit(50)%7Battachments%7Bsubattachments%2Cmedia%7D%2Cmessage%2Ccreated_time%2Cpermalink_url%7D&access_token=957520847620436|iy0WgujxxvPUbZ7yN4fKamDfPKI&date_format=U";

       String jsonUrlInit = "https://graph.facebook.com/1664303753835794?fields=posts.limit(10)%7Battachments%7Bsubattachments%2Cmedia%7D%2Cmessage%2Csource%2Ctype%2Ccreated_time%2Cpermalink_url%7D&access_token=957520847620436|iy0WgujxxvPUbZ7yN4fKamDfPKI&date_format=U";
       String jsonUrlNew = "https://graph.facebook.com/1664303753835794?fields=posts.limit(5)%7Battachments%7Bsubattachments%2Cmedia%7D%2Cmessage%2Csource%2Ctype%2Ccreated_time%2Cpermalink_url%7D&access_token=957520847620436|iy0WgujxxvPUbZ7yN4fKamDfPKI&date_format=U";

      //  String jsonUrlNew1 = "https://graph.facebook.com/172711059731825?fields=posts.limit(50)%7Battachments%7Bsubattachments%2Cmedia%7D%2Cmessage%2Csource%2Ctype%2Ccreated_time%2Cpermalink_url%7D&access_token=957520847620436|iy0WgujxxvPUbZ7yN4fKamDfPKI&date_format=U";




        String jsonUrl;


        if (LAST_DATA == 0) {
            jsonUrl = jsonUrlInit;
            initialLoad = true;
        }
        else {
            jsonUrl = jsonUrlNew;
            initialLoad = false;
        }

       // if(test) jsonUrl = jsonUrlNew1;


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
                    MyDatabaseHelper.jsonToDB(response[0]);
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
                PostListFragment.adapter.notifyItemRangeInserted(0, DB_ITEMS.size());
                MainActivity.progressBar_layout.setVisibility(View.INVISIBLE);


                if (!initialLoad) {
                    Snackbar snackbar = Snackbar
                            .make(MainActivity.progressBar_layout, String.valueOf(DB_ITEMS.size()) + " " + "NEW POSTS", Snackbar.LENGTH_LONG)
                            .setAction("↑", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    PostListFragment.layoutManagerPostList.scrollToPosition(0);
                                }
                            });

                    snackbar.show();
                }



                DB_ITEMS.clear();
                //test= true;
                //loadJSON();
            }
        }
    }


}
