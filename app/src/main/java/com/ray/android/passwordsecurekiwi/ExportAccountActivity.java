package com.ray.android.passwordsecurekiwi;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Allows user to export a account to a new device
 */
public class ExportAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_export);

        // Change the app bar to say "Export Accounts OverView"
        setTitle(getString(R.string.export_accounts_overview));
    }


}
