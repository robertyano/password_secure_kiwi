package com.ray.android.passwordsecurekiwi;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ray.android.passwordsecurekiwi.adapters.CSVReader;
import com.ray.android.passwordsecurekiwi.data.AccountDbHelper;
import com.ray.android.passwordsecurekiwi.data.AccountLogin;



public class ImportAccountActivity extends AppCompatActivity {

    TextView textFile;

    String FilePath;

    BufferedReader buffer;

    Cursor returnCursor;

    int nameIndex;

    String mCSVfile;

    Intent intent2;

    Uri returnUri;


    private static final int PICKFILE_RESULT_CODE = 1;

    private static final int IMPORTFILE_RESULT_CODE = 1;

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_import);

        // Change the app bar to say "Import Accounts"
        setTitle(getString(R.string.import_accounts_overview));


        Button btn_choose_file = findViewById(R.id.btn_attach_file);
        textFile = findViewById(R.id.file_attached);
        // Setup CSV Import Button
        AppCompatButton btn_csv_import = findViewById(R.id.btn_import_csv);

        btn_choose_file.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);

            }});

        // Testing the Import in OnClickListener
        btn_csv_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Access Database to be writable
                SQLiteOpenHelper database = new AccountDbHelper(getApplicationContext());
                SQLiteDatabase db = database.getWritableDatabase();


                InputStream inStream = null;
                try {
                    inStream = getContentResolver().openInputStream(returnUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e("ImportAccountActivity", "INPUT STREAM WAS NOT OPENED");
                }
                BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
                String line = "";
                db.beginTransaction();
                // Have file skip first row with header columns
                boolean skip = true;
                try {
                    while ((line = buffer.readLine()) != null) {
                        // File skips first row and sets variable to false, so that all other
                        // lines get imported into DB
                        if(skip) {
                            skip = false; // Skip only the first line
                            continue; }



                        String[] colums = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        if (colums.length != 5) {
                            Log.e("CSVParser", "Skipping Bad CSV Row");
                            Log.e("CSVParser", "Skipping:" + colums[4].trim());
                            Log.e("CSVParser", "Columns length: " + colums.length);
                            continue;
                        }




                        ContentValues cv = new ContentValues();
                        // ADDED replace for parsing tests
                        // cv.put("_id", colums[0].trim());
                        cv.put("account_title", colums[1].trim().replace('`',','));
                        cv.put("user_name", colums[2].trim().replace('`',','));
                        cv.put("account_password", colums[3].trim().replace('`',','));
                        cv.put("account_notes", colums[4].trim().replace('`',','));
                        db.insert(AccountLogin.AccountEntry.TABLE_NAME, null, cv);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("ImportAccountActivity", "DATABASE didn't have a chance to parse");
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();

                    Toast.makeText(ImportAccountActivity.this, R.string.toast_csv_import_sucessful,
                            Toast.LENGTH_LONG).show();

                // Open intent to go back to account_main.xml
                Intent intent = new Intent(ImportAccountActivity.this, AccountActivity.class);
                startActivity(intent);




            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    FilePath = data.getData().getPath();
                    // Uri returnUri = data.getData();  Removed for test
                    returnUri = data.getData();


                    returnCursor = getContentResolver().query(returnUri, null,
                            null, null, null);



                    /*
                     * Get the column indexes of the data in the Cursor,
                     * move to the first row in the Cursor, get the data,
                     * and display it.
                     */
                    nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);


                    returnCursor.moveToFirst();

                    // Get name of CSV file that was attached
                    mCSVfile = returnCursor.getString(nameIndex);


                    Log.e("ImportAccountActivity","FilePath:" + FilePath );
                    Log.e("ImportAccountActivity","ReturnCursor:" + returnCursor);
                    Log.e("ImportAccountActivity","mCSVfile:" + mCSVfile);

                    textFile.setText(returnCursor.getString(nameIndex) + " has been selected!");




                }
                break;


        }
    }

}



