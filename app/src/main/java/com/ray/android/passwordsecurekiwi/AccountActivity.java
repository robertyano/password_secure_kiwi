package com.ray.android.passwordsecurekiwi;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ray.android.passwordsecurekiwi.adapters.AccountCursorAdapter;
import com.ray.android.passwordsecurekiwi.adapters.SwipeDismissListViewTouchListener;
import com.ray.android.passwordsecurekiwi.data.AccountDbHelper;
import com.ray.android.passwordsecurekiwi.data.AccountLogin;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * Displays list of accounts that were entered and stored in the app
 */
public class AccountActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

     /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = "My Log - " + ContentProvider.class.getSimpleName();

    private static final String TAG = "AccountActivity";


    /**
     * Initialize value can be set asy any unique integer
     */
    public static final int ACCOUNT_LOADER = 0;

    // orderOfResults value to check whether results should be ordered by ASC or DESC
    int checker = 2;


    /**
     * Database helper object  // ADDED THIS FOR TESTS
     */
    private AccountDbHelper mDbHelper;


    /**
     * The adapter for the list view
     */
    AccountCursorAdapter mCursorAdapter;

    /**
     *  The Sort By preference array
     */
    String[] sortBy;

    /**
     *  Number for results messages that display above the ListView
     */
    private String resultsCounter;
    private TextView numberOfResults;
    private TextView orderOfResults;
    private LinearLayout orderByToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new AccountDbHelper(this);    // ADDED FOR TESTS
        setContentView(R.layout.account_main);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) {
            // showStartDialog();
            final AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.first_open_message_header)
                    .setMessage(R.string.first_open_message)
                    .setPositiveButton(R.string.first_message_confirmation, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("firstStart", false);
                            editor.apply();
                        }
                    })
                    .create();
            d.show();

            // Make the textview clickable. Must be called after show()
            ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }




        // Get the App Title Bar and menu items to display
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the account data
        final ListView accountListView = findViewById(R.id.list);



        // Setup an adapter to create a list item for each row of account data in the Cursor.
        // There is no account data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new AccountCursorAdapter(this, null);
        accountListView.setAdapter(mCursorAdapter);

        // Set up a account list item click listener
        accountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create a new intent to go to {@link DetailActivity
                Intent intent = new Intent(AccountActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific account that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                Uri currentAccountUri = ContentUris.withAppendedId(AccountLogin.AccountEntry.CONTENT_URI,
                        id);

                // Set th URI on the data field of the intent
                intent.setData(currentAccountUri);
                startActivity(intent);

            }
        });

        // Kick off the loader that loads the list items
        getLoaderManager().initLoader(ACCOUNT_LOADER, null,this);

        // Find and set the results counter in the toolbar area
        numberOfResults = findViewById(R.id.orderby_toolbar_results);
        orderOfResults = findViewById(R.id.orderby_toolbar_ordering);
        orderByToolbar = findViewById(R.id.orderby_toolbar);
        setResultsCounter(this, numberOfResults, orderOfResults);

        // Swipe to delete
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(accountListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int id : reverseSortedPositions) {
                                    long currentID = listView.getAdapter().getItemId(id);
                                    Uri currentAccountUri = ContentUris.withAppendedId(AccountLogin.AccountEntry.CONTENT_URI, currentID);
                                    HelperClass.deleteAccount(AccountActivity.this, currentAccountUri, false);
                                    setResultsCounter(AccountActivity.this, numberOfResults, orderOfResults);
                                }
                            }
                        });
        accountListView.setOnTouchListener(touchListener);



        // Set up a click listener for the OrderBy TextView which opens up Preferences
        orderOfResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get writable database
                SQLiteDatabase database = mDbHelper.getWritableDatabase();

                if (checker == 0) {
                    // Query for items from the database and get a cursor back
                    Cursor todoCursor = database.rawQuery("Select * From " +
                            AccountLogin.AccountEntry.TABLE_NAME + " Order By " +
                            AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME + " ASC;", null);
                    mCursorAdapter.swapCursor(todoCursor);
                    orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ascending, 0);
                    checker = 1;
                } else {
                    // Query for items from the database and get a cursor back
                    Cursor todoCursor = database.rawQuery("Select * From " +
                            AccountLogin.AccountEntry.TABLE_NAME + " Order By " +
                            AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME + " DESC;", null);
                    mCursorAdapter.swapCursor(todoCursor);
                    orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_descending, 0);
                    checker = 0;
                }
            }
        });
    }

  /*  private void showStartDialog() {
        final AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.first_open_message_header)
                .setMessage(R.string.first_open_message)
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("firstStart", false);
                        editor.apply();
                    }
                })
                .create().show();
    }*/


    /**
     * Helper that checks and sets the results counter above the ListView
     */
    public void setResultsCounter(Context context, TextView numberOfResults, TextView orderOfResults) {
        // Show how many results found
        long count = AccountDbHelper.getAccountsCount(this);
        resultsCounter = String.valueOf(count) + " " + getString(R.string.order_by_toolbar_results_found);
        numberOfResults.setText(resultsCounter);

        // Shows what it's sorted by and changes the arrow graphic accordingly
        sortBy = HelperClass.getSortByPreference(context);
        String sortColumn;
        //sortBy[0] to retrieve the column and sortBy[2] to retrieve the sort direction


        // Log.d(TAG, "sortBy[0] is " + sortBy[0]);

        switch (sortBy[0]) {
            case "account_title":
                sortColumn = getString(R.string.pref_order_by_account_label);
                break; 
            default:
                sortColumn = getString(R.string.pref_order_by_account_label);
                break;
        }

        switch (sortBy[1]) {
            case "ASC":
                orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ascending, 0);
                break;
            case "DESC":
                orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_descending, 0);
                break;
            default:
                orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ascending, 0);
                break;
        }
        orderOfResults.setText(sortColumn);

        // Displays App Title if there are no accounts to show
        orderByToolbar.setVisibility(View.VISIBLE);
        // Displays the app account_title
        getSupportActionBar().setTitle(R.string.app_name);

    }


    /**
     * Helper method to delete all accounts in the database.
     */
    private void deleteAllAccounts() {
        int rowsDeleted = getContentResolver().delete(AccountLogin.AccountEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            // If all rows were deleted, then show the success message as a toast
            Toast.makeText(AccountActivity.this, getString(R.string.toast_delete_all_accounts_successful, rowsDeleted),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Nothing was deleted
            Toast.makeText(AccountActivity.this, getString(R.string.toast_delete_all_accounts_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to delete pin codes in the database
     */
    private void deletePinCode() {
        int rowsDeleted = getContentResolver().delete(AccountLogin.AccountEntry.CONTENT_URI2, null, null);
        if (rowsDeleted > 0) {
            // If pass_code was deleted, then show the sucess message as a toast
            Toast.makeText(AccountActivity.this, getString(R.string.toast_delete_pin_code),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Prompt the user to confirm that they want to delete ALL the accounts.
     */
    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all the accounts.
                deleteAllAccounts();
                setResultsCounter(AccountActivity.this, numberOfResults, orderOfResults);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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

    /**
     * Prompt the user to confirm that they want to update Pin Code
     */
    private void updatePinCodeDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.update_pin_code_msg);
        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Update" button, so update the pin code
                deletePinCode();

                // Start at SignUp page to create pin code
                if (AccountDbHelper.getPassCodeCount(AccountActivity.this) == 0) {
                    Intent intent = new Intent(AccountActivity.this, SignupActivity.class);
                    finish();
                    startActivity(intent);
                }

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Add a Account" menu option
            case R.id.action_add:
                // Open EditorActivity
                Intent intent = new Intent(AccountActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;


            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Pop up confirmation dialog for deletion
                showDeleteAllConfirmationDialog();
                return true;

            // Respond to a click on the "Update Pin Code" menu option
            case R.id.action_update_pin_code:
                // Pop up confirmation dialog for update
                updatePinCodeDialog();
                return true;

            // Responds to a click on the "View Source Code" menu option
            case R.id.view_source_code:
                // Send to GitHub to view code
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/robertyano/password_secure_kiwi"));
                startActivity(browserIntent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table.
        // Use only the columns needed to display the list view for better performance.
        String[] projection = {
                AccountLogin.AccountEntry._ID,
                AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME,
                AccountLogin.AccountEntry.COLUMN_USER_NAME};

        // Get the Sort By preference array from Preferences using the helper method
        sortBy = HelperClass.getSortByPreference(this);

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,               // Parent activity context
                AccountLogin.AccountEntry.CONTENT_URI,                      //Provider content URI to query
                projection,                                 // Columns to include in the resulting Cursor
                null,                              // No selection clause
                null,                           // No selection arguments
                sortBy[0] + " " + sortBy[1]);      // Sort order from Preferences
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursorData) {
        // Update {@link AccountCursorAdapter} with this new cursor containing updated account data
        mCursorAdapter.swapCursor(cursorData);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setResultsCounter(this, numberOfResults, orderOfResults);
    }
}
