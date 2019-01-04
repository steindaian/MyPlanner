package upt.myplanner.friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import upt.myplanner.MainActivity;
import upt.myplanner.R;
import upt.myplanner.login.LoginActivity;
import upt.myplanner.photo.PhotoActivity;

public class Requests extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private RecyclerView friendsListView;
    private RequestsViewAdapter mAdapter;
    private final ArrayList<MyUser> requestList = new ArrayList<MyUser>();
    private List<String> reqUids;
    private List<String> friendUids;
    private TextView noReq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
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
                    Intent intent = new Intent(Requests.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };

        friendsListView = (RecyclerView) findViewById(R.id.reqListView);
        friendsListView.setHasFixedSize(false);
        friendsListView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RequestsViewAdapter(this, requestList,auth.getCurrentUser().getUid(),db);
        friendsListView.setAdapter(mAdapter);
        noReq = (TextView) findViewById(R.id.reqPlaceHolder);
        db.collection("users").document(auth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null) {
                    Log.e("ReqActiv","Error on getting requests uids "+e);
                    noReq.setVisibility(View.VISIBLE);
                    return;
                }

                try {
                    Log.d("RequestActivity","Req list updated");
                    MyUser user = documentSnapshot.toObject(MyUser.class);
                    reqUids = user.requests;
                    friendUids = user.friends;
                    requestList.clear();
                    mAdapter.userReqIds = reqUids;
                    mAdapter.userFriendIds = friendUids;
                    mAdapter.notifyDataSetChanged();
                    noReq.setVisibility(View.VISIBLE);
                    if(reqUids!=null) {
                        for(String uid:reqUids) {
                            db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        noReq.setVisibility(View.GONE);
                                        MyUser reqUser = task.getResult().toObject(MyUser.class);
                                        reqUser.uid = task.getResult().getId();
                                        reqUser.friends = (List<String>) task.getResult().get("friends");
                                        reqUser.requests = (List<String>) task.getResult().get("requests");
                                        requestList.add(reqUser);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    else noReq.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                }
                catch (Exception ee) {
                    noReq.setVisibility(View.VISIBLE);
                    requestList.clear();
                    mAdapter.notifyDataSetChanged();
                    ee.printStackTrace();
                    return;
                }


            }
        });


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
        if(nextActivity!=null && nextActivity!=this.getClass()) startActivity(new Intent(this, nextActivity));
        return true;
    }
}
