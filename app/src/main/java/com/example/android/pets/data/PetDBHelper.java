package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by usa19 on 8/11/2017.
 */

public class PetDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = PetDBHelper.class.getSimpleName();

    // name of the database file
    private static final String DATABASE_NAME = "shelter.db";

    // db version, if db schema is changed, you must increment the db version
    private static final int DATABASE_VERSION = 1;

    /**
     * construct a new instance of {@link PetDBHelper}
     *
     * @param context of the app
     */
    public PetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * this is called when the db is created for the first time.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create a string that contains the SQL statement to create the pets table
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetContract.PetEntry.TABLE_NAME + "("
                + PetContract.PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetContract.PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetContract.PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetContract.PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetContract.PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        // execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * This is called when the db needs to be updated
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
