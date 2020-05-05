package com.ray.android.passwordsecurekiwi.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * API Contract for the Accounts app
 */
public final class AccountLogin {

    /**
     * The "Content authority" is a name for the entire content provider.
     * In AccountLogin.java, we set this up as a string constant whose value is the same as that
     * from the AndroidManifest:
     */
    public static final String CONTENT_AUTHORITY = "com.ray.android.passwordsecurekiwi";
    /**
     * BASE_CONTENT_URI which will be shared by every URI associated with AccountLogin:
     * To make this a usable URI, we use the parse method which takes in a URI string
     * and returns a Uri.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * PATH_TableName stores the path for each of the tables which will be appended to the
     * base content URI.
     */
    public static final String PATH_ACCOUNTS = "accounts";

    /**
     * PATH_PASS_CODE_TABLE_NAME stores the path for each of the tables which will be appended to the
     * base content URI.
     */
    public static final String PATH_PASS_CODE = "pass_codes";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private AccountLogin() {
            }

    /**
     * Inner class that defines constant values for the accounts database table.
     * Each entry in the table represents a single account.
     */
    public static final class AccountEntry implements BaseColumns {

        /**
         * Complete CONTENT_URI
         * The Uri.withAppendedPath() method appends the BASE_CONTENT_URI
         * (which contains the scheme and the content authority) to the path segment.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ACCOUNTS);

        // TEST
        /**
         * Complete CONTENT_URI
         * The Uri.withAppendedPath() method appends the BASE_CONTENT_URI
         * (which contains the scheme and the content authority) to the path segment.
         */
        public static final Uri CONTENT_URI2 = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PASS_CODE);


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of accounts.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACCOUNTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single account.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACCOUNTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of passcodes
         */
        public static final String CONTENT_PASSCODE_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PASS_CODE;

        /**
         * Name of database table for accounts
         */
        public final static String TABLE_NAME = "account";

        /**
         * Unique ID number for the account (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Title of the account.
         * Type: TEXT
         */
        public final static String COLUMN_ACCOUNT_NAME = "account_title";

        /**
         * User name of the account.
         * Type: TEXT
         */
        public final static String COLUMN_USER_NAME = "user_name";

        /**
         * Password of the account.
         * Type:Text
         */
        public final static String COLUMN_ACCOUNT_PASSWORD = "account_password";

        /**
         * Notes for the account
         * Type:Text
         */
        public final static String COLUMN_ACCOUNT_NOTES = "account_notes";


        ////////
        /////////////  Pass_CODE_Table details below
        ///////

        /**
         * Name of database table for pass_code
         */
        public final static String PASS_CODE_TABLE_NAME = "pass_code";


        /**
         * Unique ID number for the account (only for use in the pass_code table).
         * Type: INTEGER
         */
        public final static String PASS_CODE_ID = BaseColumns._ID;


        /**
         * PASS_CODE for the app
         * Type: INTEGER
         */
        public final static String COLUMN_PASS_CODE = "pass_code";










    }




}
