package com.example.status.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.status.item.SubCategoryList;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "status";

    // status table name
    private static final String TABLE_NAME = "download";

    // status Table Columns names
    private static final String ID = "auto_id";
    private static final String KEY_STATUS_ID = "status_id";
    private static final String KEY_CATEGORY_NAME = "category_name";
    private static final String KEY_STATUS_NAME = "status_name";
    private static final String KEY_STATUS_IMAGE_S = "status_image_s";
    private static final String KEY_STATUS_IMAGE_B = "status_image_b";
    private static final String KEY_VIDEO_URI = "video_uri";
    private static final String KEY_GIF_URI = "gif_uri";
    private static final String KEY_STATUS_TYPE = "status_type";
    private static final String KEY_STATUS_TYPE_LAYOUT = "status_type_layout";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_DOWNLOAD_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_STATUS_ID + " TEXT,"
                + KEY_STATUS_NAME + " TEXT," + KEY_CATEGORY_NAME + " TEXT,"
                + KEY_STATUS_IMAGE_S + " TEXT," + KEY_STATUS_IMAGE_B + " TEXT,"
                + KEY_VIDEO_URI + " TEXT," + KEY_GIF_URI + " TEXT,"
                + KEY_STATUS_TYPE + " TEXT," + KEY_STATUS_TYPE_LAYOUT + " TEXT"
                + ")";
        db.execSQL(CREATE_DOWNLOAD_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //-------------------------Status download table-----------------//

    // Adding New Download status
    public void addStatusDownload(SubCategoryList scdList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STATUS_ID, scdList.getId());
        values.put(KEY_STATUS_NAME, scdList.getStatus_title());
        values.put(KEY_CATEGORY_NAME, scdList.getCategory_name());
        values.put(KEY_STATUS_IMAGE_S, scdList.getStatus_thumbnail_s());
        values.put(KEY_STATUS_IMAGE_B, scdList.getStatus_thumbnail_b());
        values.put(KEY_VIDEO_URI, scdList.getVideo_url());
        values.put(KEY_GIF_URI, scdList.getGif_url());
        values.put(KEY_STATUS_TYPE, scdList.getStatus_type());
        values.put(KEY_STATUS_TYPE_LAYOUT, scdList.getStatus_layout());


        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }


    // Getting Download status
    public List<SubCategoryList> getStatusDownload() {
        List<SubCategoryList> scdLists = new ArrayList<SubCategoryList>();

        String selectQuery;
        selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + ID + " DESC ";

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SubCategoryList list = new SubCategoryList();
                list.setId(cursor.getString(1));
                list.setStatus_title(cursor.getString(2));
                list.setCategory_name(cursor.getString(3));
                list.setStatus_thumbnail_s(cursor.getString(4));
                list.setStatus_thumbnail_b(cursor.getString(5));
                list.setVideo_url(cursor.getString(6));
                list.setGif_url(cursor.getString(7));
                list.setStatus_type(cursor.getString(8));
                list.setStatus_layout(cursor.getString(9));

                // Adding status to list
                scdLists.add(list);
            } while (cursor.moveToNext());
        }

        // return status list
        return scdLists;
    }

    //check status download or not
    public boolean checkIdStatusDownload(String id, String type) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_STATUS_ID + "=" + id + " AND " + KEY_STATUS_TYPE + " = " + "'" + type + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount() == 0;
    }

    // Deleting download status
    public boolean deleteStatusDownload(String id, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, KEY_STATUS_ID + "=" + id + " AND " + KEY_STATUS_TYPE + " = " + "'" + type + "'", null) > 0;
    }

    //-------------------------Status download table-----------------//

}
