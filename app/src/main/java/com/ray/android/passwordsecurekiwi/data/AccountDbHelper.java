package com.ray.android.passwordsecurekiwi.data;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccountDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "accounts.db";

    /**
     * If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link AccountDbHelper}
     * @param context of the app
     */
    public AccountDbHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * This is called when the database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the account table
        String SQL_CREATE_ACCOUNTS_TABLE = "CREATE TABLE " + AccountLogin.AccountEntry.TABLE_NAME + " ("
                + AccountLogin.AccountEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME + " TEXT NOT NULL, "
                + AccountLogin.AccountEntry.COLUMN_USER_NAME + " TEXT NOT NULL, "
                + AccountLogin.AccountEntry.COLUMN_ACCOUNT_PASSWORD + " TEXT NOT NULL, "
                + AccountLogin.AccountEntry.COLUMN_ACCOUNT_NOTES + " TEXT NOT NULL);";


               /* + AccountLogin.AccountEntry.COLUMN_ACCOUNT_PRICE + " REAL NOT NULL DEFAULT 0);";     */

        // Added for Login Page TEST TEST TEST
        // Create a String that contains the SQL statement to create the pass_code table
       String SQL_CREATE_PASS_CODE_TABLE = "CREATE TABLE " + AccountLogin.AccountEntry.PASS_CODE_TABLE_NAME + " ("
               + AccountLogin.AccountEntry.PASS_CODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
               + AccountLogin.AccountEntry.COLUMN_PASS_CODE + " INTEGER NOT NULL);";




        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ACCOUNTS_TABLE);
        db.execSQL(SQL_CREATE_PASS_CODE_TABLE);
    }

    // This is called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // The database is at version 1, so there is nothing to be done here


    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Get the total number of accounts in the database to display the count at the top of the list
     */
    public static long getAccountsCount(Context context) {
        SQLiteOpenHelper database = new AccountDbHelper(context);
        SQLiteDatabase db = database.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, AccountLogin.AccountEntry.TABLE_NAME);
        db.close();
        return count;
    }

    /**
     * Get the total number of pass_codes in the database to verify the count
     * // TEST TEST TEST
     */
    public static long getPassCodeCount(Context context) {
        SQLiteOpenHelper database = new AccountDbHelper(context);
        SQLiteDatabase db = database.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, AccountLogin.AccountEntry.PASS_CODE_TABLE_NAME);
        db.close();
        return count;
    }

    /**
     * Get the pin_code in the database to verify login
     */
    public static int getPinCode(Context context) {
        SQLiteOpenHelper database = new AccountDbHelper(context);
        SQLiteDatabase db = database.getReadableDatabase();

        int pin_code;

        Cursor c = db.rawQuery("Select " + AccountLogin.AccountEntry.COLUMN_PASS_CODE + " From " +
                AccountLogin.AccountEntry.PASS_CODE_TABLE_NAME + " Order By " +
                AccountLogin.AccountEntry.COLUMN_PASS_CODE + " ASC;", null);


        if (c.moveToFirst()) {
            pin_code = c.getInt(c.getColumnIndex("pass_code"));
        } else {
            pin_code = 0;
        }


        c.close();
        db.close();

        return pin_code;
    }

    //function used for fetching data for exporting database
    public Cursor raw() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + AccountLogin.AccountEntry.TABLE_NAME, new String[]{});

        return res;
    }


}
