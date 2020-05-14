package com.ray.android.passwordsecurekiwi;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ray.android.passwordsecurekiwi.adapters.CSVReader;
import com.ray.android.passwordsecurekiwi.data.AccountDbHelper;
import com.ray.android.passwordsecurekiwi.data.AccountLogin;



public class ImportAccountActivity extends AppCompatActivity {

    /**
     * Database helper object  // ADDED THIS FOR TESTS
     */
    private AccountDbHelper mDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_import);

        // Change the app bar to say "Import Accounts"
        setTitle(getString(R.string.import_accounts_overview));

        // Setup CSV Download Button
        AppCompatButton btn_import_csv = findViewById(R.id.btn_import_csv);
        btn_import_csv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {





            }
        });



    }
}
