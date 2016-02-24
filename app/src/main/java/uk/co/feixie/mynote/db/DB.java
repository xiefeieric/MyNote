package uk.co.feixie.mynote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Fei on 08/11/2015.
 */
public class DB extends SQLiteOpenHelper{

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + "note";
    public static final int DATABASE_VERSION = 2;

    public DB(Context context) {
        super(context, "my_note.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE note (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, image TEXT, video TEXT, voice TEXT, time TEXT, latitude TEXT, longitude TEXT, category TEXT)";
        db.execSQL(sql);

        String sqlCategory = "CREATE TABLE note_category (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)";
        db.execSQL(sqlCategory);

        db.execSQL("INSERT into note_category (\"name\") values (\"All Notes\")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
        System.out.println("onUpgrade");
    }
}
