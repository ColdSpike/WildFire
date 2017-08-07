package com.chatting.makrandpawar.WildFire;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.makrandpawar.chatla.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.irozon.sneaker.Sneaker;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout txtEmail;
    private TextInputLayout txtPassword;
    private Button btnLogin;
    private Toolbar mToolBar;
    private FirebaseAuth mAuth;
    private ProgressDialog loginProgress;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolBar = (Toolbar) findViewById(R.id.login_tool_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.login_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        loginProgress = new ProgressDialog(this);
        loginProgress.setTitle("Logging in to your Account");
        loginProgress.setMessage("This will only take a moment");
        loginProgress.setCanceledOnTouchOutside(false);
        loginProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        txtEmail = (TextInputLayout) findViewById(R.id.login_txt_email);
        txtPassword = (TextInputLayout) findViewById(R.id.login_txt_password);
        btnLogin = (Button) findViewById(R.id.login_btn_register);
        forgotPassword = (TextView) findViewById(R.id.loginactivity_forgotpassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getEditText().getText().toString();
                String password = txtPassword.getEditText().getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    loginProgress.show();
                    loginUser(email, password);
                } else {
                    Sneaker.with(LoginActivity.this).setTitle("Warning").setMessage("Please fill all the fields!").setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).sneakWarning();
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(LoginActivity.this)
                        .title("RESET PASSWORD?")
                        .content("A Link To Reset Your Password Will be Sent To Your Email Id")
                        .input("Your Email...", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (!input.toString().equals("")) {
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(input.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                Sneaker.with(LoginActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Success!").setMessage("Verification email sent successfully.").setDuration(4000).sneakSuccess();
                                            else
                                                Sneaker.with(LoginActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Success!").setMessage(task.getException().getMessage()).setDuration(5000).sneakError();
                                        }
                                    });
                                } else {
                                    Sneaker.with(LoginActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error").setMessage("Enter valid email id!!").setDuration(3000).sneakError();
                                }
                            }
                        })
                        .positiveText("RESET")
                        .negativeText("CANCEL")
                        .show();
            }
        });

    }

    private void loginUser(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                loginProgress.dismiss();
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }else {
                                loginProgress.dismiss();
                                Sneaker.with(LoginActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error").setMessage("A Verification Email has been sent to your Email Id. Please verify to login.").setDuration(15000).sneakError();
                                FirebaseAuth.getInstance().signOut();
                            }
                        } else {
                            loginProgress.hide();
                            Sneaker.with(LoginActivity.this).setTitle("Error!").setMessage(task.getException().getMessage()).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).sneakError();
                        }
                    }
                });
            }
        });
    }
}
