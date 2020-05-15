package com.ray.android.passwordsecurekiwi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

    TextView textFile;

    private static final int PICKFILE_RESULT_CODE = 1;

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_import);

        // Change the app bar to say "Import Accounts"
        setTitle(getString(R.string.import_accounts_overview));


        Button btn_choose_file = findViewById(R.id.btn_attach_file);
        textFile = findViewById(R.id.file_attached);

        btn_choose_file.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);

            }});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String FilePath = data.getData().getPath();

                    Uri returnUri = data.getData();
                    Cursor returnCursor =
                            getContentResolver().query(returnUri, null, null, null, null);

                    /*
                     * Get the column indexes of the data in the Cursor,
                     * move to the first row in the Cursor, get the data,
                     * and display it.
                     */
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    returnCursor.moveToFirst();


                    Log.e("ImportAccountActivity","data: " + data );

                    textFile.setText(returnCursor.getString(nameIndex) + " has been selected!");

                    //textFile.setText(FilePath);
                }
                break;

        }
    }
}



