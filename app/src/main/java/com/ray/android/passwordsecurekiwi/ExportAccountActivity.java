package com.ray.android.passwordsecurekiwi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.ray.android.passwordsecurekiwi.adapters.CSVWriter;
import com.ray.android.passwordsecurekiwi.data.AccountDbHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Allows user to export a account to a new device
 */
public class ExportAccountActivity extends AppCompatActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TEST TEST TEST
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        setContentView(R.layout.account_export);

        // Change the app bar to say "Export Accounts"
        setTitle(getString(R.string.export_accounts_overview));

        // Setup CSV Download Button
        AppCompatButton btn_csv_download = findViewById(R.id.btn_download_csv);
        btn_csv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                ShareFile();
            }
        });
    }

    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(ExportAccountActivity.this);
        AccountDbHelper dbhelper;
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getString(R.string.toast_exporting_database));
            this.dialog.show();
            dbhelper = new AccountDbHelper(ExportAccountActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

           /* File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (!exportDir.exists()) { exportDir.mkdirs(); }*/

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Password_Secure_Kiwi_data.csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file),
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
                Cursor curCSV = dbhelper.raw();
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext()) {
                    String arrStr[]=null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for(int i=0;i<curCSV.getColumnNames().length;i++)
                    {
                        mySecondStringArray[i] =curCSV.getString(i).replace(',', '`');   /// ADDED REPLACE FOR PARSING TEST
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                Log.e("ExportAccountActivity", "Exception: " + e);
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
            if (success) {
                Toast.makeText(ExportAccountActivity.this, getString(R.string.toast_csv_save_sucessful),
                        Toast.LENGTH_LONG).show();
                // ShareGif();
            } else {
                Toast.makeText(ExportAccountActivity.this, getString(R.string.toast_csv_unsucessful),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Function to share CSV file via email or other apps
    private void ShareFile() {
        File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
        String fileName = "Password_Secure_Kiwi_data.csv";
        File sharingGifFile = new File(exportDir, fileName);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(),"com.ray.android.passwordsecurekiwi.fileprovider" , sharingGifFile);


        List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            getApplicationContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Share CSV").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
    }




}


