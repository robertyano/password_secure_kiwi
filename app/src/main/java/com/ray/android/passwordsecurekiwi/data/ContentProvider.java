package com.ray.android.passwordsecurekiwi.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ray.android.passwordsecurekiwi.R;
import com.ray.android.passwordsecurekiwi.data.AccountLogin.AccountEntry;

public class ContentProvider extends android.content.ContentProvider {
    /**
     * Variable for getting the context
     */
    private Context mContext;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = "My Log - " + ContentProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the accounts table
     */
    private static final int ACCOUNTS = 100;
    /**
     * URI matcher code for the content URI for a single account in the accounts table
     */
    private static final int ACCOUNT_ID = 101;
    /**
     * URI matcher code for the content URI for the pass_codes table
     */
    private static final int PASS_CODES = 102;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The content URI of the form "content://com.ray.etc" will map to the
        // integer code {@link #ACCOUNTS}. This URI is used to provide access to MULTIPLE rows
        // of the accounts table.
        // No wildcard is used in the path.
        sUriMatcher.addURI(AccountLogin.CONTENT_AUTHORITY, AccountLogin.PATH_ACCOUNTS, ACCOUNTS);
        // The content URI of the form "content://com.ray.etc/#" will map to the
        // integer code {@link #ACCOUNT_ID}. This URI is used to provide access to ONE single row.
        // In this case the "#" wild card is used where "#" can be substituted for an integer.
        sUriMatcher.addURI(AccountLogin.CONTENT_AUTHORITY, AccountLogin.PATH_ACCOUNTS + "/#", ACCOUNT_ID);
        // The content URI of the form "content://com.ray.android.passwordsecurekiwi" wil map to the
        // integer code {@link #PASS_CODES}. This URI is used to provide access to multiple rows
        // of the pass_codes table.
        // No wildcard is ued in the path
        sUriMatcher.addURI(AccountLogin.CONTENT_AUTHORITY, AccountLogin.PATH_PASS_CODE, PASS_CODES);
    }

    /**
     * Database helper object
     */
    private AccountDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new AccountDbHelper(getContext());
        mContext = getContext();
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ACCOUNTS:
                // For the ACCOUNTS code, query the accounts table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the accounts table.
                cursor = database.query(AccountLogin.AccountEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PASS_CODES:
                // For the PASS_CODES code, query the pass codes table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the accounts table
                cursor = database.query(AccountEntry.PASS_CODE_TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
            case ACCOUNT_ID:
                // For the ACCOUNT_ID code, extract out the ID from the URI.
                // For an ray URI such as "content://com.ray.android.etc/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = AccountEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the accounts table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(AccountLogin.AccountEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException npe) {
            Log.e(LOG_TAG, "Failed to setNotificationUri on Cursor. NullPointerException: " + npe);
        }
        return cursor;
    }

    /**
     * Insert a account into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACCOUNTS:
                return insertAccount(uri, contentValues);
            case PASS_CODES:
                return insertPassCode(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }



    /**
     * Helper Method:
     * Insert a account into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertAccount(@NonNull Uri uri, @Nullable ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_account_name));
        }
        // Check that the author is not null
        String author = values.getAsString(AccountEntry.COLUMN_USER_NAME);
        if (TextUtils.isEmpty(author)) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_user_name));
        }


        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new account with the given values
        long id = database.insert(AccountLogin.AccountEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for uri " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the account content URI
        try {
            // When passing in null as a parameter for ContentObserver, by default the CursorAdapter
            // object will get notified, and the LoaderCallBack methods will still get triggered.
            // uri: content://com.ray.android.etc
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException npe) {
            Log.e(LOG_TAG, "notifyChange() Failed! " + npe);
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Helper Method:
     * Insert a pass_code into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPassCode(@NonNull Uri uri, @Nullable ContentValues values) {
        // Check that passcode is not null
        String passcode = values.getAsString(AccountLogin.AccountEntry.COLUMN_PASS_CODE);
        if (TextUtils.isEmpty(passcode)) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_pass_code));
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new passcode with the given values
        long id = database.insert(AccountEntry.PASS_CODE_TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for uri " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the account content URI
        try {
            // When passing in null as a parameter for ContentObserver, by default the CursorAdapter
            // object will get notified, and the LoaderCallBack methods will still get triggered.
            // uri: content://com.ray.android.etc/accounts
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException npe) {
            Log.e(LOG_TAG, "notifyChange() Failed! " + npe);
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);

    }





    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACCOUNTS:
                return updateAccount(uri, contentValues, selection, selectionArgs);
            case ACCOUNT_ID:
                // For the ACCOUNT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = AccountLogin.AccountEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateAccount(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update accounts in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more accounts).
     * Return the number of rows that were successfully updated.
     */
    private int updateAccount(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link AccountEntry#COLUMN_ACCOUNT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME)) {
            String name = values.getAsString(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME);
            if (name == null) {
                throw new IllegalArgumentException(mContext.getString(R.string.toast_required_account_name));
            }
        }

        // If the {@link AccountEntry#COLUMN_USER_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(AccountEntry.COLUMN_USER_NAME)) {
            String author = values.getAsString(AccountEntry.COLUMN_USER_NAME);
            if (author == null) {
                throw new IllegalArgumentException(mContext.getString(R.string.toast_required_user_name));
            }
        }




        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(AccountEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (NullPointerException npe){
                Log.e(LOG_TAG, "notifyChange() Failed! " + npe);
            }
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACCOUNTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(AccountLogin.AccountEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ACCOUNT_ID:
                // Delete a single row given by the ID in the URI
                selection = AccountEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(AccountEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // TEST TEST TEST
            case PASS_CODES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(AccountEntry.PASS_CODE_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (NullPointerException npe) {
                Log.e(LOG_TAG, "notifyChange() Failed! " + npe);
            }
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACCOUNTS:
                return AccountLogin.AccountEntry.CONTENT_LIST_TYPE;
            case ACCOUNT_ID:
                return AccountEntry.CONTENT_ITEM_TYPE;
            case PASS_CODES:
                return AccountLogin.AccountEntry.CONTENT_PASSCODE_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
