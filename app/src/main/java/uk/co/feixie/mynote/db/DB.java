package uk.co.feixie.mynote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Fei on 08/11/2015.
 */
public class DB extends SQLiteOpenHelper{

    public DB(Context context) {
        super(context, "my_note.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE note (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, image TEXT, video TEXT, voice TEXT, time TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
