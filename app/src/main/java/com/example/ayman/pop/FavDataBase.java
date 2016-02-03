package com.example.ayman.pop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Ayman on 23-Dec-15.
 */
public class FavDataBase extends SQLiteOpenHelper {

    public FavDataBase(Context context) {
        super(context, "ayman", null, 1);
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAV_TABLE = "CREATE TABLE Fav ( Id INTEGER PRIMARY KEY ,Title TEXT not null,Year TEXT not null,Vote Text not null,Overview Text not null,Poster Text not null,UNIQUE (id) ON CONFLICT IGNORE )";
        db.execSQL(CREATE_FAV_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS Contacts");
        // Create tables again
        onCreate(db);
    }

    public void addMovie(Movie mov) {
        SQLiteDatabase mydb = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Id", mov.getId());
        values.put("Title", mov.getOriginal_title());
        values.put("Year", mov.getRelease_date());
        values.put("Vote", String.valueOf(mov.getVote_average()));
        values.put("Overview", mov.getOverview());
        values.put("Poster", mov.getOffline_path());

        mydb.insert("Fav", null, values);
        mydb.close();
    }

    public Movie getMovieByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Fav", new String[]{"Title", "Year", "Vote", "Overview", "Poster"},
                "Id=?", new String[]{String.valueOf(id)}
                , null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        Movie mov = new Movie();

        mov.setOriginal_title(cursor.getString(0));
        mov.setRelease_date(cursor.getString(1));
        mov.setVote_average(Double.valueOf(cursor.getString(2)));
        mov.setOverview(cursor.getString(3));
        mov.setOffline_path(cursor.getString(4));

        return mov;
    }


    public ArrayList<Movie> getAllMovies() {
        ArrayList<Movie> movList = new ArrayList<Movie>();

        String selectQuery = "SELECT  * FROM Fav";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Movie mov = new Movie();
                mov.setId(cursor.getInt(0));
                mov.setOriginal_title(cursor.getString(1));
                mov.setRelease_date(cursor.getString(2));
                mov.setVote_average(Double.valueOf(cursor.getString(3)));
                mov.setOverview(cursor.getString(4));
                mov.setOffline_path(cursor.getString(5));

                movList.add(mov);
            } while (cursor.moveToNext());
        }
        db.close();

        return movList;
    }

    public int getMoviesCount() {
        String countQuery = "SELECT  * FROM Fav";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        db.close();
        return cursor.getCount();
    }

    public void deleteMovieByID(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Fav", "Id = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
