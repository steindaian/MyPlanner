package upt.myplanner.friends;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import upt.myplanner.MainActivity;
import upt.myplanner.R;
import upt.myplanner.login.LoginActivity;
import upt.myplanner.photo.ClickListener;
import upt.myplanner.photo.PhotoActivity;
import upt.myplanner.photo.PostActivity;
import upt.myplanner.photo.RecyclerViewAdapter;
import upt.myplanner.photo.RecyclerViewItemClickListener;

public class FriendsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private RecyclerView friendsListView;

    private Activity activity = this;
    private static final String TAG = "FriendsActivity";

    private final ArrayList<MyUser> friendsList = new ArrayList<MyUser>();
    private List<String> friendListUid = null;
    private HashMap<String,MyUser> userMap = new HashMap<String,MyUser>();
    private RecyclerView.Adapter mAdapter;
    private TextView noFriendsText;
    private EditText searchText;
    private TextWatcher searchTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent intent = new Intent(FriendsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };

        friendsListView = (RecyclerView) findViewById(R.id.friendListView);
        friendsListView.setHasFixedSize(false);
        friendsListView.setLayoutManager(new LinearLayoutManager(this));

        friendsListView.addOnItemTouchListener(new RecyclerViewItemClickListener(activity, friendsListView, new upt.myplanner.photo.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "Normal press");
                //goto PhtoActivity
                MyUser user = friendsList.get(position);
                if(user.isFriend==true) {
                    Intent intent = new Intent(FriendsActivity.this,UserPhotosActivity.class);
                    intent.putExtra("Uid",user.uid);
                    intent.putExtra("Name",user.name);
                    startActivity(intent);
                    //activity.finish();
                }
            }


            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "Long press");
                final MyUser user = friendsList.get(position);
                if(user.isFriend==false) return;
                final AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(FriendsActivity.this);
                myAlertDialog.setTitle("Unfriend");
                myAlertDialog.setMessage("Do you want to remove "+user.name+" from your friend list?");

                myAlertDialog.setNeutralButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                myAlertDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                friendListUid.remove(user.uid);
                                user.friends.remove(auth.getCurrentUser().getUid());
                                db.collection("users").document(auth.getCurrentUser().getUid()).update("friends",friendListUid);
                                db.collection("users").document(user.uid).update("friends",user.friends);
                            }
                        });

                myAlertDialog.show();
            }
        }));

        mAdapter = new FriendsViewAdapter(this, friendsList,auth.getCurrentUser().getUid());
        friendsListView.setAdapter(mAdapter);

        noFriendsText = (TextView) findViewById(R.id.friendsPlaceHolderText);
        searchText = (EditText) findViewById(R.id.searchText);
        db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null) {
                    userMap.clear();
                    friendsList.clear();
                    mAdapter.notifyDataSetChanged();
                    Log.e(TAG,"Listen for users failed. "+e);
                    return;
                }

                for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()) {
                    MyUser user = doc.getDocument().toObject(MyUser.class);
                    user.uid = doc.getDocument().getId();
                    user.friends = (List<String>) doc.getDocument().get("friends");
                    friendListUid = user.friends;
                    user.requests = (List<String>) doc.getDocument().get("requests");
                    //Log.d(TAG,user.friends.toString());
                    switch (doc.getType()) {
                        case ADDED:{
                            userMap.put(user.uid,user);
                            populateFriendsList();
                            break;
                        }
                        case REMOVED: {
                            userMap.remove(user.uid);
                            populateFriendsList();
                            break;
                        }
                        case MODIFIED:{
                            userMap.put(user.uid,user);
                            populateFriendsList();
                            break;
                        }
                    }
                }
            }
        });

        searchTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                if(text.isEmpty() || text.equals("")) {
                    populateFriendsList();
                    return;
                }
                final TextView noFriendsText = (TextView) activity.findViewById(R.id.friendsPlaceHolderText);
                friendsList.clear();
                for(MyUser user: userMap.values()) {
                    if(user.uid.equals(auth.getCurrentUser().getUid())) continue;
                    if(user.name.toLowerCase().startsWith(text.toLowerCase())) {
                        if(user.friends== null || !user.friends.contains(auth.getCurrentUser().getUid())) user.isFriend = false;
                        else user.isFriend = true;
                        friendsList.add(user);
                    }
                    else if(user.email.toLowerCase().startsWith(text.toLowerCase())) {
                        if(user.friends==null || !user.friends.contains(auth.getCurrentUser().getUid())) user.isFriend = false;
                        else user.isFriend = true;
                        friendsList.add(user);
                    }
                }
                if(friendsList.size()>0) {
                    noFriendsText.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                }
                else {
                    noFriendsText.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        searchText.addTextChangedListener(searchTextWatcher);

    }

    private void populateFriendsList() {
        friendsList.clear();
        for(MyUser user:userMap.values()) {
            if(user.friends!=null && auth.getCurrentUser()!=null && user.friends.contains(auth.getCurrentUser().getUid())) {
                noFriendsText.setVisibility(View.GONE);
                friendsList.add(user);
            }
        }
        mAdapter.notifyDataSetChanged();
        if(friendsList.size()==0) noFriendsText.setVisibility(View.VISIBLE);
        if(!searchText.getText().toString().equals("")) {
            searchTextWatcher.onTextChanged(searchText.getText().toString(),0,0,0);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Class nextActivity=null;

        if (id == R.id.nav_calendar) {
            // Handle the calendar action
        } else if (id == R.id.nav_friends) {
            nextActivity = FriendsActivity.class;
        } else if (id == R.id.nav_requests) {
            nextActivity = Requests.class;
        } else if (id == R.id.nav_settings) {
            nextActivity = MainActivity.class;
        } else if (id == R.id.nav_photos) {
            nextActivity = PhotoActivity.class;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(nextActivity!=null && nextActivity!=this.getClass()) {
            startActivity(new Intent(this, nextActivity));
        }
        return true;
    }
}
