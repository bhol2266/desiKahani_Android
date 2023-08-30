package com.bhola.desiKahaniyaAdult2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "TAGA";
    String DbName;
    String DbPath;
    Context context;
    String Database_tableNo;
    Cursor cursor;

    public DatabaseHelper(@Nullable Context mcontext, String name, int version, String Database_tableNo) {
        super(mcontext, name, null, version);
        this.context = mcontext;
        this.DbName = name;
        this.Database_tableNo = Database_tableNo;
        DbPath = "/data/data/" + "com.bhola.desiKahaniyaAdult2" + "/databases/";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d("TAGA", "oldVersion: " + oldVersion);
        Log.d("TAGA", "newVersion: " + newVersion);


    }

    public void CheckDatabases() {
        try {
            String path = DbPath + DbName;
            SQLiteDatabase.openDatabase(path, null, 0);
//            db_delete();
            //Database file is Copied here
            checkandUpdateLoginTimes_UpdateDatabaseCheck();
        } catch (Exception e) {
            this.getReadableDatabase();
            Log.d("TAGA", "CheckDatabases: " + "First Time Copying " + DbName);
            CopyDatabases();
        }
    }

    public void CopyDatabases() {


        try {
            InputStream mInputStream = context.getAssets().open(DbName);
            String outFilename = DbPath + DbName;
            OutputStream mOutputstream = new FileOutputStream(outFilename);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = mInputStream.read(buffer)) > 0) {
                mOutputstream.write(buffer, 0, length);
            }
            mOutputstream.flush();
            mOutputstream.close();
            mInputStream.close();
            //Database file is Copied here
            checkandUpdateLoginTimes_UpdateDatabaseCheck();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void checkandUpdateLoginTimes_UpdateDatabaseCheck() {

        //       Check for Database Update

        Cursor cursor1 = new DatabaseHelper(context, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "DB_VERSION").read_DB_VERSION();
        while (cursor1.moveToNext()) {
            int DB_VERSION_FROM_DATABASE = cursor1.getInt(1);

            if (DB_VERSION_FROM_DATABASE != SplashScreen.DB_VERSION_INSIDE_TABLE) {
                DatabaseHelper databaseHelper2 = new DatabaseHelper(context, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "DB_VERSION");
                databaseHelper2.db_delete();
            }

        }
        cursor1.close();

    }
    public void db_delete() {

        File file = new File(DbPath + DbName);
        if (file.exists()) {
            file.delete();
            Log.d("TAGA", "db_delete: " + "Database Deleted " + DbName);

        }
        CopyDatabases();
    }

    public void OpenDatabase() {
        String path = DbPath + DbName;
        SQLiteDatabase.openDatabase(path, null, 0);

    }


    public Cursor readsingleRow(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableNo, null, "Title=?", new String[]{encryption(title)}, null, null, null, null);
        return cursor;

    }

    public Cursor readFakeStory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("FakeStory", null, "category=?", new String[]{category}, null, null, null, "10");
        return cursor;

    }

    public int readLatestStoryDate() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("StoryItems", null, null, null, null, null, "completeDate DESC", "1");
        cursor.moveToFirst();
        int completeDate = cursor.getInt(9);
        cursor.close();
        return completeDate;

    }

    public Cursor readalldata() {

        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("select * from StoryItems", null);
        return cursor;

    }

    public Cursor readAudioStories(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        if (category.equals("AdultContent")) {
            //all means full adult contents from StoryItems table

            cursor = db.query(Database_tableNo, null, "audio=?", new String[]{"1"}, null, null, "completeDate DESC", null);
        } else {

            cursor = db.query(Database_tableNo, null, "audio=?", new String[]{"1"}, null, null, null, null);

        }

        return cursor;

    }


    public Cursor readLikedStories() {
        return getWritableDatabase().query(Database_tableNo, null, "like=?", new String[]{String.valueOf(1)}, null, null, "completeDate DESC", null);
    }


    public Cursor readaDataByCategory(String category, int page) {
        page = (page - 1) * 15;
        SQLiteDatabase sQLiteDatabase = getWritableDatabase();
        if (category.equals("Latest Stories"))
            return sQLiteDatabase.query(Database_tableNo, null, null, null, null, null, "completeDate DESC", String.valueOf(page) + ",15");
        return sQLiteDatabase.query(Database_tableNo, null, "category=?", new String[]{category}, null, null, "completeDate DESC", String.valueOf(page) + ",15");
    }


    public String updaterecord(String title, int like_value) {
        SQLiteDatabase sQLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("like", like_value);

        float res = sQLiteDatabase.update(Database_tableNo, contentValues, "Title = ?", new String[]{encryption(title)});
        if (res == -1)
            return "Failed";
        else
            return "Liked";
    }

    public String updateStoryParagraph(String title, String story) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("story", story);

        float res = db.update(Database_tableNo, cv, "Title = ?", new String[]{encryption(title)});
        if (res == -1)
            return "Failed";
        else
            return "Liked";
    }

    public String updateStoryRead(String paramString, int paramInt) {
        SQLiteDatabase sQLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("read", Integer.valueOf(paramInt));
        return (sQLiteDatabase.update(Database_tableNo, contentValues, "Title = ?", new String[]{encryption(paramString)}) == -1.0F) ? "Failed" : "Liked";
    }


    public String addstories(HashMap<String, String> m_li) {

        Log.d(TAG, "readStoryFromJson: "+ encryption(m_li.get("Title")));


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Title", encryption(m_li.get("Title")));
        values.put("href", encryption(m_li.get("href")));
        values.put("date", m_li.get("date"));
        values.put("views", m_li.get("views"));
        values.put("description", m_li.get("description"));
        values.put("audiolink", encryption(m_li.get("audiolink")));
        values.put("category", m_li.get("category"));
        values.put("tags", m_li.get("tags"));
        values.put("relatedStories", m_li.get("relatedStories"));
        values.put("completeDate", Integer.parseInt(m_li.get("completeDate")));
        values.put("like", 0);
        values.put("story", m_li.get("story"));

        if (m_li.get("audiolink").trim().length() != 0) {
            values.put("audio", 1);
        } else {
            values.put("audio", 0);
        }
        values.put("storiesInsideParagraph", m_li.get("storiesInsideParagraph"));

        float res = db.insert(Database_tableNo, null, values);
        if (res == -1)
            return "Failed";
        else
            return "Sucess";

    }

    private String encryption(String text) {

        int key = 5;
        char[] chars = text.toCharArray();
        String encryptedText = "";
        String decryptedText = "";

        //Encryption
        for (char c : chars) {
            c += key;
            encryptedText = encryptedText + c;
        }

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return encryptedText;
    }

    public String updateTitle(String title, String translatedTitle) {

        String col_Title = "Title";
        String col_href = "href";
        String col_story = "story";


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("story", translatedTitle);

        float res = db.update(Database_tableNo, cv, "Title = ?", new String[]{title});
        if (res == -1)
            return "Failed";
        else
            return "Success";
    }

    public Cursor read_DB_VERSION() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableNo, null, null, null, null, null, null, null);
        return cursor;

    }
    public void deleteAllrows() {
        Log.d(TAG, "deleteAllrows: " + Database_tableNo);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Database_tableNo, null, null);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }


}
