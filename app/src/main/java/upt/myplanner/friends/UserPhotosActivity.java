package upt.myplanner.friends;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import upt.myplanner.R;
import upt.myplanner.login.LoginActivity;
import upt.myplanner.photo.ClickListener;
import upt.myplanner.photo.PhotoActivity;
import upt.myplanner.photo.PhotoPost;
import upt.myplanner.photo.PostActivity;
import upt.myplanner.photo.RecyclerViewAdapter;
import upt.myplanner.photo.RecyclerViewItemClickListener;

public class UserPhotosActivity extends AppCompatActivity {

    private static final String TAG = "UserPhotosActivity";
    private Context context = this;
    private FirebaseFirestore db;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    private String userUid="";
    private String userName="";
    private RecyclerView photoListView;
    private ArrayList<PhotoPost> photoList = new ArrayList<PhotoPost>();
    private CircleImageView profileImg;
    private TextView profileName;
    private TextView profileDescription;
    private TextView photoPH;
    private ListenerRegistration profileRegistration;
    private EventListener<DocumentSnapshot> profileListener;
    private StorageReference mStorage;
    private ChildEventListener photoEventListener;
    private ConcurrentHashMap<String, PhotoPost> photoMap = new ConcurrentHashMap<String, PhotoPost>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorage = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else {
            try {
                userUid = auth.getCurrentUser().getUid();
            }
            catch (Exception e) {
                e.printStackTrace();
                this.onBackPressed();
                finish();
            }
        }
        setContentView(R.layout.activity_user_photos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(UserPhotosActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        try{
            userUid = getIntent().getStringExtra("Uid");
            userName = getIntent().getStringExtra("Name");
            if(userUid==null || userUid.isEmpty() || userUid.equals("")) {
                super.onBackPressed();
                finish();
            }
            if(userUid.equals(auth.getCurrentUser().getUid())) {
                startActivity(new Intent(this,PhotoActivity.class));
                finish();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed();
            finish();
        }
        setTitle(userName);
        profileImg = (CircleImageView) findViewById(R.id.userprofilePicture);
        profileName = (TextView) findViewById(R.id.userprofileName);
        profileDescription = (TextView) findViewById(R.id.userprofileDescription);
        photoPH = (TextView) findViewById(R.id.userphotoPlaceHolder);
        photoListView = (RecyclerView) findViewById(R.id.userphotoListView);
        photoListView.setHasFixedSize(false);
        photoListView.setLayoutManager(new LinearLayoutManager(this));

        final RecyclerView.Adapter mAdapter = new RecyclerViewAdapter(this, photoList);
        photoListView.setAdapter(mAdapter);

        photoListView.addOnItemTouchListener(new RecyclerViewItemClickListener(UserPhotosActivity.this, photoListView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "Normal press");
                Intent intent = new Intent(UserPhotosActivity.this,PostActivity.class);
                intent.putExtra("List", photoList);
                intent.putExtra("Position",position);
                intent.putExtra("Username",userName);
                intent.putExtra("Uid",userUid);
                intent.putExtra("Internet",true);
                startActivity(intent);
            }


            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "Long press");

            }
        }));

        profileListener = new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen to profile picture changes failed.", e);
                    Toast.makeText(UserPhotosActivity.this, "Profile picture not loaded!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists() && documentSnapshot.get("downloadImgPath") != null) {
                    Log.d(TAG, "Download url for profile picture is " + (String) documentSnapshot.get("downloadImgPath"));

                    String profilePhotoPath = (String) documentSnapshot.get("downloadImgPath");
                    //if(userName== null || userName.trim().equals(""))
                    userName = (String ) documentSnapshot.get("name");
                    Picasso.with(context)
                            .load(profilePhotoPath) // thumnail url goes here
                            .resize(300, 300)
                            .centerCrop()
                            .placeholder(R.drawable.placeholder_img)
                            .error(R.drawable.error_img)
                            .into(profileImg);

                    profileName.setText(userName);
                    if (documentSnapshot.get("description") != null) {
                        profileDescription.setText((String) documentSnapshot.get("description"));
                    }

                }
            }
        };

        //listener for photo posts
        photoEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {
                //Log.v(TAG,"id of photo is: "+dataSnapshot.getKey());
                photoPH.setVisibility(View.INVISIBLE);
                final PhotoPost p = dataSnapshot.getValue(PhotoPost.class);
                final String key = dataSnapshot.getKey();
                if (p.getImgPath() == null) return;
                mStorage.child(p.getImgPath()).getMetadata().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        photoMap.put(key, p);
                        photoList.clear();
                        photoList.addAll(photoMap.values());
                        photoList.sort(new Comparator<PhotoPost>() {
                            @Override
                            public int compare(PhotoPost o1, PhotoPost o2) {
                                return o1.getTimestamp().compareTo(o2.getTimestamp());
                            }
                        });
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {
                final PhotoPost p = dataSnapshot.getValue(PhotoPost.class);
                final String key = dataSnapshot.getKey();
                mStorage.child(p.getImgPath()).getMetadata().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        photoMap.put(key, p);
                        photoList.clear();
                        photoList.addAll(photoMap.values());
                        photoList.sort(new Comparator<PhotoPost>() {
                            @Override
                            public int compare(PhotoPost o1, PhotoPost o2) {
                                return o1.getTimestamp().compareTo(o2.getTimestamp());
                            }
                        });
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                PhotoPost p = dataSnapshot.getValue(PhotoPost.class);
                if (photoMap.containsKey(dataSnapshot.getKey()))
                    photoMap.remove(dataSnapshot.getKey());
                photoList.clear();
                photoList.addAll(photoMap.values());
                photoList.sort(new Comparator<PhotoPost>() {
                    @Override
                    public int compare(PhotoPost o1, PhotoPost o2) {
                        return o1.getTimestamp().compareTo(o2.getTimestamp());
                    }
                });
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

    }
    @Override
    public void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
        profileRegistration = db.collection("users").document(userUid).addSnapshotListener(profileListener);
        auth.addAuthStateListener(authListener);
        databaseReference.child("posts").child(userUid).addChildEventListener(photoEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        if(profileRegistration!=null) {
            profileRegistration.remove();
            profileRegistration = null;
        }
        if(photoEventListener!= null) {
            databaseReference.child("posts").child(userUid).removeEventListener(photoEventListener);
        }
    }
}
