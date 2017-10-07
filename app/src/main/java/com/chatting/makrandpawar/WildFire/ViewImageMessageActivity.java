package com.chatting.makrandpawar.WildFire;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.makrandpawar.chatla.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

public class ViewImageMessageActivity extends AppCompatActivity {
private String message;
    private ImageView imageView;
    private Toolbar toolbar;

    // full screen image viewing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_message);

        toolbar = (Toolbar) findViewById(R.id.viewimagemessage_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        message = getIntent().getStringExtra("IMAGEMESSAGE");
        imageView = (ImageView) findViewById(R.id.viewimagemessage_imageview);

        Picasso.with(this).load(message).error(R.drawable.default_image_sad).into(imageView);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent backIntent = new Intent(ViewImageMessageActivity.this,ChatActivity.class);
            backIntent.putExtra("CHATWITH",getIntent().getStringExtra("CHATWITHTOIMAGE"));
            backIntent.putExtra("CHATROOM",getIntent().getStringExtra("CHATROOMTOIMAGE"));
            startActivity(backIntent);
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(ViewImageMessageActivity.this,ChatActivity.class);
        backIntent.putExtra("CHATWITH",getIntent().getStringExtra("CHATWITHTOIMAGE"));
        backIntent.putExtra("CHATROOM",getIntent().getStringExtra("CHATROOMTOIMAGE"));
        startActivity(backIntent);
        finish();
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
