package com.ray.android.passwordsecurekiwi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ray.android.passwordsecurekiwi.data.AccountDbHelper;
import com.ray.android.passwordsecurekiwi.data.AccountLogin;


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    // Global Context
    public static Context context;

    EditText _inputPinCode;
    EditText _inputPinCodeConfirmation;
    AppCompatButton _signupButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_signup);


        _inputPinCode = findViewById(R.id.input_pin_code);
        _inputPinCodeConfirmation = findViewById(R.id.input_pin_code_confirmation);
        _signupButton = findViewById(R.id.btn_signup);


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();

            }
        });

    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);


        String inputPinCodeString = _inputPinCode.getText().toString();


        // Create a ContentValues object where column names are they keys,
        // and account attributes from the editors are values
        ContentValues values = new ContentValues();
        values.put(AccountLogin.AccountEntry.COLUMN_PASS_CODE, inputPinCodeString);

        // This is a new passcode, so insert a new passcode into the provider,
        // returning the content URI for the new passcode
        Uri newUri = getContentResolver().insert(AccountLogin.AccountEntry.CONTENT_URI2, values);

        if (AccountDbHelper.getPassCodeCount(SignupActivity.this) > 0) {
            finish();
            Intent intent = new Intent(SignupActivity.this, AccountActivity.class);
            startActivity(intent);
        }










        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        //progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();


    }

    public void onSignupFailed() {
        // Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String inputPinCodeString = _inputPinCode.getText().toString();
        String inputPinCodeConfirmationString = _inputPinCodeConfirmation.getText().toString();


        if (inputPinCodeString.isEmpty() || inputPinCodeString.length() < 4 || inputPinCodeString.length() > 10) {
            _inputPinCode.setError("must be 4 digit code");
            Toast.makeText(getBaseContext(), "Must be 4 digit code", Toast.LENGTH_LONG).show();
            valid = false;
        } else {
            _inputPinCode.setError(null);
        }

        if (inputPinCodeConfirmationString.isEmpty() || inputPinCodeConfirmationString.length() < 4 ||
                inputPinCodeConfirmationString.length() > 10) {
            _inputPinCodeConfirmation.setError("must be 4 digit code");
            valid = false;
        } else {
            _inputPinCodeConfirmation.setError(null);
        }

        if (!inputPinCodeConfirmationString.equals(inputPinCodeString)) {
            _inputPinCodeConfirmation.setError("Pin Codes must match");
            Toast.makeText(getBaseContext(), "Pin Codes must match", Toast.LENGTH_LONG).show();
            valid = false;
        } else {
            _inputPinCode.setError(null);
        }



        return valid;
    }
}