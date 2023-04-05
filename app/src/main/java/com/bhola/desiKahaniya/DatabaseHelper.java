package com.bhola.desiKahaniya;

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

public class DatabaseHelper extends SQLiteOpenHelper {
    String DbName;
    String DbPath;
    Context context;
    String Database_tableName;
    Cursor cursor;

    public DatabaseHelper(@Nullable Context mcontext, String name, int version, String Database_tableName) {
        super(mcontext, name, null, version);
        this.context = mcontext;
        this.DbName = name;
        this.Database_tableName = Database_tableName;
        DbPath = "/data/data/" + "com.bhola.desiKahaniya" + "/databases/";
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


    public Cursor readLikedStories(String TableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TableName, null, null, null, null, null, null, null);
        return cursor;
    }

    public Cursor readalldata() {
        SQLiteDatabase db = this.getWritableDatabase();
        String columns[] = {"id", "Date", "Title", "Liked"};
        Cursor cursor = db.query(Database_tableName, columns, null, null, null, null, null, null);
        return cursor;

    }

    public Cursor reeead() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableName, null, null, null, null, null, null, null);
        return cursor;

    }

    public Cursor readalldataStory() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableName, null, null, null, null, null, null, null);
        return cursor;

    }


    public String updaterecord(int _id, int like_value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Liked", like_value);

        float res = db.update(Database_tableName, cv, "id=" + _id, null);
        if (res == -1)
            return "Failed";
        else
            return "Liked";

    }


    public Cursor readsingleRow(String Title) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableName, null, "Title=?", new String[]{Title}, null, null, null, null);
        return cursor;

    }


    public String addstories(String Date, String Heading, String Title) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Date", Date);
        values.put("Heading", Heading);
        values.put("Title", Title);
        values.put("Liked", 0);

        float res = db.insert(Database_tableName, null, values);
        if (res == -1)
            return "Failed";
        else
            return "Sucess";

    }

    public String deleteRows() {
        SQLiteDatabase db = this.getWritableDatabase();
        float res = db.delete(Database_tableName, null, null);
        if (res == -1)
            return "Failed";
        else
            return "Deleted all rows";
    }


    public String updateEncryptStory(int _id, String encryptedStory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Heading", encryptedStory);

        float res = db.update(Database_tableName, cv, "id=" + _id, null);
        if (res == -1)
            return "Failed";
        else
            return Database_tableName + " " + _id + ": Sucess";

    }

    public Cursor read_DB_VERSION() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableName, null, null, null, null, null, null, null);
        return cursor;

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }


}
