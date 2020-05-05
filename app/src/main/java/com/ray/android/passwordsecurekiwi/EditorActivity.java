package com.ray.android.passwordsecurekiwi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ray.android.passwordsecurekiwi.data.AccountLogin;


/**
 * Allows user to create a new account or edit an existing one
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the account data loader
    private static final int EXISTING_ACCOUNT_LOADER = 0;

    // Content URI for the existing account (null if it's a new account)
    private Uri mCurrentAccountUri;

    // EditText fields to enter the account's information
    private EditText mAccountTypeEditText, mUserNameEditText, mPasswordEditText, mAccountNotesEditText;



    // Values for validation
    private String AccountTypeString;
    private String UserNameString;
    private String PasswordString;
    private String AccountNotesString;


    // Boolean flag that keeps track of whether the account has been edited (true) or not (false)
    private boolean mAccountHasChanged = false;


    /*
    * OnTouchListener that listens touches on EditText views, implying that they are modifying
    * the view, so we change the mAccountHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mAccountHasChanged = true;
            view.performClick();
            return false;
        }
    };


    /**
     * OnFocusChangeListener that listens for any click outside EditTextd field
     * so we can hide the keyboard.
     */
    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                hideSoftKeyboard(view);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_editor);

        // Examine intent that was used to launch this activity
        // in order to figure out if we're creating a new account or or editing an existing one
        Intent intent = getIntent();
        mCurrentAccountUri = intent.getData();

        // If the intent DOES NOT contain a account content URI, then we know that we are
        // creating a new account
        if (mCurrentAccountUri == null) {
            // This is a new account, so change the app bar to say "Add a Account"
            setTitle(getString(R.string.editor_activity_title_add_account));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a account that hasn't been created yet)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing account, so change app bar to say "Edit Account"
            setTitle(getString(R.string.editor_activity_title_edit_account));

            // Initialize a loader to read the account data from the database
            // and display the current values of the editor
            getSupportLoaderManager().initLoader(EXISTING_ACCOUNT_LOADER, null, this);
        }

        // Find all the relevant view we'll need to read user input from
        mAccountTypeEditText = findViewById(R.id.editTextAccountType);
        mUserNameEditText = findViewById(R.id.editUserName);
        mPasswordEditText = findViewById(R.id.editTextPassword);
        mAccountNotesEditText = findViewById(R.id.editTextAccountNotes);



        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mAccountTypeEditText.setOnTouchListener(mTouchListener);
        mUserNameEditText.setOnTouchListener(mTouchListener);
        mPasswordEditText.setOnTouchListener(mTouchListener);
        mAccountNotesEditText.setOnTouchListener(mTouchListener);


        // Setup OnFocusChangeListeners on all the input fields, so we can hide the
        // soft keyboard and get it out of the way
        mAccountTypeEditText.setOnFocusChangeListener(mFocusChangeListener);
        mUserNameEditText.setOnFocusChangeListener(mFocusChangeListener);
        mPasswordEditText.setOnFocusChangeListener(mFocusChangeListener);
        mAccountNotesEditText.setOnFocusChangeListener(mFocusChangeListener);

    }




    public boolean isValidAccount() {
        // Read from input fields. Use trim to eliminate leading or trailing white space.
        AccountTypeString = mAccountTypeEditText.getText().toString().trim();
        UserNameString = mUserNameEditText.getText().toString().trim();
        PasswordString = mPasswordEditText.getText().toString().trim();
        AccountNotesString = mAccountNotesEditText.getText().toString().trim();

        if (TextUtils.isEmpty(AccountNotesString)) {
            AccountNotesString = " ";
        }


        // Quick validation
        if (TextUtils.isEmpty(AccountTypeString)) {
            // Show the error in a toast message.
            Toast.makeText(this, getString(R.string.toast_required_account_name),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(UserNameString)) {
            // Show the error in a toast message
            Toast.makeText(this, getString(R.string.toast_required_user_name),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(PasswordString)) {
            // Show the error in a toast message
            Toast.makeText(this, getString(R.string.toast_required_pass_code),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get user input from editor and save account into database.
     */
    private void saveAccount() {
        // Check if this is supposed to be a new account
        // and check if all the fields in the editor are blank
        if (mCurrentAccountUri == null &&
                TextUtils.isEmpty(AccountTypeString) && TextUtils.isEmpty(UserNameString) &&
                    TextUtils.isEmpty(PasswordString) && TextUtils.isEmpty(AccountNotesString)) {
            // Since no fields were modified, we can return early without creating a new account.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are they keys,
        // and column attributes from the editors are values
        ContentValues values = new ContentValues();
        values.put(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME, AccountTypeString);
        values.put(AccountLogin.AccountEntry.COLUMN_USER_NAME, UserNameString);
        values.put(AccountLogin.AccountEntry.COLUMN_ACCOUNT_PASSWORD, PasswordString);
        values.put(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NOTES, AccountNotesString);


        // Determine if this is a new or existing account by checking if mCurrentAccountUri is null or not
        if (mCurrentAccountUri == null) {
            // This is a new account, so insert a new account into the provider,
            // returning the content URI for the new account
            Uri newUri = getContentResolver().insert(AccountLogin.AccountEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.toast_insert_account_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast
                Toast.makeText(this, getString(R.string.toast_insert_account_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING account, so update the account with content URI: mCurrentAccountUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentAccountUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentAccountUri, values, null,
                    null);

            // Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.toast_update_account_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise the update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.toast_update_account_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new account, hide the "Delete" menu item
        if (mCurrentAccountUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save account to database
                if (isValidAccount()) {
                    saveAccount();
                    // Exit activity
                    finish();
                    return true;
                } else {
                    return false;
                }
                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                HelperClass.showDeleteConfirmationDialog(this, mCurrentAccountUri);
                return true;
            // Responds to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the account hasn't changed, continue with navigating up to a parent activity
                // which is the link {@link AccountActivity}
                if (!mAccountHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, set up a dialog to warn the user
                // Create a click listener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the account hasn't changed, continue with handling back button press
        if (!mAccountHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Since the editor shows all account attributes, define a projection that contains
        // all columns from the account table
        String [] projection = {
                AccountLogin.AccountEntry._ID,
                AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME,
                AccountLogin.AccountEntry.COLUMN_USER_NAME,
                AccountLogin.AccountEntry.COLUMN_ACCOUNT_PASSWORD,
                AccountLogin.AccountEntry.COLUMN_ACCOUNT_NOTES} ;

        // This loader will execute ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentAccountUri,        // Query the content URI for the current account
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the curosor
        if (cursor == null || cursor.getCount() <1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of account attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME);
            int authorColumnIndex = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_USER_NAME);
            int passwordColumnIndex = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_ACCOUNT_PASSWORD);
            int notesColumnIndex = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NOTES);



            // Extract out the value from the Cursor for the given column index
            String account_name = cursor.getString(nameColumnIndex);
            String user_name = cursor.getString(authorColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String account_notes = cursor.getString(notesColumnIndex);




            // Update the views on the screen with the values in the database
            mAccountTypeEditText.setText(account_name);
            mUserNameEditText.setText(user_name);
            mPasswordEditText.setText(password);
            mAccountNotesEditText.setText(account_notes);


        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mAccountTypeEditText.setText("");
        mUserNameEditText.setText("");
        mPasswordEditText.setText("");
        mAccountNotesEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the account.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Hide the software keyboard when necessary
    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
