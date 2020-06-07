package com.furkank.colordetector.handler;

import android.app.Activity;
import android.widget.Button;
import android.widget.Toast;

import com.furkank.colordetector.model.User;
import com.furkank.colordetector.firebase.FirebaseReadHandler;
import com.furkank.colordetector.firebase.FirebaseWriteHandler;

import java.util.ArrayList;

public class AuthHandler {

    private Activity loginActivity;

    private String email;
    private String password;

    private Button loginButton = null;

    private SessionHandler sessionHandler = null;
    private FirebaseWriteHandler firebaseWriteHandler = null;
    private FirebaseReadHandler<User> firebaseReadHandler = null;

    public AuthHandler(Activity loginActivity, Button loginButton) {
        this.sessionHandler = new SessionHandler(loginActivity);
        this.firebaseWriteHandler = new FirebaseWriteHandler();
        this.firebaseReadHandler = new FirebaseReadHandler<User>(User.class, loginActivity);

        this.loginActivity = loginActivity;
        this.loginButton = loginButton;
    }

    /**
     * Let user log in or being headed to register
     *
     * @param email
     * @param password
     */
    public void login(String email, String password) {
        this.email = email;
        this.password = password;
        firebaseReadHandler.read("users", this::readCallback);
    }

    public Integer readCallback(ArrayList<User> input) {
        // Create a user instance with information
        User loginUser = new User(email, password);

        // If there is no user in db, register
        if (input.size() == 0) {
            register(loginUser);
            return null;
        }

        for (User user : input) {
            if (user.getEmail().equals(loginUser.getEmail())) {
                // Login is successful
                if (user.getPassword().equals(loginUser.getPassword())) {
                    Toast.makeText(loginActivity, "Login successfully", Toast.LENGTH_SHORT).show();
                    sessionHandler.create(email, password);
                    return null;
                }
                // Login failed
                else {
                    Toast.makeText(loginActivity, "Hatalı şifre !", Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                    return null;
                }
            }
        }

        // Entered email is not existing, register
        register(loginUser);

        return null;
    }

    /**
     * Registers new user and let them log in
     *
     * @param newUser
     */
    private void register(User newUser) {
        Toast.makeText(loginActivity, "You are being registered..", Toast.LENGTH_SHORT).show();
        boolean result = firebaseWriteHandler.add("users", newUser);
        if (result) {
            sessionHandler.create(newUser.getEmail(), newUser.getPassword());
            Toast.makeText(loginActivity, "Registration successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(loginActivity, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }


}
