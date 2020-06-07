package com.furkank.colordetector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.opengl.GLES20;
import android.graphics.SurfaceTexture;

import com.furkank.colordetector.handler.CameraHandler;
import com.furkank.colordetector.handler.ColorDetectHandler;
import com.furkank.colordetector.handler.IntentHandler;
import com.furkank.colordetector.handler.SessionHandler;
import com.furkank.colordetector.model.UserColor;
import com.google.android.material.snackbar.Snackbar;

import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.IntBuffer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private View pointer = null;
    private DrawerLayout drawer = null;
    private TextView loggedInEmail = null;
    private TextureView cameraView = null;

    private SessionHandler sessionHandler = null;

    private CameraHandler cameraHandler = null;
    private ColorDetectHandler colorDetectHandler = null;

    public boolean readyToCatch = false;
    private Toast readyToCatchToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // Define pointer
        pointer = findViewById(R.id.pointer);

        // Define camera viewer
        cameraView = findViewById(R.id.cameraView);
        cameraView.setSurfaceTextureListener(cameraViewListener);

        // Define session handler
        sessionHandler = new SessionHandler(this);

        // Get camera service
        // Create camera handler
        Object cameraService = getSystemService(Context.CAMERA_SERVICE);
        cameraHandler = new CameraHandler(this, cameraService, cameraView);

        // Color detect handler
        colorDetectHandler = new ColorDetectHandler(this);

        // Define drawer
        drawer = findViewById(R.id.drawer_layout);

        // Define navigation
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Handle tools under menu view
        handleNavigationTools(navigationView);
    }

    // Listen texture view and if it is available
    // then try to open the camera
    protected TextureView.SurfaceTextureListener cameraViewListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            // Open the camera
            cameraHandler.openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            int b[] = new int[50 * (100 + 50)];
            IntBuffer ib = IntBuffer.wrap(b);
            ib.position(0);
            GLES20.glReadPixels(100, 100, 50, 50, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraHandler.openCamera();
        } else {
            Toast.makeText(this, "You need to give CAMERA permission.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When clicked on color catch button
     *
     * @param view
     */
    public void onColorCaught(View view) {
        // If list is not ready yet, then keep user waiting
        if (!readyToCatch) {
            if (readyToCatchToast != null) {
                readyToCatchToast.cancel();
            }
            readyToCatchToast = Toast.makeText(this, "Please, wait for the list to load", Toast.LENGTH_SHORT);
            readyToCatchToast.show();
            return;
        }

        // We need to create an instance of color detect handler
        // each time because we need to each variable within as empty at first
        colorDetectHandler.detect(cameraView, pointer);

        Snackbar
                .make(
                        view,
                        colorDetectHandler.name + " (" + colorDetectHandler.hue + ")",
                        10000
                )
                .setAction(R.string.save, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean loginResult = sessionHandler.checkLogin();
                        if (!loginResult) {
                            return;
                        }

                        UserColor userColor = new UserColor();
                        boolean result = userColor.save(
                                sessionHandler.getUserEmail(),
                                colorDetectHandler.hex,
                                colorDetectHandler.name
                        );

                        if (result) {
                            Toast.makeText(MainActivity.this, R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.not_saved, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    public void onDrawerToggleClicked(View v) {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(GravityCompat.START, true);
    }

    // The following methods is automatically generated

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void handleNavigationTools(NavigationView navigationView) {
        // Nav header
        loggedInEmail = navigationView.getHeaderView(0).findViewById(R.id.loggedInEmail);
        if (sessionHandler.isLoggedIn()) {
            loggedInEmail.setText(sessionHandler.getUserEmail());
        }

        // Nav buttons
        Menu menu = navigationView.getMenu();

        // Login menu item
        MenuItem loginNav = menu.findItem(R.id.nav_login);
        if (sessionHandler.isLoggedIn()) {
            loginNav.setVisible(false);
        }

        // Logout menu item
        MenuItem logoutNav = menu.findItem(R.id.nav_logout);
        if (!sessionHandler.isLoggedIn()) {
            logoutNav.setVisible(false);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            sessionHandler.checkLogin();
        } else if (id == R.id.nav_mycolors) {
            if (sessionHandler.isLoggedIn()) {
                IntentHandler.open(this, MyColorsActivity.class, false);
            } else {
                Toast.makeText(this, R.string.need_login_for_mycolors, Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_logout) {
            sessionHandler.remove();
        } else if (id == R.id.nav_github) {
            goToGithub();
        } else if (id == R.id.nav_about) {
            IntentHandler.open(this, AboutActivity.class, false);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goToGithub() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/fkolcu/color-detector"));
        startActivity(browserIntent);
    }
}
