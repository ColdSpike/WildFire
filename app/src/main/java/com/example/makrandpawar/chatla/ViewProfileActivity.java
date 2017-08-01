package com.example.makrandpawar.chatla;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.irozon.sneaker.Sneaker;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ViewProfileActivity extends AppCompatActivity {

    private TextView name;
    private TextView status;
    private ImageView image;
    private Button removeFriend;
    private DatabaseReference mDatabase;
    private String displayName;
    private String userStatus;
    private String dp;
    private TextView numberOfFriends;
    private DatabaseReference friendSinceReference;
    private TextView friendSince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        name = (TextView) findViewById(R.id.viewprofile_txt_displayname);
        status = (TextView) findViewById(R.id.viewprofile_txt_status);
        image = (ImageView) findViewById(R.id.viewprofile_img_dp);
        removeFriend = (Button) findViewById(R.id.viewprofile_btn_removefriend);
        numberOfFriends = (TextView) findViewById(R.id.viewprofile_txt_numberoffriends);
        friendSince = (TextView) findViewById(R.id.viewprofile_friendsince);


        final String userRef = getIntent().getStringExtra("VIEWPROFILE_USERREF");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userRef);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                displayName = dataSnapshot.child("displayname").getValue().toString();
                dp = dataSnapshot.child("image").getValue().toString();
                userStatus = dataSnapshot.child("status").getValue().toString();

                displayDetails(displayName, dp, userStatus);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference checkFriend = FirebaseDatabase.getInstance().getReference().child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Query query = checkFriend.orderByKey().equalTo(userRef);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String totalFriends = dataSnapshot.getChildrenCount() + " Total Friends";
                numberOfFriends.setText(totalFriends);
                if (dataSnapshot.exists()) {
                    removeFriend.setVisibility(View.VISIBLE);
                } else {
                    removeFriend.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        removeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(ViewProfileActivity.this)
                        .title("Remove Friend " + name.getText().toString() + " ?")
                        .positiveText("REMOVE")
                        .negativeText("CANCEL")
                        .titleColor(Color.RED)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                DatabaseReference removeFriendRef = FirebaseDatabase.getInstance().getReference().child("friends");

                                removeFriendRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(userRef).setValue(null);
                                removeFriendRef.child(userRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);

                                Sneaker.with(ViewProfileActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                        .setTitle("Success")
                                        .setMessage("Your are no longer friends with " + name.getText().toString() + " !")
                                        .setDuration(4000)
                                        .sneakSuccess();
                            }
                        })
                        .show();
            }
        });

        friendSinceReference = FirebaseDatabase.getInstance().getReference().child("friendsdata").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(userRef);
        friendSinceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String timestamp = dataSnapshot.child("friendsince").getValue().toString();
                Date date = new Date(Long.parseLong(timestamp));
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a");
                format.setTimeZone(TimeZone.getDefault());
                String time = format.format(date);
                friendSince.setText("Friend Since: " + time);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    private void displayDetails(String displayName, final String dp, String userStatus) {
        name.setText(displayName);
        status.setText(userStatus);
        if (!image.equals("default")) {
            Picasso.with(ViewProfileActivity.this).load(dp).placeholder(R.drawable.default_avatar).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    Picasso.with(ViewProfileActivity.this).load(dp).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(image);
                }
            });
        }
    }


}
