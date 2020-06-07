package com.furkank.colordetector;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.furkank.colordetector.firebase.FirebaseReadHandler;
import com.furkank.colordetector.firebase.FirebaseWriteHandler;
import com.furkank.colordetector.handler.SessionHandler;
import com.furkank.colordetector.listener.OnSwipeTouchListener;
import com.furkank.colordetector.model.User;
import com.furkank.colordetector.model.UserColor;

import java.util.ArrayList;
import java.util.HashMap;

public class MyColorsActivity extends AppCompatActivity {

    private TextView emptyColorText = null;
    private ProgressBar progressBar = null;

    private ArrayList<UserColor> myColorList = null;
    private LinearLayout myColorsContainer = null;
    private HashMap<Integer, Integer> idMaps = null;

    private SessionHandler sessionHandler = null;
    FirebaseReadHandler<UserColor> firebaseReadHandler = null;
    FirebaseWriteHandler firebaseWriteHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_colors);

        // Initialize hashmap
        idMaps = new HashMap<Integer, Integer>();

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

        // Initialize firebase write handler
        firebaseWriteHandler = new FirebaseWriteHandler();

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
        colorView.setId(View.generateViewId());
        colorView.setTag(userColor.key);
        colorView.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeRight(View view) {
                deleteTheColor((ImageView) view);
            }

            public void onSwipeLeft(View view) {
                copyTheColor((ImageView) view);
            }
        });

        // Create text for color name
        TextView colorText = new TextView(this);
        colorText.setText(userColor.getName() + " (" + userColor.getColor() + ")");
        colorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        colorText.setId(View.generateViewId());
        colorText.setPadding(0, 5, 0, 50);

        // Set ids of image view and text to id maps
        idMaps.put(colorView.getId(), colorText.getId());

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

    private void deleteTheColor(ImageView imageView) {
        int imageViewId = imageView.getId();
        int colorTextId = idMaps.get(imageViewId);

        TextView colorText = findViewById(colorTextId);

        // Get the key in the db
        String key = (String) imageView.getTag();

        // Remove from db
        firebaseWriteHandler.remove("colors", key);

        // Remove from view
        myColorsContainer.removeView(imageView);
        myColorsContainer.removeView(colorText);

        Toast.makeText(MyColorsActivity.this, R.string.color_deleted, Toast.LENGTH_SHORT).show();
    }
}
