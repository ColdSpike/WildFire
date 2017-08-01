package com.example.makrandpawar.chatla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.irozon.sneaker.Sneaker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private ImageButton sendButton;
    private EditText messageArea;
    private String chatWith;
    private Toolbar mToolBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int LIMITVALUE = 5;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<UsersChatActivityModelClass, ChatActivityViewHolder> mRecyclerAdapter;
    private String chatRoom;
    private String chatStillValid;
    private static final int CAMERA_REQUEST_CODE = 299;
    private boolean hidden = true;
    private Uri mCropImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase, mDatabase2;
    private ProgressDialog progressDialog;
    private String filemanagerstring = null;
    private String selectedImagePath = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolBar = (Toolbar) findViewById(R.id.chatactivity_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.YELLOW, Color.BLUE);

        progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading Image");

        chatWith = getIntent().getStringExtra("CHATWITH");
        chatRoom = getIntent().getStringExtra("CHATROOM");
        final String chatUserName = getIntent().getStringExtra("CHATUSERNAME");
        getSupportActionBar().setTitle(chatUserName);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(chatWith);
        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                FirebaseDatabase.getInstance().getReference().child("ServerTime").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot1) {
                        if (dataSnapshot.child("statusonline").getValue().toString().equals("true"))
                            getSupportActionBar().setSubtitle("online");
                        else {
                            getSupportActionBar().setSubtitle(TimeAgo.getTimeAgo(Long.parseLong(dataSnapshot.child("statusonline").getValue().toString()), Long.parseLong(dataSnapshot1.getValue().toString())));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("chats").child(chatRoom).child("invalid");
        databaseReference1.keepSynced(true);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatStillValid = dataSnapshot.child("from").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendButton = (ImageButton) findViewById(R.id.chatactivity_send);
        messageArea = (EditText) findViewById(R.id.chatactivity_edt_message);
        mRecyclerView = (RecyclerView) findViewById(R.id.chatsactivity_recyclerview);

        final LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.hasFixedSize();


        mDatabase = FirebaseDatabase.getInstance().getReference().child("chats").child(chatRoom);
        mDatabase.keepSynced(true);
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("userchats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(chatRoom).child("lastmessage");
        mDatabase2.keepSynced(true);
        final Query[] query = {mDatabase.orderByChild("timestamp").limitToLast(LIMITVALUE + 1)};
        setRecyclerView(query[0]);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LIMITVALUE += 5;
                query[0] = mDatabase.orderByChild("timestamp").limitToLast(LIMITVALUE + 1);
                setRecyclerView(query[0]);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(0);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }

        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();
                sendMessage(messageText, 0);
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

    private void sendMessage(String message, int messageType) {
        if (!chatStillValid.equals("not dummy")) {
            if (!message.equals("")) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("message", message);
                map.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
                map.put("timestamp", ServerValue.TIMESTAMP);
                map.put("seen", "no");
                if (messageType == 1)
                    map.put("type", "image");
                else if (messageType == 2)
                    map.put("type", "video");
                else
                    map.put("type", "text");

                mDatabase.push().setValue(map);

                if (messageType == 1) {
                    mDatabase2.setValue("image");
                    mDatabase2.getParent().getParent().getParent().child(chatWith).child(chatRoom).child("lastmessage").setValue("image");
                } else if (messageType == 2) {
                    mDatabase2.setValue("video");
                    mDatabase2.getParent().getParent().getParent().child(chatWith).child(chatRoom).child("lastmessage").setValue("video");
                } else {
                    mDatabase2.setValue(message);
                    mDatabase2.getParent().getParent().getParent().child(chatWith).child(chatRoom).child("lastmessage").setValue(message);
                }

                messageArea.setText("");
            }
        } else {
            Sneaker.with(ChatActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error!").setMessage("Can not send message to this chat anymore!").setDuration(10000).sneakError();
        }
    }

    private void setRecyclerView(Query query) {
        mRecyclerAdapter = new FirebaseRecyclerAdapter<UsersChatActivityModelClass, ChatActivityViewHolder>(
                UsersChatActivityModelClass.class,
                R.layout.chatsactivity_singlechat,
                ChatActivityViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(ChatActivityViewHolder viewHolder, UsersChatActivityModelClass model, int position) {
                if (model.getFrom().equals("not dummy")) {
                    viewHolder.setMessage("CHAT NO LONGER ACTIVE!", "other user", "", "text", "", ChatActivity.this);
                } else if (model.getFrom().equals("dummy")) {
                    viewHolder.setMessage("", "", "", "", "", ChatActivity.this);
                } else {
                    Date date = new Date(Long.parseLong(model.getTimestamp()));
                    DateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                    format.setTimeZone(TimeZone.getDefault());
                    String time = format.format(date);
                    viewHolder.setMessage(model.getMessage(), model.getFrom(), time, model.getType(), model.getSeen(), ChatActivity.this);
                }
            }

            @Override
            public void onBindViewHolder(final ChatActivityViewHolder viewHolder, final int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.cardViewText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new MaterialDialog.Builder(ChatActivity.this)
                                .title("Delete?")
                                .titleColor(Color.RED)
                                .positiveText("DELETE")
                                .negativeText("CANCEL")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        mRecyclerAdapter.getRef(position).setValue(null);
                                    }
                                })
                                .show();
                        return true;
                    }
                });
                viewHolder.cardViewImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, ViewImageMessageActivity.class);
                        intent.putExtra("IMAGEMESSAGE", viewHolder.message);
                        intent.putExtra("CHATROOMTOIMAGE", chatRoom);
                        intent.putExtra("CHATWITHTOIMAGE", chatWith);
                        startActivity(intent);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    public static class ChatActivityViewHolder extends RecyclerView.ViewHolder {
        View mView;
        private TextView msgText;
        private CardView cardViewText;
        private TextView imgTime;
        private TextView msgTimeText;
        private ImageView msgImage;
        private CardView cardViewImage;
        private String message;
        private CardView cardViewVideo;
        private TextView videoTime;
        private VideoView videoView;


        public ChatActivityViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            msgText = (TextView) mView.findViewById(R.id.chatactivity_message_right);
            cardViewText = (CardView) mView.findViewById(R.id.chatactivity_cardview_right);
            msgTimeText = (TextView) mView.findViewById(R.id.chatactivity_time_right);
            imgTime = (TextView) mView.findViewById(R.id.chatactivity_time_image_right);
            msgImage = (ImageView) mView.findViewById(R.id.chatactivity_imageright);
            cardViewImage = (CardView) mView.findViewById(R.id.chatactivity_cardview_imageright);
            videoView = (VideoView) mView.findViewById(R.id.chatactivity_video);
            videoTime = (TextView) mView.findViewById(R.id.chatactivity_time_video);
            cardViewVideo = (CardView) mView.findViewById(R.id.chatactivity_cardview_video);
        }

        public void setMessage(String message, String from, String time, String type, String seen, final Context context) {
            this.message = message;
            cardViewImage.setVisibility(View.GONE);
            cardViewText.setVisibility(View.GONE);
            cardViewVideo.setVisibility(View.GONE);


            if (from.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                if (type == null) {

                } else if (type.equals("image")) {
                    cardViewImage.setVisibility(View.VISIBLE);
                    msgImage.setBackground(context.getDrawable(R.drawable.in_message_bg));
                    Picasso.with(context).load(message).resize(300, 300).placeholder(context.getDrawable(R.drawable.default_avatar)).into(msgImage);
                    imgTime.setText(time);
                    cardViewImage.setLayoutParams(getCardViewParams(true));
                } else if (type.equals("text")) {
                    cardViewText.setVisibility(View.VISIBLE);
                    msgText.setBackground(context.getDrawable(R.drawable.in_message_bg));
                    msgText.setText(message);
                    msgTimeText.setText(time);
                    msgTimeText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    msgText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    cardViewText.setLayoutParams(getCardViewParams(true));
                } else if (type.equals("video")) {
                    cardViewVideo.setVisibility(View.VISIBLE);
                   // videoView.setBackground(context.getDrawable(R.drawable.in_message_bg));
                    videoTime.setText(time);
                    cardViewVideo.setLayoutParams(getCardViewParams(true));
                    Uri uri = Uri.parse("android.resource://com.example.makrandpawar.chatla/"+R.raw.d);
                    videoView.setVideoURI(uri);
                    //
                    videoView.requestFocus();
                   videoView.start();
                }
            } else {
                if (type == null) {

                } else if (type.equals("image")) {
                    cardViewImage.setVisibility(View.VISIBLE);
                    msgImage.setBackground(context.getDrawable(R.drawable.out_message_bg));
                    Picasso.with(context).load(message).resize(300, 300).placeholder(context.getDrawable(R.drawable.default_avatar)).into(msgImage);
                    imgTime.setText(time);
                    cardViewImage.setLayoutParams(getCardViewParams(false));
                } else if (type.equals("text")) {
                    cardViewText.setVisibility(View.VISIBLE);
                    msgText.setBackground(context.getDrawable(R.drawable.out_message_bg));
                    msgText.setText(message);
                    msgTimeText.setText(time);
                    msgTimeText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    msgText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    cardViewText.setLayoutParams(getCardViewParams(false));
                } else if (type.equals("video")) {
                    cardViewVideo.setVisibility(View.VISIBLE);
                   // videoView.setBackground(context.getDrawable(R.drawable.out_message_bg));
                    videoTime.setText(time);
                    cardViewVideo.setLayoutParams(getCardViewParams(false));
                    Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()+"DCIM/Camera/video.mp4");

                    videoView.setVideoURI(uri);
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            Toast.makeText(context, "VIDEO PREPARED", Toast.LENGTH_SHORT).show();
                            mp.setLooping(true);
                            videoView.start();
                        }
                    });
                   // videoView.requestFocus();
                   // videoView.start();
                }
            }
        }

        private ViewGroup.LayoutParams getCardViewParams(boolean isRight) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cardViewText.getLayoutParams();
            if (isRight) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            } else {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            }
            return layoutParams;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chatactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.chatactivity_menu_profile:
                Intent viewProfileIntent = new Intent(ChatActivity.this, ViewProfileActivity.class);
                viewProfileIntent.putExtra("VIEWPROFILE_USERREF", chatWith);
                startActivity(viewProfileIntent);
                return true;
            case R.id.chatactivity_menu_image:
                startImagePicker();
                return true;
            case R.id.chatactivity_menu_video:
                startVideoPicker();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return true;
        }
    }

    private void startVideoPicker() {
        Intent videoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        videoIntent.setType("video/*");
        startActivityForResult(Intent.createChooser(videoIntent, "select video"), REQUEST_TAKE_GALLERY_VIDEO);
    }

    private void startImagePicker() {
        if (ContextCompat.checkSelfPermission(ChatActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            CropImage.startPickImageActivity(ChatActivity.this);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            } else {
                Sneaker.with(ChatActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Permission Denied").setMessage("Grant CAMERA permissions to use camera for picker").sneakError();
            }
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(ChatActivity.this, imageUri)) {
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

                progressDialog.show();

                File thumbFile = new File(resultUri.getPath());
                File thumbImage = null;
                try {
                    thumbImage = new Compressor(this).setQuality(50).compressToFile(thumbFile);
                } catch (IOException e) {
                    Sneaker.with(ChatActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error!").setMessage(e.getMessage()).sneakError();
                }

                mStorageRef = FirebaseStorage.getInstance().getReference();
                mStorageRef.child("message_images").child(chatRoom).child(UUID.randomUUID().toString()).putFile(Uri.fromFile(thumbImage)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        sendMessage(downloadUrl, 1);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Sneaker.with(ChatActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Error!").setMessage(e.getMessage()).sneakError();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") int progress = (int) (100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                        progressDialog.setProgress(progress);
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


/* for video picker*/
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                //Uri selectedImageUri = data.getData();

                // OI FILE Manager
                //filemanagerstring = selectedImageUri.getPath();

                // MEDIA GALLERY
                selectedImagePath = getPath(data.getData());
                if (selectedImagePath != null) {
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    File file = new File(selectedImagePath);
                    mStorageRef.child("message_video").child(chatRoom).child(UUID.randomUUID().toString()).putFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String url = taskSnapshot.getDownloadUrl().toString();
                            Toast.makeText(ChatActivity.this, "VIDEO UPLOADED", Toast.LENGTH_SHORT).show();
                            sendMessage(url, 2);
                        }
                    });
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .start(this);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                // Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
                Sneaker.with(ChatActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Permission Denied").setMessage("Cancelling, required permissions are not granted!").sneakError();
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                CropImage.startPickImageActivity(ChatActivity.this);
            } else {
                //Toast.makeText(this, "Permission denied. Camera option not available", Toast.LENGTH_SHORT).show();
                Sneaker.with(ChatActivity.this).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT).setTitle("Permission Denied").setMessage("Camera option not available!").sneakError();
                CropImage.startPickImageActivity(ChatActivity.this);
            }
        }
    }
}
