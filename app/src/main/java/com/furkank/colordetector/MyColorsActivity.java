package com.furkank.colordetector;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.furkank.colordetector.firebase.FirebaseReadHandler;

import java.util.ArrayList;

public class MyColorsActivity extends AppCompatActivity {

    private TextView emptyColorText = null;
    private ProgressBar progressBar = null;
    private ArrayList<UserColor> myColorList = null;
    FirebaseReadHandler<UserColor> firebaseReadHandler = null;
    private SessionHandler sessionHandler = null;
    private LinearLayout myColorsContainer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_colors);

        // Define container layout
        myColorsContainer = findViewById(R.id.myColorsContainer);

        // Define progressbar
        progressBar = findViewById(R.id.loader);

        // Define empty color text
        emptyColorText = findViewById(R.id.emptyColorText);

        // Initialize session handler
        sessionHandler = new SessionHandler(this);

        // Initialize the color list
        myColorList = new ArrayList<UserColor>();

        // Initialize firebase read handler
        firebaseReadHandler = new FirebaseReadHandler<UserColor>(UserColor.class, this);

        // Get my colors
        firebaseReadHandler.read("colors", this::readCallback);
    }

    private Integer readCallback(ArrayList<UserColor> colorList) {
        // Read is done, remove loader
        progressBar.setVisibility(View.INVISIBLE);

        // If list is empty, show message
        if (colorList.isEmpty()) {
            Toast.makeText(this, "You haven't saved any color yet", Toast.LENGTH_SHORT).show();
            return null;
        }

        User currentUser = new User(sessionHandler.getUserEmail(), "");

        for (UserColor userColor : colorList) {
            if (userColor.getEmail().equals(currentUser.getEmail())) {
                // Add color to my list
                myColorList.add(userColor);

                // And list it on the view
                addNewColorToView(userColor);
            }
        }

        // If my color list is empty, show message
        if (myColorList.isEmpty()) {
            emptyColorText.setVisibility(View.VISIBLE);
        } else {
            myColorsContainer.setWeightSum(myColorList.size());
        }

        return null;
    }

    private void addNewColorToView(UserColor userColor) {
        // Create a color view
        ImageView colorView = new ImageView(this);
        colorView.setMinimumHeight(150);
        colorView.setBackgroundColor(Color.parseColor(userColor.getColor()));

        // Create text for color name
        TextView colorText = new TextView(this);
        colorText.setText(userColor.getName() + " (" + userColor.getColor() + ")");
        colorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        colorText.setPadding(0, 5, 0, 50);

        // Add item view to container view
        myColorsContainer.addView(colorView);
        myColorsContainer.addView(colorText);
    }
}
