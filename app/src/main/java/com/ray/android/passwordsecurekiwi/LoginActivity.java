package com.ray.android.passwordsecurekiwi;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ray.android.passwordsecurekiwi.R;
import com.ray.android.passwordsecurekiwi.data.AccountDbHelper;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    EditText _passwordText;
    Button _loginButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _passwordText = findViewById(R.id.input_password);
        _loginButton = findViewById(R.id.btn_login);

        // If a pass code hasn't been created, then start at SignUp page
        if (AccountDbHelper.getPassCodeCount(LoginActivity.this) == 0) {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            finish();
            startActivity(intent);
        }



        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String password = _passwordText.getText().toString();
                if (password.length() > 0) {
                    login();
                }

            }
        });


    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);


        Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
        finish();
        startActivity(intent);



        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        //progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        String password = _passwordText.getText().toString();
        if (password.length() != 4) {
            Toast.makeText(getBaseContext(), "Must be 4 digit code", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        }


        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String password = _passwordText.getText().toString();

        if (password.length() != 4 || password.length() == 0) {
            _passwordText.setError("Must be 4 digit code");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (AccountDbHelper.getPinCode(LoginActivity.this) != Integer.parseInt(password)) {
            _passwordText.setError("Pin code does not match");
            valid = false;
        } else {
            _passwordText.setError(null);
        }


        return valid;
    }
}