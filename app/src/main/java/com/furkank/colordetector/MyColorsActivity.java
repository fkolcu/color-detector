package com.furkank.colordetector;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.furkank.colordetector.firebase.FirebaseReadHandler;
import com.furkank.colordetector.listener.OnSwipeTouchListener;

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
        colorView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                copyTheColor((ImageView) v);
                return super.onTouch(v, event);
            }

            public void onSwipeRight() {
                Toast.makeText(MyColorsActivity.this, "right", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeLeft() {
                Toast.makeText(MyColorsActivity.this, "left", Toast.LENGTH_SHORT).show();
            }
        });

        // Create text for color name
        TextView colorText = new TextView(this);
        colorText.setText(userColor.getName() + " (" + userColor.getColor() + ")");
        colorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        colorText.setPadding(0, 5, 0, 50);

        // Add item view to container view
        myColorsContainer.addView(colorView);
        myColorsContainer.addView(colorText);
    }

    /**
     * Copies the hex color of touched color
     *
     * @param v
     */
    private void copyTheColor(ImageView v) {
        ColorDrawable d = (ColorDrawable) v.getBackground();
        String hexColor = String.format("#%06X", (0xFFFFFF & d.getColor()));

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("color", hexColor);
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(MyColorsActivity.this, R.string.copied, Toast.LENGTH_SHORT).show();
    }
}
