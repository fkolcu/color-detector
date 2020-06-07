package com.furkank.colordetector;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.furkank.colordetector.handler.AuthHandler;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    private Snackbar snackbar = null;

    private Button loginButton = null;
    private TextView emailTxt = null;
    private TextView passwordTxt = null;

    private AuthHandler authHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Define login tools
        loginButton = findViewById(R.id.login);
        emailTxt = findViewById(R.id.email);
        passwordTxt = findViewById(R.id.password);

        // Handle to login and register
        authHandler = new AuthHandler(this, loginButton);
    }

    public void onLoginButtonClicked(View view) {
        if (snackbar != null) {
            snackbar.dismiss();
        }

        if (emailTxt.getText().toString().isEmpty()) {
            snackbar = Snackbar.make(view, R.string.email_required, 3000);
            snackbar.show();
            return;
        }

        if (passwordTxt.getText().toString().isEmpty()) {
            snackbar = Snackbar.make(view, R.string.password_required, 3000);
            snackbar.show();
            return;
        }

        loginButton.setEnabled(false);
        authHandler.login(emailTxt.getText().toString(), passwordTxt.getText().toString());
    }
}
