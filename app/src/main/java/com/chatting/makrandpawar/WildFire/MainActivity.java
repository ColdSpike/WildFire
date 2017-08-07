package com.chatting.makrandpawar.WildFire;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.makrandpawar.chatla.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private ViewPager mViewPager;
    private SectionsViewPagerAdapter mSectionsViewPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolBar = (Toolbar) findViewById(R.id.main_tool_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.main_title);

        mViewPager = (ViewPager) findViewById(R.id.main_tab_pager);
        mSectionsViewPagerAdapter = new SectionsViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsViewPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        mViewPager.setCurrentItem(1, true);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToStartActivity();
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tokenid").setValue(refreshedToken);
        }
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

    private void sendToStartActivity() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_toolbar_logout:
                mAuth.signOut();
                sendToStartActivity();
                return true;
            case R.id.main_toolbar_setting:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.main_toolbar_all:
                startActivity(new Intent(MainActivity.this, AllUsersActivity.class));
                return true;
            default:
                return true;
        }

    }
}
