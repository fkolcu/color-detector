package com.furkank.colordetector.model;

import com.furkank.colordetector.handler.EncryptionHandler;

public class User {
    public String email;
    public String password;

    public User(){
    }

    public User(String email, String password){
        this.email = email;
        this.password = EncryptionHandler.encryptPassword(password);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPlainPassword(String plainPassword)
    {
        this.password = EncryptionHandler.encryptPassword(password);
    }

    public void setPassword(String password) {
        this.password =password;
    }
}
