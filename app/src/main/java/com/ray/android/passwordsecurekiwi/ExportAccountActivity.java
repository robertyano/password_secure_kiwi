package com.ray.android.passwordsecurekiwi;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.ray.android.passwordsecurekiwi.adapters.CSVWriter;
import com.ray.android.passwordsecurekiwi.data.AccountDbHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

        // Setup CSV Download Button
        AppCompatButton btn_csv_download = findViewById(R.id.btn_download_csv);
        btn_csv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(ExportAccountActivity.this);
        AccountDbHelper dbhelper;
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            dbhelper = new AccountDbHelper(ExportAccountActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (!exportDir.exists()) { exportDir.mkdirs(); }

            File file = new File(exportDir, "Password_Secure_Kiwi_data.csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor curCSV = dbhelper.raw();
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext()) {
                    String arrStr[]=null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for(int i=0;i<curCSV.getColumnNames().length;i++)
                    {
                        mySecondStringArray[i] =curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
            if (success) {
                Toast.makeText(ExportAccountActivity.this, "CSV file has been saved to Downloads!", Toast.LENGTH_LONG).show();
                // ShareGif();
            } else {
                Toast.makeText(ExportAccountActivity.this, "Export failed", Toast.LENGTH_LONG).show();
            }
        }
    }




}


