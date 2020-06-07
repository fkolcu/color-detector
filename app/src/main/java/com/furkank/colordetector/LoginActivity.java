package com.furkank.colordetector;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.furkank.colordetector.handler.AuthHandler;

public class LoginActivity extends AppCompatActivity {

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

    public void onLoginButtonClicked(View view)
    {
        loginButton.setEnabled(false);
        authHandler.login(emailTxt.getText().toString(), passwordTxt.getText().toString());
    }
}
