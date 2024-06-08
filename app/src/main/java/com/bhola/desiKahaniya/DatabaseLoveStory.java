package com.bhola.desiKahaniya;

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

public class DatabaseLoveStory extends SQLiteOpenHelper {
    String DbName;
    String DbPath;
    Context context;
    String Database_tableNo;
    Cursor cursor;

//    When Deleting story from  Favourite_list "Database_tableNo" act as "Story Title"

    public DatabaseLoveStory(@Nullable Context mcontext, String DB_NAME, int version, String Table_Number) {
        super(mcontext, DB_NAME, null, version);
        this.context = mcontext;
        this.DbName = DB_NAME;
        this.Database_tableNo = Table_Number;
        DbPath = "/data/data/" + "com.bhola.desiKahaniya" + "/databases/";
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void CheckDatabases() {
        try {
            String path = DbPath + DbName;
            SQLiteDatabase.openDatabase(path, null, 0);
//            db_delete();
            //Database file is Copied here
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
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    public void db_delete() {

        File file = new File(DbPath + DbName);
        if (file.exists()) {
            file.delete();
            Log.d("TAGA", "db_delete: " + "Database Deleted " + DbName);

        }
        CopyDatabases();
    }

    public Cursor readalldata() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableNo, null, null, null, null, null, null, "20");
        return cursor;

    }



    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

}
