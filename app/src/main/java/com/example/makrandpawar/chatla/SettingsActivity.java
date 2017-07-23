package com.example.makrandpawar.chatla;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chootdev.csnackbar.Duration;
import com.chootdev.csnackbar.Type;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.irozon.sneaker.Sneaker;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private static final String OLD_STATUS = "OLD_STATUS";
    private static final int CAMERA_REQUEST_CODE = 199;
    private DatabaseReference mDatabase;
    private TextView txtDisplayName;
    private TextView txtStatus;
    private CircularImageView imgDp;
    private Button btnChangeStatus;
    private Button btnChangeDp;
    private String status;
    private Uri mCropImageUri;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);
        mDatabase.keepSynced(true);

        txtDisplayName = (TextView) findViewById(R.id.settings_txt_displayname);
        txtStatus = (TextView) findViewById(R.id.settings_txt_status);
        imgDp = (CircularImageView) findViewById(R.id.settings_img_dp);
        btnChangeStatus = (Button) findViewById(R.id.settings_btn_status);
        btnChangeDp = (Button) findViewById(R.id.settings_btn_dp);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("displayname").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                txtDisplayName.setText(displayName);
                txtStatus.setText(status);
                if (!image.equals("default")) {
                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(imgDp);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra(OLD_STATUS, status);
                startActivity(statusIntent);
            }
        });

        btnChangeDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    CropImage.startPickImageActivity(SettingsActivity.this);

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                    } else {
                        Sneaker.with(SettingsActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Permission Denied").setMessage("Grant CAMERA permissions to use camera for picker").sneakError();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("statusonline").setValue("true");
            }
        },2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("statusonline").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(SettingsActivity.this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                File thumbFile = new File(resultUri.getPath());
                File thumbImage = null;
                try {
                    thumbImage = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).compressToFile(thumbFile);
                } catch (IOException e) {
                    Sneaker.with(SettingsActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error!").setMessage(e.getMessage()).sneakError();
                }


                imgDp.setImageURI(resultUri);

                mStorageRef = FirebaseStorage.getInstance().getReference();
                mStorageRef.child("profile_images").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("dp.jpg").putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        mDatabase.child("image").setValue(downloadUrl);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(SettingsActivity.this, "Failed to change image", Toast.LENGTH_SHORT).show();
                        Sneaker.with(SettingsActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error!").setMessage(e.getMessage()).sneakError();

                    }
                });

                mStorageRef.child("profile_images").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("thumb.jpg").putFile(Uri.fromFile(thumbImage)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Sneaker.with(SettingsActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("ThumbError!").setMessage(e.getMessage()).sneakError();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        mDatabase.child("thumb_image").setValue(downloadUrl);
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(500, 500)
                .start(this);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                // Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
                Sneaker.with(SettingsActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Permission Denied").setMessage("Cancelling, required permissions are not granted!").sneakError();
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                CropImage.startPickImageActivity(SettingsActivity.this);
            } else {
                //Toast.makeText(this, "Permission denied. Camera option not available", Toast.LENGTH_SHORT).show();
                Sneaker.with(SettingsActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Permission Denied").setMessage("Camera option not available!").sneakError();
                CropImage.startPickImageActivity(SettingsActivity.this);
            }
        }
    }
}
