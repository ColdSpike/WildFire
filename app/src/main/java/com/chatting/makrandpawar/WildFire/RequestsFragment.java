package com.chatting.makrandpawar.WildFire;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class RequestsFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<UsersRequestFragmentModelClass, RequestFragmentViewHolder> mRecyclerAdapter;
    private DatabaseReference mDatabase;
    private int mExpandedPosition = -1;
    private ValueEventListener getNameListener;
    private String selfName;
    private String selfImage;
    private CircularImageView noRequestImage;
    private TextView noRequestTxt;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfName = dataSnapshot.child("displayname").getValue().toString();
                selfImage = dataSnapshot.child("image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(getNameListener);
        mDatabase.keepSynced(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_requests, container, false);

        mDatabase.removeEventListener(getNameListener);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.requestfragment_recyclerview);
        noRequestImage = (CircularImageView) rootView.findViewById(R.id.requestfragment_norequest_image);
        noRequestTxt = (TextView) rootView.findViewById(R.id.requestfragment_norequest_text);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("requests").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

        mRecyclerAdapter = new FirebaseRecyclerAdapter<UsersRequestFragmentModelClass, RequestFragmentViewHolder>(
                UsersRequestFragmentModelClass.class,
                R.layout.requestfragment_singleuser,
                RequestFragmentViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                if (mRecyclerAdapter.getItemCount() == 0) {
                    noRequestImage.setVisibility(View.VISIBLE);
                    noRequestTxt.setVisibility(View.VISIBLE);
                } else {
                    noRequestImage.setVisibility(View.GONE);
                    noRequestTxt.setVisibility(View.GONE);
                }
            }

            @Override
            protected void populateViewHolder(RequestFragmentViewHolder viewHolder, UsersRequestFragmentModelClass model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setMessage(model.getMessage());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
            }

            @Override
            public void onBindViewHolder(final RequestFragmentViewHolder viewHolder, final int position) {
                super.onBindViewHolder(viewHolder, position);

                final boolean isExpanded = position == mExpandedPosition;

                viewHolder.expand.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                viewHolder.cardView.setCardElevation(isExpanded ? 100 : 10);
                if (isExpanded) expander(viewHolder.expand);
                viewHolder.relativeLayout.setActivated(isExpanded);

                viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mExpandedPosition = isExpanded ? -1 : position;
                        notifyDataSetChanged();
                    }
                });

                viewHolder.dp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewProfileIntent = new Intent(getActivity(), ViewProfileActivity.class);
                        viewProfileIntent.putExtra("VIEWPROFILE_USERREF", mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/requests/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/", ""));
                        startActivity(viewProfileIntent);
                    }
                });

                viewHolder.accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String friendRef = mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/requests/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/", "");
                        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference addFriendRef = FirebaseDatabase.getInstance().getReference();//.child("friends");

//                        addFriendRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friendRef).child("name").setValue(viewHolder.displayName.getText().toString());
//                        addFriendRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friendRef).child("image").setValue(viewHolder.userImageForRequestFragment);
//                        addFriendRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friendRef).child("chatactive").setValue(0);
//                        addFriendRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friendRef).child("chatroom").setValue(" ");
//                        addFriendRef.getParent().child("friendsdata").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friendRef).child("friendsince").setValue(ServerValue.TIMESTAMP);
//
//                        DatabaseReference reverseAddFriendRef = FirebaseDatabase.getInstance().getReference().child("friends");
//
//                        reverseAddFriendRef.child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(selfName);
//                        reverseAddFriendRef.child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("image").setValue(selfImage);
//                        reverseAddFriendRef.child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatactive").setValue(0);
//                        reverseAddFriendRef.child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatroom").setValue(" ");
//                        reverseAddFriendRef.getParent().child("friendsdata").child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friendsince").setValue(ServerValue.TIMESTAMP);

//                        mDatabase.child(friendRef).setValue(null);

                        Map acceptMap = new HashMap();
                        acceptMap.put("friends/" + myUid + "/" + friendRef + "/name", viewHolder.displayName.getText().toString());
                        acceptMap.put("friends/" + myUid + "/" + friendRef + "/image", viewHolder.userImageForRequestFragment);
                        acceptMap.put("friends/" + myUid + "/" + friendRef + "/chatactive", 0);
                        acceptMap.put("friends/" + myUid + "/" + friendRef + "/chatroom", " ");
                        acceptMap.put("friendsdata/" + myUid + "/" + friendRef + "/friendsince", ServerValue.TIMESTAMP);

                        acceptMap.put("friends/" + friendRef + "/" + myUid + "/name", selfName);
                        acceptMap.put("friends/" + friendRef + "/" + myUid + "/image", selfImage);
                        acceptMap.put("friends/" + friendRef + "/" + myUid + "/chatactive", 0);
                        acceptMap.put("friends/" + friendRef + "/" + myUid + "/chatroom", " ");
                        acceptMap.put("friendsdata/" + friendRef + "/" + myUid + "/friendsince", ServerValue.TIMESTAMP);

                        acceptMap.put("requests/" + myUid + "/" + friendRef, null);

                        addFriendRef.updateChildren(acceptMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (null == databaseError)
                                    Sneaker.with(getActivity()).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                            .setTitle("Success")
                                            .setMessage("You are now Friends with " + viewHolder.displayName.getText().toString() + "!")
                                            .setDuration(4000)
                                            .sneakSuccess();
                            }
                        });
                    }
                });

                viewHolder.reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(getActivity())
                                .title("Reject friend Request from " + viewHolder.displayName.getText().toString() + " ?")
                                .positiveText("REJECT")
                                .negativeText("CANCEL")
                                .titleColor(Color.RED)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String friendRef = mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/requests/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/", "");
                                        mDatabase.child(friendRef).setValue(null);

                                        Sneaker.with(getActivity()).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                                .setTitle("Success")
                                                .setMessage("Friend Request Rejected from " + viewHolder.displayName.getText().toString() + "!")
                                                .setDuration(4000)
                                                .sneakSuccess();
                                    }
                                })
                                .show();
                    }
                });


            }
        };

        mRecyclerView.setAdapter(mRecyclerAdapter);

        return rootView;
    }


    public static class RequestFragmentViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView displayName;
        TextView requestMessage;
        CircularImageView dp;
        RelativeLayout expand;
        CardView cardView;
        RelativeLayout relativeLayout;
        Button accept;
        Button reject;
        String userImageForRequestFragment;

        public RequestFragmentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            accept = (Button) mView.findViewById(R.id.requestfragment_accept);
            reject = (Button) mView.findViewById(R.id.requestfragment_reject);
            expand = (RelativeLayout) mView.findViewById(R.id.requestfragment_expandedrelativelayout);
            cardView = (CardView) mView.findViewById(R.id.requestfragment_cardview);
            relativeLayout = (RelativeLayout) mView.findViewById(R.id.requestfragment_RL);
        }

        public void setName(String name) {
            displayName = (TextView) mView.findViewById(R.id.requestfragment_name);
            displayName.setText(name);
        }

        public void setMessage(String message) {
            requestMessage = (TextView) mView.findViewById(R.id.requestfragment_message);
            requestMessage.setText(message);
        }

        public void setImage(final Context context, final String image) {
            dp = (CircularImageView) mView.findViewById(R.id.requestfragment_image);
            userImageForRequestFragment = image;
            if (!image.equals("default")) {
                Picasso.with(context).load(image).placeholder(R.drawable.default_avatar).networkPolicy(NetworkPolicy.OFFLINE).centerCrop().resize(64, 64).into(dp, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(image).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).centerCrop().resize(64, 64).into(dp);
                    }
                });
            }
        }
    }

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
        a.setDuration((int) (targetHeight * 5 / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecyclerAdapter.cleanup();
    }
}
