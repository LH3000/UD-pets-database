package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for Pets app
 */

public class PetProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * URI matcher code for te content URI for the pets table
     */
    public static final int PETS = 100;
    /**
     * URI matcher code for te content URI for a single pet the pets table
     */
    public static final int PET_ID = 101;
    /**
     * URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI
     * common to use NO_MATCH here
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // static initializer. This is run the first time anything is called from this class
    static {
        // the content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to multiple rows of the
        // pets table
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // the content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PETS_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    private PetDBHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDBHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // get the db object
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // build cursor to hold the result of the query
        Cursor cursor;

        // figure out if the uri matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // query the db directly with the URI
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // For this case, extract out the ID from the URI.
                // for an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // this will perform a query on the pets table where _id = 3 to return a cursor row
                // that contains that row
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException(
                        "Insertion is not supported for " + uri
                );
        }
    }

    private Uri insertPet(Uri uri, ContentValues contentValues) {
        // check that the name is not null
        String name = contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        // check that gender is not null
        Integer gender = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        // if the weight is provided, check that it's greater than or equal to 0kg
        Integer weight = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // insert the new pet with the given values
        long id = db.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues);
        // if the id is -1, then the insertion failed. log an error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // once know the ID of the new row in the table, return the new URI
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the x  selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // for the pet_id code, extract out the ID from the URI
                // so we know which row to update. Selection will be "_id=?" and
                // selection arguments will be a String array containing the actual ID.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the db with the given content values. Apply the change specified in the
     * selection and selection arguments (which could be 0 or 1 or more pets)
     * return the number of rows that were successfully updated
     *
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            // if the weight is provided, check that it's greater than or equal to 0kg
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }
        // if there are no value to update, don't try to update the db
        if (values.size() == 0) {
            return 0;
        }

        // get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // update the new pet with the given values
        return db.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // delete all rows that match the selection and selection args
                return db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                // delete a single row given by the ID in the URI
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("delete is not support for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }
}
