package com.bhola.desiKahaniya;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
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


    /**
     * Runs a query that must never crash the app.
     *
     * Live crash reports show two ways these reads blow up on real devices: a
     * SQLiteDiskIOException when the device's storage is failing or full, and
     * "no such table" when the bundled copy did not complete. Neither is
     * recoverable here, and neither is worth killing the screen over - callers
     * all iterate the cursor, so an empty one degrades to "nothing found".
     */
    private Cursor safeQuery(String table, String[] columns, String selection,
                             String[] selectionArgs, String orderBy, String limit) {
        try {
            return getWritableDatabase().query(
                    table, columns, selection, selectionArgs, null, null, orderBy, limit);
        } catch (Exception e) {
            Log.e(TAG, "query on " + table + " failed", e);
            return new MatrixCursor(columns != null ? columns : new String[0]);
        }
    }

    public Cursor readsingleRow(String title) {
        return safeQuery(Database_tableNo, null, "Title=?",
                new String[]{encryption(title)}, null, null);
    }

    /**
     * Whether a story with this title exists in the current table.
     *
     * Roughly 38% of the relatedStories / storiesInsideParagraph entries in the
     * shipped data name a story that is not in the database. Those links used to be
     * drawn anyway and did nothing at all when tapped, because the click handler
     * silently returns on a failed lookup - a dead control on screen, which is
     * exactly what a store reviewer flags as broken.
     *
     * Projection is a single column with LIMIT 1 so this stays cheap enough to call
     * while laying the links out; it never reads the story body.
     */
    public boolean titleExists(String title) {
        if (title == null || title.trim().length() == 0) return false;

        Cursor cursor = null;
        try {
            cursor = this.getWritableDatabase().query(
                    Database_tableNo, new String[]{"Title"}, "Title=?",
                    new String[]{encryption(title.trim())}, null, null, null, "1");
            return cursor.getCount() > 0;
        } catch (Exception e) {
            // If the check itself fails, keep the link: the click handler is still
            // guarded, so the worst case is the old behaviour rather than a crash.
            return true;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public Cursor readLoveStory(String category) {
        return safeQuery("LoveStory", null, "category=?", new String[]{category}, null, "10");
    }

    /**
     * Index of a story within its own category, 0-based, or -1 if it is not in the
     * LoveStory collection at all.
     *
     * The category is looked up from the row rather than taken from the caller: the
     * "category" intent extra is carried by a field that is never assigned, so it
     * arrives null everywhere.
     *
     * The listing query below mirrors readLoveStory() - same table, filter, ordering
     * and limit - so the membership gate can never disagree with the position the user
     * actually sees. If one changes, so must the other.
     */
    public int loveStoryPositionOf(String title) {
        if (title == null) return -1;

        String target = encryption(title);

        Cursor row = safeQuery("LoveStory", new String[]{"category"}, "Title=?",
                new String[]{target}, null, "1");
        String category = row.moveToFirst() ? row.getString(0) : null;
        row.close();
        if (category == null) return -1;

        Cursor list = safeQuery("LoveStory", new String[]{"Title"}, "category=?",
                new String[]{category}, null, "10");
        int position = -1;
        int index = 0;
        while (list.moveToNext()) {
            if (target.equals(list.getString(0))) {
                position = index;
                break;
            }
            index++;
        }
        list.close();
        return position;
    }

    public int readLatestStoryDate() {
        // moveToFirst() can legitimately return false (empty or unreadable table);
        // reading column 9 regardless threw rather than returning "no stories yet".
        Cursor cursor = safeQuery("StoryItems", null, null, null, "completeDate DESC", "1");
        int completeDate = 0;
        try {
            if (cursor.moveToFirst() && cursor.getColumnCount() > 9) {
                completeDate = cursor.getInt(9);
            }
        } catch (Exception e) {
            Log.e(TAG, "latest story date could not be read", e);
        } finally {
            cursor.close();
        }
        return completeDate;
    }

    public Cursor readalldata() {

        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("select * from StoryItems", null);
        return cursor;

    }

    public Cursor readAudioStories(String category) {
        //"AdultContent" means the full adult set from the StoryItems table
        String orderBy = category.equals("AdultContent") ? "completeDate DESC" : null;
        return safeQuery(Database_tableNo, null, "audio=?", new String[]{"1"}, orderBy, null);
    }


    public Cursor readLikedStories() {
        return safeQuery(Database_tableNo, null, "like=?",
                new String[]{String.valueOf(1)}, "completeDate DESC", null);
    }


    public Cursor readaDataByCategory(String category, int page) {
        page = (page - 1) * 15;
        String limit = page + ",15";
        if (category.equals("Latest Stories"))
            return safeQuery(Database_tableNo, null, null, null, "completeDate DESC", limit);
        return safeQuery(Database_tableNo, null, "category=?",
                new String[]{category}, "completeDate DESC", limit);
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
        values.put("description", encryption(m_li.get("description")));
        values.put("audiolink", encryption(m_li.get("audiolink")));
        values.put("category", m_li.get("category"));
        values.put("tags", encryption(m_li.get("tags")));
        values.put("relatedStories", encryption(m_li.get("relatedStories")));
        values.put("completeDate", Integer.parseInt(m_li.get("completeDate")));
        values.put("like", 0);
        values.put("story", encryption(m_li.get("story")));

        if (m_li.get("audiolink").trim().length() != 0) {
            values.put("audio", 1);
        } else {
            values.put("audio", 0);
        }
        values.put("storiesInsideParagraph", encryption(m_li.get("storiesInsideParagraph")));

        float res = db.insert(Database_tableNo, null, values);
        if (res == -1)
            return "Failed";
        else
            return "Sucess";

    }

    private String encryption(String text) {

        int key = 5;

        // Shifts in place. The old version built the string one character at a time
        // (quadratic) and then decrypted it again into a variable it never used.
        // Same output, without either cost.
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] += key;
        }
        return new String(chars);
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
