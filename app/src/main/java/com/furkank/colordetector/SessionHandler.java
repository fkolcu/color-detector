package com.furkank.colordetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class SessionHandler {
    private static final int PRIVATE_MODE = 0;
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String PREF_NAME = "spColorDetector";

    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private Context context;

    public SessionHandler(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    /**
     * Gets logged in user email
     *
     * @return
     */
    public String getUserEmail() {
        if (!isLoggedIn()) {
            return "";
        }

        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public void create(String email, String password) {
        editor.putBoolean(IS_LOGIN, true);

        // Save email and password
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);

        editor.commit();

        // Go to main activity
        goTo("main");
    }

    /**
     * Gets the situation shows if user is logged in
     *
     * @return
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }

    public boolean checkLogin() {

        if (!this.isLoggedIn()) {
            goTo("login");
            return false;
        } else {
            return true;
        }
    }

    public void remove() {
        editor.clear();
        editor.commit();

        goTo("main");
    }

    private void goTo(String activity) {
        Class targetClass = null;

        switch (activity) {
            case "main":
                targetClass = MainActivity.class;
                break;
            case "login":
                targetClass = LoginActivity.class;
                break;
            default:
                Toast.makeText(context, "There is a problem on navigation", Toast.LENGTH_SHORT).show();
                return;
        }

        IntentHandler.open(context, targetClass);
    }
}
