package com.furkank.colordetector.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseWriteHandler {
    FirebaseDatabase database = null;

    public FirebaseWriteHandler()
    {
        database = FirebaseDatabase.getInstance();
    }

    /**
     * Adds data to the collection in the firebase realtime database
     *
     * @param collection
     * @param data
     */
    public boolean add(String collection, Object data) {
        DatabaseReference myRef = database.getReference(collection);
        DatabaseReference newRef = myRef.push();
        newRef.setValue(data);
        return true;
    }
}