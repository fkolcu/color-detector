package com.furkank.colordetector;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.opengl.GLES20;
import android.graphics.SurfaceTexture;

import com.furkank.colordetector.firebase.FirebaseWriteHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.nio.IntBuffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private View pointer = null;
    private TextureView cameraView = null;

    private CameraHandler cameraHandler = null;
    private ColorDetectHandler colorDetectHandler = null;

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

        // Get camera service
        // Create camera handler
        Object cameraService = getSystemService(Context.CAMERA_SERVICE);
        cameraHandler = new CameraHandler(this, cameraService, cameraView);

        // Color detect handler
        colorDetectHandler = new ColorDetectHandler(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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
                        UserColor userColor = new UserColor();
                        userColor.setColor(colorDetectHandler.hex);
                        userColor.setName(colorDetectHandler.name);

                        FirebaseWriteHandler fbWrite = new FirebaseWriteHandler();
                        boolean result = fbWrite.add("colors", userColor);
                        if(result){
                            Toast.makeText(MainActivity.this, R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, R.string.not_saved, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
