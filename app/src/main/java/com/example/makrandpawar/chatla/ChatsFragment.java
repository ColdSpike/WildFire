package com.example.makrandpawar.chatla;


import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.irozon.sneaker.Sneaker;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ChatsFragmentAdapter mRecyclerAdapter;
    private DatabaseReference mDatabase;
    private CircularImageView noChatsImage;
    private TextView noChatsTxt;
    private ArrayList<String> chatRooms;
    private ArrayList<String> chatRoomName;
    private ArrayList<String> chatRoomImage;
    private ArrayList<String> chatRoomWith;
    private ArrayList<String> chatRoomLastMessage;
    private ValueEventListener valueEventListener1;
    private ValueEventListener valueEventListener2;
    private DatabaseReference getNameRef;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.chatsfragment_recyclerview);
        noChatsImage = (CircularImageView) rootView.findViewById(R.id.chatsfragment_nofriend_image);
        noChatsTxt = (TextView) rootView.findViewById(R.id.chatsfragment_nofriend_text);

        chatRooms = new ArrayList<>();
        chatRoomImage = new ArrayList<>();
        chatRoomName = new ArrayList<>();
        chatRoomWith = new ArrayList<>();
        chatRoomLastMessage = new ArrayList<>();

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("userchats").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabase.keepSynced(true);
        valueEventListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mRecyclerView.setAdapter(null);
                if (valueEventListener2 != null) {
                    getNameRef.removeEventListener(valueEventListener2);
                }

                chatRooms.clear();
                chatRoomWith.clear();
                chatRoomLastMessage.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    chatRooms.add(dataSnapshot1.getKey());
                    chatRoomWith.add(dataSnapshot1.child("with").getValue().toString());
                    chatRoomLastMessage.add(dataSnapshot1.child("lastmessage").getValue().toString());

                }
                if (chatRooms.size() > 0) {
                    noChatsTxt.setVisibility(View.GONE);
                    noChatsImage.setVisibility(View.GONE);

                    getNameRef = FirebaseDatabase.getInstance().getReference().child("users");
                    getNameRef.keepSynced(true);
                    valueEventListener2 = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            chatRoomImage.clear();
                            chatRoomName.clear();
                            for (int i = 0; i < chatRoomWith.size(); i++) {
                                chatRoomName.add(dataSnapshot.child(chatRoomWith.get(i)).child("displayname").getValue().toString());
                                chatRoomImage.add(dataSnapshot.child(chatRoomWith.get(i)).child("thumb_image").getValue().toString());
                            }
                            mRecyclerAdapter = new ChatsFragmentAdapter(chatRoomName, chatRoomImage, chatRoomWith, chatRoomLastMessage);
                            mRecyclerView.setAdapter(mRecyclerAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };

                    getNameRef.addValueEventListener(valueEventListener2);

                } else {
                    noChatsTxt.setVisibility(View.VISIBLE);
                    noChatsImage.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(valueEventListener1);

        return rootView;
    }


    public class ChatsFragmentAdapter extends RecyclerView.Adapter<ChatsFragmentAdapter.ChatsFragmentViewHolder> {

        private ArrayList<String> chatRoomName;
        private ArrayList<String> chatRoomImage;
        private ArrayList<String> chatRoomWith;
        private ArrayList<String> chatRoomLastMessage;
        private LayoutInflater inflater;

        public ChatsFragmentAdapter(ArrayList<String> chatRoomName, ArrayList<String> chatRoomImage, ArrayList<String> chatRoomWith, ArrayList<String> chatRoomLastMessage) {
            this.chatRoomName = chatRoomName;
            this.chatRoomImage = chatRoomImage;
            this.chatRoomWith = chatRoomWith;
            this.inflater = LayoutInflater.from(getActivity());
            this.chatRoomLastMessage = chatRoomLastMessage;
        }

        @Override
        public ChatsFragmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.chatsfragment_singleuser, parent, false);

            ChatsFragmentViewHolder holder = new ChatsFragmentViewHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(final ChatsFragmentViewHolder holder, final int position) {
            holder.setRoomName(chatRoomName.get(position), chatRoomLastMessage.get(position));
            holder.setRoomImage(chatRoomImage.get(position));
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                    chatIntent.putExtra("CHATUSERNAME",holder.roomName.getText().toString());
                    chatIntent.putExtra("CHATROOM", chatRooms.get(position));
                    chatIntent.putExtra("CHATWITH", chatRoomWith.get(position));
                    getActivity().startActivity(chatIntent);
                }
            });
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new MaterialDialog.Builder(getActivity())
                            .title("Delete Chat ?")
                            .titleColor(Color.RED)
                            .positiveText("DELETE")
                            .negativeText("CANCEL")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                    databaseReference.child("userchats").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child(chatRooms.get(position))
                                            .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Sneaker.with(getActivity()).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                                    .setTitle("Success")
                                                    .setMessage("Chat deleted Successfully")
                                                    .setDuration(3000)
                                                    .sneakSuccess();
                                            mRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    databaseReference.child("chats").child(chatRooms.get(position)).child("invalid").child("from").setValue("not dummy");
                                    databaseReference.child("chats").child(chatRooms.get(position)).child("invalid").child("message").setValue("This chat has been deleted by other user");
                                    databaseReference.child("friends").child(chatRoomWith.get(position)).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatactive").setValue(0);
                                    databaseReference.child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(chatRoomWith.get(position)).child("chatactive").setValue(0);
                                }
                            })
                            .show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return chatRoomName.size();
        }

        class ChatsFragmentViewHolder extends RecyclerView.ViewHolder {
            TextView roomName;
            CircularImageView roomImage;
            CardView cardView;
            private TextView lastMessage;

            public ChatsFragmentViewHolder(View itemView) {
                super(itemView);

                roomImage = (CircularImageView) itemView.findViewById(R.id.chatsfragment_image);
                roomName = (TextView) itemView.findViewById(R.id.chatsfragment_name);
                cardView = (CardView) itemView.findViewById(R.id.chatsfragment_cardview);
                lastMessage = (TextView) itemView.findViewById(R.id.chatsfragment_lastmessage);
            }

            public void setRoomName(String name, String message) {
                roomName.setText(name);
                lastMessage.setText(message);
            }

            public void setRoomImage(final String image) {
                if (!image.equals("default")) {
                    Picasso.with(getActivity()).load(image).placeholder(R.drawable.default_avatar).networkPolicy(NetworkPolicy.OFFLINE).centerCrop().resize(64, 64).into(roomImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getActivity()).load(image).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).centerCrop().resize(64, 64).into(roomImage);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (valueEventListener1 != null) {
            mDatabase.removeEventListener(valueEventListener1);
            if (valueEventListener2 != null)
                getNameRef.removeEventListener(valueEventListener2);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (valueEventListener1 != null) {
            mDatabase.removeEventListener(valueEventListener1);
            if (valueEventListener2 != null)
                getNameRef.removeEventListener(valueEventListener2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mDatabase.addValueEventListener(valueEventListener1);
        //  getNameRef.addValueEventListener(valueEventListener2);
    }
}

