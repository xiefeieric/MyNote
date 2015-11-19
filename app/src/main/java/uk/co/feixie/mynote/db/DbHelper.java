package uk.co.feixie.mynote.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import uk.co.feixie.mynote.model.Note;

/**
 * Created by Fei on 08/11/2015.
 */
public class DbHelper {

    public static final String TABLE = "note";
    private DB mDB;

    public DbHelper(Context context) {
        mDB = new DB(context);
    }

    public boolean add(Note note) {
        SQLiteDatabase writableDatabase = mDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        //title TEXT, content TEXT, image TEXT, video TEXT, voice TEXT, time TEXT
        values.put("title",note.getTitle());
        values.put("content",note.getContent());
        values.put("image",note.getImagePath());
        values.put("video",note.getVideoPath());
        values.put("voice",note.getVoicePath());
        values.put("time", note.getTime());
        values.put("latitude", note.getLatitude());
        values.put("longitude", note.getLongitude());
        long insertFinish = writableDatabase.insert(TABLE, null, values);
        if (insertFinish!=-1) {
            writableDatabase.close();
            return true;
        } else {
            writableDatabase.close();
            return false;
        }
    }

    public boolean delete(Note note) {
        SQLiteDatabase writableDatabase = mDB.getWritableDatabase();
        int deleteFinish = writableDatabase.delete(TABLE, "_id = ?", new String[]{String.valueOf(note.getId())});
        if (deleteFinish!=0) {
            writableDatabase.close();
            return true;
        } else {
            writableDatabase.close();
            return false;
        }
    }

    public boolean update(Note note) {
        SQLiteDatabase writableDatabase = mDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title",note.getTitle());
        values.put("content",note.getContent());
        values.put("image",note.getImagePath());
        values.put("video",note.getVideoPath());
        values.put("voice",note.getVoicePath());
        values.put("time", note.getTime());
        int updateFinish = writableDatabase.update(TABLE, values, "_id=?", new String[]{String.valueOf(note.getId())});
        if (updateFinish!=0) {
            writableDatabase.close();
            return true;
        } else {
            writableDatabase.close();
            return false;
        }
    }

    public List<Note> queryAll() {
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase readableDatabase = mDB.getReadableDatabase();
        Cursor cursor = readableDatabase.query(TABLE, null, null, null, null, null, null);
        //_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, image TEXT, video TEXT, voice TEXT, time TEXT
        if (cursor!=null) {
            while (cursor.moveToNext()) {
                Note note = new Note();
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                note.setId(id);
                String title = cursor.getString(cursor.getColumnIndex("title"));
                note.setTitle(title);
                String content = cursor.getString(cursor.getColumnIndex("content"));
                note.setContent(content);
                String imagePath = cursor.getString(cursor.getColumnIndex("image"));
                note.setImagePath(imagePath);
                String videoPath = cursor.getString(cursor.getColumnIndex("video"));
                note.setVideoPath(videoPath);
                String voicePath = cursor.getString(cursor.getColumnIndex("voice"));
                note.setVoicePath(voicePath);
                String time = cursor.getString(cursor.getColumnIndex("time"));
                note.setTime(time);
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                note.setLatitude(latitude);
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                note.setLongitude(longitude);

                noteList.add(note);
            }
            cursor.close();
            readableDatabase.close();
            return noteList;
        } else {
            cursor.close();
            readableDatabase.close();
            return null;
        }
    }
}
