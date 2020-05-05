package com.ray.android.passwordsecurekiwi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class HelperClass {


    /**
     * A helper method that gets the user's Sort By column and direction (ASC or DESC) preference
     * from Preferences
     *
     * @return String array sort by COLUMN and ascending or descending order
     * Example: new String[] { "AccountEntry.COLUMN_USER_NAME", "DESC" };
     *
     * To extract use String[] sortBy = HelperClass.getSortByPreference(context);
     * Then: sortBy[0] to retrieve the column and sortBy[2] to retrieve the sort direction;
     */
    public static String[] getSortByPreference(Context context) {
        String orderByColumn = getPreferenceStringValue(context, R.string.pref_order_by_key, R.string.pref_order_by_default);
        String orderByDireciton = getPreferenceStringValue(context, R.string.pref_order_by_direction_key,
                R.string.pref_order_by_direction_default);
        return new String[]{orderByColumn, orderByDireciton};
    }



    /**
     * A helper method to extract any current preference String value
     *
     * @param key          preference's key
     * @param defaultValue preference's default value
     * @return preference  current value
     */
    private static String getPreferenceStringValue(Context context, int key, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(
                context.getString(key),
                context.getString(defaultValue)
        );
    }

    /**
     * Prompt the user to confirm that they want to delete this account.
     */
    public static void showDeleteConfirmationDialog(final Context context, final Uri currentAccountUri){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Delete" button, so delete the account.
                deleteAccount(context, currentAccountUri, true);
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Perform the deletion of the account in the database.
     */
    public static void deleteAccount(Context context, Uri currentAccountUri, Boolean confirm) {
        // Only perform the delete if this is an existing account.
        if (currentAccountUri != null) {
            // Call the ContentResolver to delete the account at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentAccountUri
            // content URI already identifies the account that we want.
            int rowsDeleted = context.getContentResolver().delete(currentAccountUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(context, context.getString(R.string.toast_delete_account_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(context, context.getString(R.string.toast_delete_account_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // If accessed via the "Are you sure you want to delete" dialog
        if (confirm) {
            // Close the activity and go back to the main activity CatalogActivity instead of the
            // previous activity
            Intent i = new Intent(context, AccountActivity.class);
            ((Activity) context).finish();  //Kill the activity from which you will go to next activity
            context.startActivity(i);
        }
    }




}
