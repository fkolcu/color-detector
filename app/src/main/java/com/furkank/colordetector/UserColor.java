package com.furkank.colordetector;

import com.furkank.colordetector.firebase.FirebaseWriteHandler;

public class UserColor {
    public String key;

    // User email address to store colors with
    public String email;

    // Hex color code, like #2281b0
    public String color;

    // Color name, like blue
    public String name;

    public UserColor() {
    }

    public UserColor(String userEmail, String userColor, String userColorName) {
        email = userEmail;
        color = userColor;
        name = userColorName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Saves user color into db
     * @param hex
     * @param name
     * @return
     */
    public boolean save(String email, String hex, String name) {
        // Create new user color instance
        UserColor userColor = new UserColor(email, hex, name);

        FirebaseWriteHandler fbWrite = new FirebaseWriteHandler();
        return fbWrite.add("colors", userColor);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
