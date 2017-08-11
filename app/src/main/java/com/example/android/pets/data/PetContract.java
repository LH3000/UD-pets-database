package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by usa19 on 8/11/2017.
 */

public class PetContract {
    //set constructor to private to prevent accidental instantiating
    private PetContract() {
    }

    public static final class PetEntry implements BaseColumns {
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PET_NAME = "name";
        public final static String COLUMN_PET_BREED = "breed";
        public final static String COLUMN_PET_GENDER = "weight";

        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}
