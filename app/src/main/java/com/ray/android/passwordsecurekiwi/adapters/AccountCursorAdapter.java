package com.ray.android.passwordsecurekiwi.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ray.android.passwordsecurekiwi.R;
import com.ray.android.passwordsecurekiwi.data.AccountLogin;

/**
 * {@link AccountCursorAdapter} is an adapter for a listview or grid view
 * that uses a {@link Cursor} of account data as its data source. This adapter knows
 * how to create list items for each row of acount data in the {@link Cursor}.
 */
public class AccountCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link AccountCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public AccountCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }


    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Return the list item view.
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the account data (in the current row pointed to by cursor) to the given
     * list item layout. For RAY, the account_title for the current account can be set on the account_title TextView
     * in the list item layout.
     *
     * @param view          Existing view, returned earlier by newView() method
     * @param context       app context
     * @param cursorData    The cursor from which to get the data. The cursor is already moved to the
     *                      correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursorData) {

        // Find fields to populated in inflated template
        TextView titleTextView = view.findViewById(R.id.account_type);
        TextView authorTextView = view.findViewById(R.id.user_namee);


        // Extract properties from cursor
        final String accountTitle = cursorData.getString(cursorData.getColumnIndexOrThrow(AccountLogin.AccountEntry.COLUMN_ACCOUNT_NAME));
        final String accountAuthor = cursorData.getString(cursorData.getColumnIndexOrThrow(AccountLogin.AccountEntry.COLUMN_USER_NAME));



        // Populate fields with extracted properties
        titleTextView.setText(accountTitle);
        authorTextView.setText(accountAuthor);


    }
}
