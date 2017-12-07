package com.album.memory.memoryalbum.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by user on 11/18/17.
 */

public class SQLiteManager extends SQLiteOpenHelper {
    private static final String TAG = "SQLITE_MANAGER";
    private static String DATABASE_NAME = "FaceEmotion";
    private static final int DATABASE_VERSION = 15;

    // Table Names
    private static final String TABLE_IMAGES = "images";

    // Post Table Columns
    private static final String KEY_IMAGE_ID = "id";
    private static final String KEY_IMAGE_DATE = "date";
    private static final String KEY_IMAGE_URI_PATH = "uri_path";
    private static final String KEY_IMAGE_FILE_PATH = "file_path";
    private static final String KEY_IMAGE_FACE_DATA = "face_data";
    private static final String KEY_IMAGE_EXIF_LAT = "exif_lat";
    private static final String KEY_IMAGE_EXIF_LNG = "exif_lng";
    private static final String KEY_IMAGE_EXIF_DATE = "exif_date";
    private static final String KEY_IMAGE_EXIF_AREA = "exif_area";
    private static final String KEY_IMAGE_FACE_ANGER = "face_anger";
    private static final String KEY_IMAGE_FACE_FEAR = "face_fear";
    private static final String KEY_IMAGE_FACE_NEUTRAL = "face_neutral";
    private static final String KEY_IMAGE_FACE_HAPPINESS = "face_happiness";
    private static final String KEY_IMAGE_FACE_SURPRISE = "face_surprise";
    private static final String KEY_IMAGE_CATEGORY = "category";

    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_IMAGES +
                "(" + KEY_IMAGE_ID + " INT PRIMARY KEY, " +
                " " + KEY_IMAGE_DATE + " INT NOT NULL, " +
                " " + KEY_IMAGE_URI_PATH + "		TEXT	NOT NULL, " +
                " " + KEY_IMAGE_FILE_PATH + "		TEXT	NOT NULL, " +
                " " + KEY_IMAGE_FACE_DATA + "		TEXT	NOT NULL, " +
                " " + KEY_IMAGE_EXIF_LAT + "		TEXT, " +
                " " + KEY_IMAGE_EXIF_LNG + "		TEXT, " +
                " " + KEY_IMAGE_EXIF_DATE + "		TEXT, " +
                " " + KEY_IMAGE_EXIF_AREA + "		TEXT, " +
                " " + KEY_IMAGE_FACE_ANGER + "		REAL 	NOT NULL, " +
                " " + KEY_IMAGE_FACE_FEAR + "		REAL    NOT NULL, " +
                " " + KEY_IMAGE_FACE_NEUTRAL + "		REAL 	NOT NULL, " +
                " " + KEY_IMAGE_FACE_HAPPINESS + "		REAL 	NOT NULL, " +
                " " + KEY_IMAGE_FACE_SURPRISE + "		REAL 	NOT NULL, " +
                " " + KEY_IMAGE_CATEGORY + "		INT 	NOT NULL " +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        // do whatever is required for the upgrade
        if (newVersion > oldVersion){
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);

            database.execSQL("CREATE TABLE " + TABLE_IMAGES +
                    "(" + KEY_IMAGE_ID + " INT PRIMARY KEY, " +
                    " " + KEY_IMAGE_DATE + " INT NOT NULL, " +
                    " " + KEY_IMAGE_URI_PATH + "		TEXT	NOT NULL, " +
                    " " + KEY_IMAGE_FILE_PATH + "		TEXT	NOT NULL, " +
                    " " + KEY_IMAGE_FACE_DATA + "		TEXT	NOT NULL, " +
                    " " + KEY_IMAGE_EXIF_LAT + "		TEXT, " +
                    " " + KEY_IMAGE_EXIF_LNG + "		TEXT, " +
                    " " + KEY_IMAGE_EXIF_DATE + "		TEXT, " +
                    " " + KEY_IMAGE_EXIF_AREA + "		TEXT, " +
                    " " + KEY_IMAGE_FACE_ANGER + "		REAL 	NOT NULL, " +
                    " " + KEY_IMAGE_FACE_FEAR + "		REAL    NOT NULL, " +
                    " " + KEY_IMAGE_FACE_NEUTRAL + "		REAL 	NOT NULL, " +
                    " " + KEY_IMAGE_FACE_HAPPINESS + "		REAL 	NOT NULL, " +
                    " " + KEY_IMAGE_FACE_SURPRISE + "		REAL 	NOT NULL, " +
                    " " + KEY_IMAGE_CATEGORY + "		INT 	NOT NULL " +
                    ")");
        }
    }

    public void addImage(ImageModel img) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            ContentValues values = new ContentValues();
            values.put(KEY_IMAGE_DATE, img.DATE);
            values.put(KEY_IMAGE_URI_PATH, img.URI_PATH);
            values.put(KEY_IMAGE_FILE_PATH, img.FILE_PATH);
            values.put(KEY_IMAGE_FACE_DATA, img.FACE_DATA);
            values.put(KEY_IMAGE_EXIF_LAT, img.EXIF_LAT);
            values.put(KEY_IMAGE_EXIF_LNG, img.EXIF_LNG);
            values.put(KEY_IMAGE_EXIF_DATE, img.EXIF_DATE);
            values.put(KEY_IMAGE_EXIF_AREA, img.EXIF_AREA);

            values.put(KEY_IMAGE_FACE_ANGER, img.FACE_ANGER);
            values.put(KEY_IMAGE_FACE_FEAR, img.FACE_FEAR);
            values.put(KEY_IMAGE_FACE_NEUTRAL, img.FACE_NEUTRAL);
            values.put(KEY_IMAGE_FACE_HAPPINESS, img.FACE_HAPPINESS);
            values.put(KEY_IMAGE_FACE_SURPRISE, img.FACE_SURPRISE);
            values.put(KEY_IMAGE_CATEGORY, img.CATEGORY);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_IMAGES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add images to database");
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteImage(String uri){
        String IMAGES_DELETE_QUERY = "DELETE FROM " + TABLE_IMAGES + " WHERE " + KEY_IMAGE_URI_PATH + " = \"" + uri + "\"";

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL(IMAGES_DELETE_QUERY);
        return true;
    }

    public boolean imageAlreadyExist(String uri){
        String IMAGES_SELECT_QUERY = "SELECT * FROM " + TABLE_IMAGES + " WHERE " + KEY_IMAGE_URI_PATH + " = \"" + uri + "\"";

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(IMAGES_SELECT_QUERY, null);

        boolean isExist = false;
        try {
            if (cursor.moveToFirst()) {
                isExist = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get images from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return isExist;

    }

    public ArrayList<ImageModel> getAllImages() {
        ArrayList<ImageModel> images = new ArrayList<>();

        // SELECT * FROM IMAGES
        String IMAGES_SELECT_QUERY = "SELECT * FROM " + TABLE_IMAGES;

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(IMAGES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    ImageModel newImg = new ImageModel();
                    newImg.DATE = cursor.getLong(cursor.getColumnIndex(KEY_IMAGE_DATE));
                    newImg.URI_PATH = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URI_PATH));
                    newImg.FILE_PATH = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_FILE_PATH));
                    newImg.FACE_DATA = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_FACE_DATA));
                    newImg.EXIF_LAT = cursor.getDouble(cursor.getColumnIndex(KEY_IMAGE_EXIF_LAT));
                    newImg.EXIF_LNG = cursor.getDouble(cursor.getColumnIndex(KEY_IMAGE_EXIF_LNG));
                    newImg.EXIF_DATE = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_EXIF_DATE));
                    newImg.EXIF_AREA = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_EXIF_AREA));


                    newImg.FACE_ANGER = cursor.getDouble(cursor.getColumnIndex(KEY_IMAGE_FACE_ANGER));
                    newImg.FACE_FEAR = cursor.getDouble(cursor.getColumnIndex(KEY_IMAGE_FACE_FEAR));
                    newImg.FACE_NEUTRAL = cursor.getDouble(cursor.getColumnIndex(KEY_IMAGE_FACE_NEUTRAL));
                    newImg.FACE_HAPPINESS = cursor.getDouble(cursor.getColumnIndex(KEY_IMAGE_FACE_HAPPINESS));
                    newImg.FACE_SURPRISE = cursor.getDouble(cursor.getColumnIndex(KEY_IMAGE_FACE_SURPRISE));

                    newImg.CATEGORY = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CATEGORY));
                    images.add(0, newImg);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get images from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return images;
    }
}
