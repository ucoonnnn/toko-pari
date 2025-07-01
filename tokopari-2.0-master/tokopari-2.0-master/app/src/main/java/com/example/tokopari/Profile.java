package com.example.tokopari;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.app.AlertDialog;
import android.widget.VideoView;

import static android.app.Activity.RESULT_OK;

public class Profile extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private Uri imageUri;
    private ImageButton photoProfile;

    private EditText etName, etUsername, etEmail;
    private Button buttonLogout, buttonSave;
    private SharedPreferences sharedPreferences;
    private TextInputLayout nameInputLayout, usernameInputLayout, emailInputLayout;
    private TextView textTokopari;
    private VideoView videoView;
    private FrameLayout frameLayout;
    private android.util.Log Log;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inisialisasi komponen
        etName = view.findViewById(R.id.etName);
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        buttonLogout = view.findViewById(R.id.button);
        buttonSave = view.findViewById(R.id.buttonSave);
        nameInputLayout = view.findViewById(R.id.nameInputLayout);
        usernameInputLayout = view.findViewById(R.id.usernameInputLayout);
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        photoProfile = view.findViewById(R.id.photoProfile);
        textTokopari = view.findViewById(R.id.textTokopari);
        videoView = view.findViewById(R.id.videoTokopari);
        frameLayout = view.findViewById(R.id.frameLayout);

        videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("ProfileFragment", "Surface created, starting video...");

                Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.tokopari_vid);
                videoView.setVideoURI(videoUri);
                videoView.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                videoView.stopPlayback();
            }
        });

        textTokopari.setOnClickListener(v -> {
            Log.d("ProfileFragment", "Text clicked, hiding text and showing video");

            textTokopari.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.VISIBLE);

            Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.tokopari_vid);
            videoView.setVideoURI(videoUri);

            videoView.setOnPreparedListener(mp -> {
                Log.d("ProfileFragment", "Video prepared, starting playback...");
                videoView.start();
            });

            videoView.setOnCompletionListener(mp -> {
                Log.d("ProfileFragment", "Video playback completed");

                getActivity().runOnUiThread(() -> {
                    videoView.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.GONE);

                    textTokopari.setVisibility(View.VISIBLE);
                });
            });
        });


        loadSavedData();
        setInitialHints();

        photoProfile.setOnClickListener(v -> showImagePickerDialog());

        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                nameInputLayout.setHint("Name");
            } else {
                nameInputLayout.setHint("Enter Name");
            }
        });

        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                usernameInputLayout.setHint("Username");
            } else {
                usernameInputLayout.setHint("Enter Username");
            }
        });

        etName.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                clearFocusFromFields();
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        etUsername.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                clearFocusFromFields();
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        buttonSave.setOnClickListener(v -> saveData());
        buttonLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void showImagePickerDialog() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                openGallery();
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Profile Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Profile picture taken from camera");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
            } else if (requestCode == CAMERA_REQUEST_CODE && imageUri != null) {
            }

            if (imageUri != null) {
                photoProfile.setImageURI(imageUri);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profileImageUri", imageUri.toString());
                editor.apply();
            }
        }
    }

    private void saveData() {
        String name = etName.getText().toString();
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();

        SharedPrefManager sharedPrefManager = new SharedPrefManager(getActivity());
        sharedPrefManager.setUserName(name);
        sharedPrefManager.setUserUsername(username);
        sharedPrefManager.setUserEmail(email);
    }

    private void loadSavedData() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(getActivity());

        String savedName = sharedPrefManager.getUserName();
        String savedUsername = sharedPrefManager.getUserUsername();
        String savedEmail = sharedPrefManager.getUserEmail();

        etName.setText(savedName);
        etUsername.setText(savedUsername);
        etEmail.setText(savedEmail);

        String savedImageUri = sharedPreferences.getString("profileImageUri", null);
        if (savedImageUri != null) {
            imageUri = Uri.parse(savedImageUri);
            photoProfile.setImageURI(imageUri);
        }
    }

    private void setInitialHints() {
        if (!etName.getText().toString().isEmpty()) {
            nameInputLayout.setHint("Name");
        }
        if (!etUsername.getText().toString().isEmpty()) {
            usernameInputLayout.setHint("Username");
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        SharedPrefManager sharedPrefManager = new SharedPrefManager(getActivity());
        sharedPrefManager.logout();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginAndRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear the activity stack
        startActivity(intent);

        getActivity().finishAffinity();
    }

    private void clearFocusFromFields() {
        etName.clearFocus();
        etUsername.clearFocus();

        etName.setFocusable(false);
        etUsername.setFocusable(false);

        View nonEditableView = getView().findViewById(R.id.buttonSave);
        nonEditableView.requestFocus();

        etName.setFocusable(true);
        etUsername.setFocusable(true);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}