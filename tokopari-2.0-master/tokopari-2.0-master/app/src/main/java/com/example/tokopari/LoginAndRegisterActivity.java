package com.example.tokopari;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.example.tokopari.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginAndRegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonLogin, buttonRegister;
    private TextView textViewSwitchToRegister, textViewSwitchToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_register);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewSwitchToRegister = findViewById(R.id.textViewSwitchToRegister);
        textViewSwitchToLogin = findViewById(R.id.textViewSwitchToLogin);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // SharedPreferences for storing email
        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
        String savedEmail = sharedPrefManager.getUserEmail();
        if (savedEmail != null && !savedEmail.isEmpty()) {
            editTextEmail.setText(savedEmail);  // Automatically fill the email field
        }

        // Initially show login fields
        showLoginFields();

        buttonLogin.setOnClickListener(v -> login());
        buttonRegister.setOnClickListener(v -> register());

        textViewSwitchToRegister.setOnClickListener(v -> showRegistrationFields());
        textViewSwitchToLogin.setOnClickListener(v -> showLoginFields());
    }

    private void showRegistrationFields() {
        editTextConfirmPassword.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.GONE);
        textViewSwitchToLogin.setVisibility(View.VISIBLE);
        textViewSwitchToRegister.setVisibility(View.GONE);
    }

    private void showLoginFields() {
        editTextConfirmPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.VISIBLE);
        textViewSwitchToLogin.setVisibility(View.GONE);
        textViewSwitchToRegister.setVisibility(View.VISIBLE);
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    // Login functionality with Firebase Authentication
    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showErrorDialog("Please enter your email.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showErrorDialog("Please enter your password.");
            return;
        }

        if (!isValidEmail(email)) {
            showErrorDialog("Please enter a valid email.");
            return;
        }

        // Firebase Authentication for login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase Authentication successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Store email in SharedPreferences
                            SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
                            sharedPrefManager.setLoggedIn(true);  // Set login status
                            sharedPrefManager.setUserEmail(user.getEmail()); // Store email in SharedPreferences

                            Log.d("LoginActivity", "Email disimpan setelah login: " + email);

                            // Redirect to MainActivity or the home screen
                            Intent intent = new Intent(LoginAndRegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();  // Close Login/Register Activity
                        }
                    } else {
                        showErrorDialog("Login failed. Invalid credentials.");
                    }
                });
    }

    // Registration functionality with Firebase Authentication
    private void register() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showErrorDialog("Please enter your email.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showErrorDialog("Please enter your password.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorDialog("Passwords do not match.");
            return;
        }

        if (!isValidEmail(email)) {
            showErrorDialog("Please enter a valid email.");
            return;
        }

        if (!isValidPassword(password)) {
            showErrorDialog("Password must be at least 8 characters and contain at least one uppercase letter.");
            return;
        }

        // Firebase Authentication for registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase Authentication successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Store email in SharedPreferences
                            SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
                            sharedPrefManager.setUserEmail(user.getEmail()); // Store email in SharedPreferences

                            // Show successful registration message
                            Toast.makeText(this, "Registration successful! You can now log in.", Toast.LENGTH_SHORT).show();

                            showLoginFields(); // Switch to login screen after successful registration
                        }
                    } else {
                        showErrorDialog("Registration failed. Please try again.");
                    }
                });
    }

    private boolean isValidEmail(String email) {
        // Check if the email contains @gmail.com and matches email patterns
        return email.contains("@gmail.com") && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        // Check if password is at least 8 characters and contains at least one uppercase letter
        return password.length() >= 8 && password.matches(".*[A-Z].*");
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if the user is already logged in using Firebase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, redirect to MainActivity
            SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
            sharedPrefManager.setLoggedIn(true);
            sharedPrefManager.setUserEmail(currentUser.getEmail());

            Intent intent = new Intent(LoginAndRegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close Login/Register Activity
        } else {
            // User is not logged in, show the login screen
            String savedEmail = new SharedPrefManager(this).getUserEmail();
            if (savedEmail != null && !savedEmail.isEmpty()) {
                editTextEmail.setText(savedEmail);  // Automatically fill the email field
            }
        }
    }

    // Logout functionality
    private void logout() {
        // Sign out from Firebase Auth
        mAuth.signOut();

        // Clear SharedPreferences
        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
        sharedPrefManager.setLoggedIn(false);  // Set login status to false
        sharedPrefManager.setUserEmail("");    // Clear stored email

        // Redirect to LoginAndRegisterActivity (without referencing MainActivity)
        Intent intent = new Intent(this, LoginAndRegisterActivity.class);
        startActivity(intent);
        finish();  // Close the current activity
    }
}
