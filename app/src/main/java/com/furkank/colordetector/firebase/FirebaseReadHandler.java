package com.furkank.colordetector.firebase;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class FirebaseReadHandler<T> {
    private Class<T> typeClass;
    private Activity activity;

    FirebaseDatabase database = null;

    public FirebaseReadHandler(Class<T> typeClass, Activity activity) {
        this.typeClass = typeClass;
        this.activity = activity;
        database = FirebaseDatabase.getInstance();
    }

    /**
     * Reads data and calls callback function with data
     *
     * @param collection
     * @param callback
     */
    public void read(String collection, final Function<ArrayList<T>, Integer> callback) {
        final ArrayList<T> objectList = new ArrayList<T>();

        DatabaseReference ref = database.getReference(collection);

        // After reference created, it will trigger the following event
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Get object with set value
                    T object = ds.getValue(typeClass);

                    // Try to add key into the object
                    try {
                        object.getClass().getMethod("setKey", String.class).invoke(object, ds.getKey());
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    // Add object to the list
                    objectList.add(object);
                }

                callback.apply(objectList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(activity, "An error has occurred while reading from database", Toast.LENGTH_SHORT).show();
            }
        };

        // Add listener to ref
        ref.addValueEventListener(valueEventListener);
    }
}
