package com.ray.android.passwordsecurekiwi;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.ray.android.passwordsecurekiwi.data.AccountLogin;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {

    /**
     * Identifier for the account data loader
     */
    private static final int EXISTING_ACCOUNT_LOADER = 0;
    /**
     * String that holds the account account_title for the AppBar
     */
    String account_title;
    /**
     * Custom Expandable AppBar
     */
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    AppBarLayout mAppBarLayout;


    /**
     * Content URI for the existing account (null if it's a new account)
     */
    private Uri mCurrentAccountUri;
    /**
     * EditText fields to enter the account's information
     */
    private TextView mTitle, mUserName, mPassword, mNotes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back button click
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Style the CollapsingToolbarLayout Title
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        // This is the most important when you are putting custom textview in CollapsingToolbar
        mCollapsingToolbarLayout.setTitle(" ");

        mAppBarLayout = findViewById(R.id.appbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                // Test if APPBar is expanded or collapsed
                if (scrollRange + verticalOffset == 0) {
                    // When AppBar is collapsed
                    // when collapsingToolbar at that time display actionbar account_title
                    mCollapsingToolbarLayout.setTitle(account_title);
                    isShow = true;

                } else if (isShow) {
                    // When AppBar is collapsed
                    // careful there must be a space between double quote otherwise it doesn't work
                    mCollapsingToolbarLayout.setTitle(" ");
                    isShow = false;



                }
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);





        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new account or editing an existing one.
        Intent intent = getIntent();
        mCurrentAccountUri = intent.getData();

        // Initialize a loader to read the account data from the database
        // and display the current values on the screen
        getSupportLoaderManager().initLoader(EXISTING_ACCOUNT_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mTitle = findViewById(R.id.textViewTitle);
        // mAccountType = findViewById(R.id.textViewAccountType);
        mUserName = findViewById(R.id.textViewUserName);
        mPassword = findViewById(R.id.textViewPassword);
        mNotes = findViewById(R.id.textViewAccountNotes);
    }



    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Since the activity shows all account attributes, define a projection that contains
        // all columns from the account table
        String[] projection = {
                AccountLogin.AccountEntry._ID,
                AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME,
                AccountLogin.AccountEntry.COLUMN_USER_NAME,
                AccountLogin.AccountEntry.COLUMN_ACCOUNT_PASSWORD,
                AccountLogin.AccountEntry.COLUMN_ACCOUNT_NOTES};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentAccountUri,                // Query the content URI for the current account
                projection,                     // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,               // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of account attributes that we're interested in
            int AccountType = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME);
            int authorColumnIndex = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_USER_NAME);
            int passwordColumnIndex = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_ACCOUNT_PASSWORD);
            int notesColumnIndex = cursor.getColumnIndex(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NOTES);


            // Extract out the value from the Cursor for the given column index
            account_title = cursor.getString(AccountType);
            String author = cursor.getString(authorColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String notes = cursor.getString(notesColumnIndex);


            mTitle.setText(account_title);
            // mAccountType.setText(account_title);
            mUserName.setText(author);
            mPassword.setText(password);
            mNotes.setText(notes);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Edit" menu option
            case R.id.action_edit:
                // Go to {@link EditorActivity}
                Intent editIntent = new Intent(this, EditorActivity.class);
                editIntent.setData(mCurrentAccountUri);
                startActivity(editIntent);
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                HelperClass.showDeleteConfirmationDialog(this, mCurrentAccountUri);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Go back to {@link CatalogActivity}.
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitle.setText(getResources().getString(R.string.currently_unavailable));
        // mAccountType.setText(getResources().getString(R.string.currently_unavailable));

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

    }
}
