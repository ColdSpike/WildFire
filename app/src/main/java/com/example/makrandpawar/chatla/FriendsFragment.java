package com.example.makrandpawar.chatla;


import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FriendsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<UsersFriendsFragmentModelClass, FriendsFragmentViewHolder> mRecyclerAdapter;
    private DatabaseReference mDatabase;
    private int mExpandedPosition = -1;
    private CircularImageView noFriendImage;
    private TextView noFriendTxt;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.friendsfragment_recyclerview);
        noFriendImage = (CircularImageView) rootView.findViewById(R.id.friendsfragment_nofriend_image);
        noFriendTxt = (TextView) rootView.findViewById(R.id.friendsfragment_nofriend_text);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabase.keepSynced(true);

        mRecyclerAdapter = new FirebaseRecyclerAdapter<UsersFriendsFragmentModelClass, FriendsFragmentViewHolder>(
                UsersFriendsFragmentModelClass.class,
                R.layout.friendsfragment_singleuser,
                FriendsFragmentViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                if (mRecyclerAdapter.getItemCount() == 0) {
                    noFriendImage.setVisibility(View.VISIBLE);
                    noFriendTxt.setVisibility(View.VISIBLE);
                } else {
                    noFriendImage.setVisibility(View.GONE);
                    noFriendTxt.setVisibility(View.GONE);
                }
            }

            @Override
            protected void populateViewHolder(FriendsFragmentViewHolder viewHolder, UsersFriendsFragmentModelClass model, int position) {
                if (model.getName()!=null  && model.getChatroom()!=null && model.getImage()!=null) {
                    viewHolder.setName(model.getName());
                    viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                    viewHolder.chatActive = model.getChatactive();
                    viewHolder.chatRoom = model.getChatroom();
                }
            }

            @Override
            public void onBindViewHolder(final FriendsFragmentViewHolder viewHolder, final int position) {
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

                final String friendRef = mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/friends/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/", "");

                viewHolder.sendMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewHolder.chatActive == 1) {
                            Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                            chatIntent.putExtra("CHATWITH", friendRef);
                            chatIntent.putExtra("CHATROOM", viewHolder.chatRoom);
                            startActivity(chatIntent);
                        } else {
                            mDatabase.child(friendRef).child("chatactive").setValue(1);
                            mDatabase.getParent().child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatactive").setValue(1);

                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            Map<String, String> map = new HashMap<String, String>();
                            map.put("lastmessage", "New Chat Started.");
                            map.put("with", friendRef);

                            final String key = databaseReference.child("userchats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();
                            databaseReference.child("userchats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).setValue(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            databaseReference.child("chats").child(key).child("invalid").child("from").setValue("dummy");
                                            databaseReference.child("chats").child(key).child("invalid").child("message").setValue("dummy");

                                            Map<String,String> map1 = new HashMap();
                                            map1.put("lastmessage", "has started a new chat.");
                                            map1.put("with", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                            databaseReference.child("userchats").child(friendRef).child(key).setValue(map1);

                                            databaseReference.child("friends").child(friendRef).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatroom").setValue(key);
                                            databaseReference.child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friendRef).child("chatroom").setValue(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                                                        chatIntent.putExtra("CHATWITH", friendRef);
                                                        chatIntent.putExtra("CHATROOM", key);
                                                        startActivity(chatIntent);
                                                    }
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                });

                viewHolder.viewProfile.setOnClickListener(new View.OnClickListener()

                {
                    @Override
                    public void onClick(View v) {
                        Intent viewProfileIntent = new Intent(getActivity(), ViewProfileActivity.class);
                        viewProfileIntent.putExtra("VIEWPROFILE_USERREF", mRecyclerAdapter.getRef(position).toString().replace("https://chatla-1a62a.firebaseio.com/friends/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/", ""));
                        startActivity(viewProfileIntent);
                    }
                });

                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(friendRef);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("statusonline").exists())
                            if (dataSnapshot.child("statusonline").getValue().toString().equals("true"))
                                viewHolder.userStatusOnline.setVisibility(View.VISIBLE);
                        else
                            viewHolder.userStatusOnline.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mRecyclerView.setAdapter(mRecyclerAdapter);

        mRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()

        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        return rootView;
    }

    public static class FriendsFragmentViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView displayName;
        CircularImageView dp;
        RelativeLayout expand;
        CardView cardView;
        RelativeLayout relativeLayout;
        Button sendMessage;
        Button viewProfile;
        int chatActive;
        String chatRoom;
        ImageView userStatusOnline;

        public FriendsFragmentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            sendMessage = (Button) mView.findViewById(R.id.friendsfragment_sendmessage);
            viewProfile = (Button) mView.findViewById(R.id.friendsfragment_viewprofile);
            expand = (RelativeLayout) mView.findViewById(R.id.friendsfragment_expandedrelativelayout);
            cardView = (CardView) mView.findViewById(R.id.friendsfragment_cardview);
            relativeLayout = (RelativeLayout) mView.findViewById(R.id.friendsfragment_RL);
            userStatusOnline = (ImageView) mView.findViewById(R.id.friendsfragment_userstatusonline);
        }

        public void setName(String name) {
            displayName = (TextView) mView.findViewById(R.id.friendsfragment_name);
            displayName.setText(name);
        }

        public void setImage(final Context context, final String image) {
            dp = (CircularImageView) mView.findViewById(R.id.friendsfragment_image);
            if (!image.equals("default")) {
                Picasso.with(context).load(image).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).centerCrop().resize(64, 64).into(dp);
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
