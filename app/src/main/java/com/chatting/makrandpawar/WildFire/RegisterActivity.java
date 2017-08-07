package com.chatting.makrandpawar.WildFire;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.makrandpawar.chatla.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.irozon.sneaker.Sneaker;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG ="RegisterActivity" ;
    private TextInputLayout txtDisplayName;
    private TextInputLayout txtEmail;
    private TextInputLayout txtPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private ProgressDialog registerProgress;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolBar = (Toolbar) findViewById(R.id.register_tool_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.register_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        registerProgress = new ProgressDialog(this);
        registerProgress.setTitle("Creating your Account");
        registerProgress.setMessage("This will only take a moment");
        registerProgress.setCanceledOnTouchOutside(false);
        registerProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        txtDisplayName = (TextInputLayout) findViewById(R.id.register_txt_displayname);
        txtEmail = (TextInputLayout) findViewById(R.id.register_txt_email);
        txtPassword = (TextInputLayout) findViewById(R.id.register_txt_password);
        btnRegister = (Button) findViewById(R.id.register_btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = txtDisplayName.getEditText().getText().toString();
                String email = txtEmail.getEditText().getText().toString();
                String password = txtPassword.getEditText().getText().toString();

                if (!displayName.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    registerProgress.show();
                    registerUser(displayName, email, password);
                }else {
                    Sneaker.with(RegisterActivity.this).setTitle("Warning").setMessage("Please fill all the fields!").setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).sneakWarning();
                }
            }
        });

         final TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length
                if (s.length()>20){
                    txtDisplayName.setHint("DISPLAY NAME CAN NOT EXCEED THE LIMIT");
                    btnRegister.setEnabled(false);
                }else{
                    txtDisplayName.setHint("Display Name");
                    btnRegister.setEnabled(true);
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        txtDisplayName.getEditText().addTextChangedListener(mTextEditorWatcher);

    }

    private void registerUser(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            registerProgress.hide();
                            Sneaker.with(RegisterActivity.this).setTitle("Error!").setMessage(task.getException().getMessage()).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).sneakError();
                        }else {
                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (task.isSuccessful()){
                                        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);

                                        final Uri defaultImage = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                                "://" + getResources().getResourcePackageName(R.drawable.default_avatar)
                                                + '/' + getResources().getResourceTypeName(R.drawable.default_avatar)
                                                + '/' + getResources().getResourceEntryName(R.drawable.default_avatar) );

                                        FirebaseStorage.getInstance().getReference().child("profile_images").child(currentUid).child("dp.jpg").putFile(defaultImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                final String imageUrl = taskSnapshot.getDownloadUrl().toString();
                                                FirebaseStorage.getInstance().getReference().child("profile_images").child(currentUid).child("thumb.jpg").putFile(defaultImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        HashMap<String,String> userMap = new HashMap<String, String>();
                                                        userMap.put("displayname",displayName.trim());
                                                        userMap.put("status","Hi! I'm Using WildFire :)");
                                                        userMap.put("image",imageUrl);
                                                        userMap.put("thumb_image",taskSnapshot.getDownloadUrl().toString());
                                                        userMap.put("tokenid", FirebaseInstanceId.getInstance().getToken());

                                                        mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    registerProgress.dismiss();
                                                                    Sneaker.with(RegisterActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Success").setMessage("A Verification Email has been sent to your Email Id. Please verify to login.").setDuration(15000).sneakSuccess();
                                                                    FirebaseAuth.getInstance().signOut();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }else {
                                        Sneaker.with(RegisterActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error!").setMessage(task.getException().getMessage()).setDuration(5000).sneakError();
                                    }
                                }
                            });
                        }

                    }
                });
    }
}
