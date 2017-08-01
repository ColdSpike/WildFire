package com.example.makrandpawar.chatla;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chootdev.csnackbar.Duration;
import com.chootdev.csnackbar.Snackbar;
import com.chootdev.csnackbar.Type;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.irozon.sneaker.Sneaker;

public class StatusActivity extends AppCompatActivity {

    private static final String OLD_STATUS = "OLD_STATUS";
    private Toolbar mToolBar;
    private TextInputLayout txtStatus;
    private Button btnSave;
    private ProgressDialog statusProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolBar = (Toolbar) findViewById(R.id.status_tool_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.status_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtStatus = (TextInputLayout) findViewById(R.id.status_txt_status);
        btnSave = (Button) findViewById(R.id.status_btn_save);

        String oldStatus = getIntent().getStringExtra(OLD_STATUS);

        txtStatus.getEditText().setText(oldStatus);

        statusProgress = new ProgressDialog(this);
        statusProgress.setTitle("Updating Status");
        statusProgress.setMessage("This will only take a moment");
        statusProgress.setCanceledOnTouchOutside(false);
        statusProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusProgress.show();

                String newStatus = txtStatus.getEditText().getText().toString();
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);
                mDatabase.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            statusProgress.dismiss();
                            Sneaker.with(StatusActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Success").setMessage("Status Update Successful").sneakSuccess();
                        }else {
                            Sneaker.with(StatusActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error!").setMessage("Status Update Failed. Try Again").sneakError();
                        }
                    }
                });


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("statusonline").setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("statusonline").setValue(ServerValue.TIMESTAMP);
    }
}
