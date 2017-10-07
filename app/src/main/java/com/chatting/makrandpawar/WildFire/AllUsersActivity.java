package com.chatting.makrandpawar.WildFire;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chatting.makrandpawar.WildFire.model.AllUsersModelClass;
import com.example.makrandpawar.chatla.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.irozon.sneaker.Sneaker;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<AllUsersModelClass, AllUsersViewHolder> mRecyclerAdapter;
    private int mExpandedPosition = -1;
    private String selfName;
    private ValueEventListener getNameListener;
    private String selfImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolBar = (Toolbar) findViewById(R.id.allusers_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.allusers_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.allusers_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //get database reference and allow offline sync
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabase.keepSynced(true);

        //get name and image of the current user
        getNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfName = dataSnapshot.child("displayname").getValue().toString();
                selfImage = dataSnapshot.child("thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addListenerForSingleValueEvent(getNameListener);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabase.removeEventListener(getNameListener);

        mRecyclerAdapter = new FirebaseRecyclerAdapter<AllUsersModelClass, AllUsersViewHolder>(
                AllUsersModelClass.class,
                R.layout.allusersactivity_singleuser,
                AllUsersViewHolder.class,
                FirebaseDatabase.getInstance().getReference().child("users")) {

            @Override
            protected void populateViewHolder(final AllUsersViewHolder viewHolder, AllUsersModelClass model, final int position) {
                viewHolder.setName(model.getDisplayname());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(getApplicationContext(), model.getThumbImage());

                //check if the user has sent friend request
                final String friendRef = mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/users/", "");

                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("requests");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                            viewHolder.invite.setText("Cancel Request");
                            viewHolder.invite.setVisibility(View.VISIBLE);

                            viewHolder.invite.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new MaterialDialog.Builder(AllUsersActivity.this)
                                            .title("Cancel Friend Request to " + viewHolder.displayName.getText().toString() + "?")
                                            .positiveText("OK")
                                            .negativeText("CANCEL")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    String friendRef = mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/users/", "");
                                                    FirebaseDatabase.getInstance().getReference().child("requests").child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);
                                                    // FirebaseDatabase.getInstance().getReference().updateChildren();
                                                    Sneaker.with(AllUsersActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                                            .setTitle("Success")
                                                            .setMessage("Friend Request Cancelled for " + viewHolder.displayName.getText().toString() + "!")
                                                            .setDuration(4000)
                                                            .sneakSuccess();
                                                    mRecyclerAdapter.notifyItemChanged(position);
                                                }
                                            })
                                            .show();
                                }
                            });
                        } else {
                            //check if user is already a friend
                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("friends");
                            databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friendRef).exists()) {
                                        viewHolder.invite.setVisibility(View.GONE);
                                    } else
                                        viewHolder.invite.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onBindViewHolder(final AllUsersViewHolder viewHolder, final int position) {
                super.onBindViewHolder(viewHolder, position);

                final boolean isExpanded = position == mExpandedPosition;

                viewHolder.expand.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                viewHolder.cardView.setCardElevation(isExpanded ? 100 : 10);
                if (!mRecyclerAdapter.getRef(position).toString().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if (isExpanded) expander(viewHolder.expand);
                    viewHolder.relativeLayout.setActivated(isExpanded);

                    viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mExpandedPosition = isExpanded ? -1 : position;
                            notifyDataSetChanged();
                        }
                    });

                    viewHolder.viewProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent viewProfileIntent = new Intent(AllUsersActivity.this, ViewProfileActivity.class);
                            viewProfileIntent.putExtra("VIEWPROFILE_USERREF", mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/users/", ""));
                            startActivity(viewProfileIntent);
                        }
                    });

                    viewHolder.invite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialDialog.Builder(AllUsersActivity.this)
                                    .title("Send " + viewHolder.displayName.getText().toString() + " a friend Request?")
                                    .positiveText("SEND")
                                    .negativeText("CANCEL")
                                    .input("Send a greeting message...", "", new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                            new SendRequest().execute(mRecyclerAdapter.getRef(position).toString(),
                                                    input.toString(),
                                                    selfName,
                                                    selfImage,
                                                    viewHolder.displayName.getText().toString(),
                                                    String.valueOf(position));

                                        }
                                    })
                                    .inputRange(2, 50)
                                    .show();
                        }
                    });
                }
            }
        };

        mRecyclerView.setAdapter(mRecyclerAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("statusonline").setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("statusonline").setValue(ServerValue.TIMESTAMP);
    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView displayName;
        TextView userStatus;
        CircularImageView dp;
        RelativeLayout expand;
        CardView cardView;
        RelativeLayout relativeLayout;
        TextView invite;
        TextView viewProfile;
        String userImageForActivity;

        public AllUsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            viewProfile = (TextView) mView.findViewById(R.id.allusers_viewprofile);
            invite = (TextView) mView.findViewById(R.id.allusers_invite);
            expand = (RelativeLayout) mView.findViewById(R.id.allusers_expandedrelativelayout);
            cardView = (CardView) mView.findViewById(R.id.alluser_cardview);
            relativeLayout = (RelativeLayout) mView.findViewById(R.id.allusers_RL);
        }

        public void setName(String name) {
            displayName = (TextView) mView.findViewById(R.id.allusers_name);
            displayName.setText(name);
        }

        public void setStatus(String status) {
            userStatus = (TextView) mView.findViewById(R.id.allusers_status);
            userStatus.setText(status);
        }

        public void setImage(final Context context, final String image) {
            dp = (CircularImageView) mView.findViewById(R.id.allusers_image);
            userImageForActivity = image;
            if (!image.equals("default")) {

                Picasso.with(context).load(image).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).centerCrop().resize(64, 64).into(dp);

            }
        }
    }
    //send friend request
    private class SendRequest extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {
            String friendRef = params[0].replace("https://chatla-1a62a.firebaseio.com/users/", "");
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map requestMap = new HashMap();
            requestMap.put("requests/" + friendRef + "/" + myUid + "/message", params[1]);
            requestMap.put("requests/" + friendRef + "/" + myUid + "/name", params[2]);
            requestMap.put("requests/" + friendRef + "/" + myUid + "/image", params[3]);

            mDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (null == databaseError) {
                        Sneaker.with(AllUsersActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                .setTitle("Success")
                                .setMessage("Friend Request Sent Successfully to " + params[4] + " !")
                                .setDuration(4000)
                                .sneakSuccess();
                        mRecyclerAdapter.notifyItemChanged(Integer.parseInt(params[5]));
                    } else
                        Sneaker.with(AllUsersActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                .setTitle("Error Sending Friend Request!")
                                .setMessage(databaseError.getMessage())
                                .setDuration(4000)
                                .sneakError();
                }
            });

            return null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerAdapter.cleanup();
    }

    //animate card expansion
    public static void expander(final View v) {
        v.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RecyclerView.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (targetHeight * 10 / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
