package com.example.tokopari;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);

        // Check if the user is logged in
        if (!sharedPrefManager.isLoggedIn()) {
            // If not logged in, redirect to the LoginAndRegister activity
            Intent intent = new Intent(MainActivity.this, LoginAndRegisterActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // If logged in, set up the main view
        bottomNavigationView = findViewById(R.id.bottomNavView);
        frameLayout = findViewById(R.id.frameLayout);

        ColorStateList iconColorStateList = ContextCompat.getColorStateList(this, R.color.nav_icon_color_selector);
        bottomNavigationView.setItemIconTintList(iconColorStateList);

        // Set listener for navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Fragment selectedFragment = null; // Fragment to be loaded

                // Using if-else to determine which fragment to load
                if (itemId == R.id.navHome) {
                    selectedFragment = new Home();
                } else if (itemId == R.id.navCart) {
                    selectedFragment = new Cart();
                } else if (itemId == R.id.navProfile) {
                    selectedFragment = new Profile();
                }

                // Load the selected fragment
                loadFragment(selectedFragment);
                return true;
            }
        });

        // Load the initial fragment (Home) if already logged in
        loadFragment(new Home());
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, fragment);
            fragmentTransaction.commit();
        }
    }
}
