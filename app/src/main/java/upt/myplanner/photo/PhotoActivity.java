package upt.myplanner.photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import upt.myplanner.MainActivity;
import upt.myplanner.R;
import upt.myplanner.calendar.CalendarActivity;
import upt.myplanner.friends.FriendsActivity;
import upt.myplanner.friends.Requests;
import upt.myplanner.login.LoginActivity;



//add also delete option of profile picture


public class PhotoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE_REQUEST = 1;
    private static final int LOCATION_REQUEST = 3;
    private static final int REQUEST_CHECK_SETTINGS = 4;
    public static final String PHOTO_POSITION = "PhotoPosition";
    private final Context context = this;
    private final Activity activity = this;
    File photoFile = null;
    private Uri mImageUri = null;
    private final double THUMBNAIL_SIZE = 1.0;
    private boolean camera_en = false;
    private boolean storage_en = false;
    private boolean location_en = false;
    private boolean buttonLoc_en = false;
    private boolean profile_en = false;
    private ProgressDialog mProgress;
    private StorageReference mStorage;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private EventListener profileListener;
    private ChildEventListener photoEventListener;
    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseFirestore db;
    private Location mLocation;
    private LocationManager mLocManager;
    private LocationListener mLocListener;
    private LocationRequest mLocationRequest;

    private CircleImageView profileImg;
    private TextView profileName;
    private TextView profileDescription;
    private EditText eDescription;
    private ImageButton editButton;
    private ImageButton deleteButton;
    private RecyclerView photoListView;
    private TextView photoPH;
    private TextView tLocation;
    private final ArrayList<PhotoPost> photoList = new ArrayList<PhotoPost>();
    private HashMap<String, PhotoPost> photoMap = new HashMap<String, PhotoPost>();

    //private FirebaseRecyclerAdapter mAdapter;
    public static final String TAG = "PhotoActivity";
    private String profilePhotoPath;
    private ListenerRegistration profileRegistration = null;
    private String userName;

    private boolean internetEn = false;
    private String profilePhotoImgPath = null;
    private String uid;
    private boolean checkConn = true;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            checkConn = false;
            finish();
        }
        else {
            try {
                uid = auth.getCurrentUser().getUid();
                userName = auth.getCurrentUser().getDisplayName();
                if(auth.getCurrentUser().getPhotoUrl()!=null)
                    profilePhotoPath = auth.getCurrentUser().getPhotoUrl().toString();
                else
                    profilePhotoPath = null;
            }
            catch (Exception e) {
                e.printStackTrace();
                super.onBackPressed();
                checkConn = false;
                finish();
            }
        }

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent intent = new Intent(PhotoActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    checkConn = false;
                    finish();
                }
            }
        };

        //location
        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG,"Location has changed");
                mLocation = location;
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(context, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    tLocation.setText(addresses.get(0).getLocality()+", "+addresses.get(0).getCountryName());
                } catch (IOException e) {
                    e.printStackTrace();
                    tLocation.setText("No location");
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        createLocationRequest();

        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Photos");
        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        profileImg = (CircleImageView) findViewById(R.id.profilePicture);
        profileName = (TextView) findViewById(R.id.profileName);
        profileDescription = (TextView) findViewById(R.id.profileDescription);
        photoPH = (TextView) findViewById(R.id.photoPlaceHolder);
        photoListView = (RecyclerView) findViewById(R.id.photoListView);
        photoListView.setHasFixedSize(false);
        photoListView.setLayoutManager(new LinearLayoutManager(this));

        photoListView.addOnItemTouchListener(new RecyclerViewItemClickListener(activity, photoListView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "Normal press");
                Intent intent = new Intent(activity,PostActivity.class);
                intent.putExtra("List", photoList);
                intent.putExtra("Position",position);
                intent.putExtra("Username",userName);
                intent.putExtra("Uid",auth.getCurrentUser().getUid());
                intent.putExtra("Internet",internetEn);
                checkConn = false;
                startActivity(intent);
            }


            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "Long press");
                if(isInternetAvailable())
                    checkDeletePost(position);
                else {
                    Log.e(TAG,"No internet connection for this function to work");
                    Toast.makeText(context,"No internet connection. Function is disabled",Toast.LENGTH_LONG).show();
                }
            }
        }));

        final RecyclerView.Adapter mAdapter = new RecyclerViewAdapter(this, photoList);
        photoListView.setAdapter(mAdapter);

        editButton = (ImageButton) findViewById(R.id.bEditDescription);
        deleteButton = (ImageButton) findViewById(R.id.bDeleteDescription);
        eDescription = (EditText) findViewById(R.id.eProfileDescription);

        eDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) hideKeyboard(activity);
            }
        });
        eDescription.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER  && event.getAction() == KeyEvent.ACTION_DOWN) {

                    if ( ((EditText)v).getLineCount() >= 7 )
                        return true;
                }

                return false;
            }
        });
        profileDescription.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(isInternetAvailable()) {
                    eDescription.setVisibility(View.VISIBLE);
                    editButton.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                    String description = profileDescription.getText().toString();
                    profileDescription.setVisibility(View.INVISIBLE);
                    eDescription.setText(description);
                    return true;
                }
                else {
                    Log.e(TAG,"No internet connection for this function to work");
                    Toast.makeText(context,"No internet connection. Function is disabled",Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(activity);
                String description = eDescription.getText().toString();
                db.collection("users").document(auth.getCurrentUser().getUid()).update("description", description).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(PhotoActivity.this,"Profile picture updated succesfully!",Toast.LENGTH_LONG).show();
                        editButton.setVisibility(View.GONE);
                        deleteButton.setVisibility(View.GONE);
                        eDescription.setVisibility(View.GONE);
                        profileDescription.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PhotoActivity.this, "Error at updating profile description. Try again or cancel!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                eDescription.setVisibility(View.GONE);
                profileDescription.setVisibility(View.VISIBLE);
                hideKeyboard(activity);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_photo);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //add also delete option of profile picture
        profileListener = new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen to profile picture changes failed.", e);
                    Toast.makeText(PhotoActivity.this, "Profile picture not loaded!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists() ) {
                    userName = (String) documentSnapshot.get("name");
                    profileName.setText(userName);
                    if (documentSnapshot.get("description") != null) {
                        profileDescription.setText((String) documentSnapshot.get("description"));
                    }
                    if( documentSnapshot.get("downloadImgPath") != null) {
                        Log.d(TAG, "Download url for profile picture is " + (String) documentSnapshot.get("downloadImgPath"));
                        profilePhotoImgPath = (String) documentSnapshot.get("img_path");
                        if (profilePhotoPath == null || profilePhotoPath.equals(""))
                            profilePhotoPath = (String) documentSnapshot.get("downloadImgPath");
                        Picasso.with(context)
                                .load(profilePhotoPath) // thumnail url goes here
                                .resize(300, 300)
                                .centerCrop()
                                .placeholder(R.drawable.placeholder_img)
                                .error(R.drawable.img_person)
                                .into(profileImg);
                    }
                    else {
                        Picasso.with(context)
                                .load(R.drawable.img_person) // thumnail url goes here
                                .resize(300, 300)
                                .centerCrop()
                                .into(profileImg);
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
                if (p.imgPath == null) return;
                mStorage.child(p.imgPath).getMetadata().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        databaseReference.child("posts").child(auth.getCurrentUser().getUid()).child(p.timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DB record deleted because storage doesn't exists");
                            }
                        });
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
                                return o1.timestamp.compareTo(o2.timestamp);
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
                mStorage.child(p.imgPath).getMetadata().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        databaseReference.child("posts").child(auth.getCurrentUser().getUid()).child(p.timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DB record deleted because storage doesn't exists");
                            }
                        });
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
                                return o1.timestamp.compareTo(o2.timestamp);
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
                        return o1.timestamp.compareTo(o2.timestamp);
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


        //add photo without boolean, doesn't make any sense
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInternetAvailable()) {
                    profile_en = false;
                    mLocation = null;
                    if (mImageUri != null) createDialogForUpload();
                    else add_photo();
                }
                else {
                    Log.e(TAG,"No internet connection for this function to work");
                    Toast.makeText(context,"No internet connection. Function is disabled",Toast.LENGTH_LONG).show();
                }
            }
        });
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetAvailable()) {
                    mLocation = null;
                    profile_en = true;
                    add_photo();
                }
                else {
                    Log.e(TAG,"No internet connection for this function to work");
                    Toast.makeText(context,"No internet connection. Function is disabled",Toast.LENGTH_LONG).show();
                }
            }
        });
        profileImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "Long press profile picture");
                if(!isInternetAvailable()) {
                    Log.e(TAG,"No internet connection for this function to work");
                    Toast.makeText(context,"No internet connection. Function is disabled",Toast.LENGTH_LONG).show();
                    return true;
                }
                if(profilePhotoImgPath==null) return true;
                final AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(PhotoActivity.this);
                myAlertDialog.setTitle("Delete profile picture");
                myAlertDialog.setMessage("Do you want to delete your profile picture?");

                myAlertDialog.setNeutralButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                myAlertDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                HashMap<String,Object> map = new HashMap<String,Object>();
                                map.put("img_path",null);
                                map.put("downloadImgPath",null);
                                db.collection("users").document(auth.getCurrentUser().getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(context,"Profile picture deleted",Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            Toast.makeText(context,"Error at deleting profile picture",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                                int remove_from_db = 1;
                                for(PhotoPost p:photoList) {
                                    if(p.downloadImgPath.equals(profilePhotoPath)) remove_from_db = 0;
                                }
                                if(remove_from_db == 1) {
                                    Log.d(TAG,"Deleting the profile picture from storage also, "+profilePhotoImgPath+", " +profilePhotoPath);
                                    mStorage.child(profilePhotoImgPath).delete();
                                }
                                profilePhotoPath = null;
                                profilePhotoImgPath = null;
                            }
                        });

                myAlertDialog.show();
                return true;
            }
        });

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void checkDeletePost(final int position) {
        final AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
        myAlertDialog.setTitle("Delete post");
        myAlertDialog.setMessage("Are you sure you want to delete this post?");
        mProgress.setTitle("Deleting...");

        myAlertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        mProgress.show();
                        final PhotoPost p = photoList.get(position);
                        //check if it is profile picture also
                        if (p.downloadImgPath.equals(profilePhotoPath)) { //delete only the post
                            databaseReference.child("posts").child(auth.getCurrentUser().getUid()).child(p.timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Photo deleted from db also");
                                    Toast.makeText(context, "Photo deleted successfully", Toast.LENGTH_LONG).show();
                                    mProgress.dismiss();
                                }
                            });//add failure
                            return;
                        }
                        mStorage.child(p.imgPath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Photo deleted from storage");
                                databaseReference.child("posts").child(auth.getCurrentUser().getUid()).child(p.timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Photo deleted from db also");
                                        Toast.makeText(context, "Photo deleted successfully", Toast.LENGTH_LONG).show();
                                        mProgress.dismiss();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to delete photo from storage");
                                e.printStackTrace();
                                mProgress.dismiss();
                                Toast.makeText(context, "Failed to delete photo.Try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
        myAlertDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                });
        myAlertDialog.show();
    }

    private void require_permission() {
        //require permission
        //try to add a listener
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CAMERA_REQUEST);
        } else {
            camera_en = true;
            storage_en = true;
        }

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
//                    LOCATION_REQUEST);
//            mLocation = null;
//        } else {
//            location_en = true;
//        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    public static boolean isReachable(String targetUrl) {


        try
        {
            HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(
                    targetUrl).openConnection();
            httpUrlConnection.setRequestMethod("HEAD");
            int responseCode = httpUrlConnection.getResponseCode();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception noInternetConnection)
        {
            return false;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        isInternetAvailableOnce();
    }
    private void isInternetAvailableOnce() {
        final boolean[] internetActive = new boolean[1];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(checkConn) {
                    if(internetEn==false)
                        internetEn = isReachable("https://www.google.com/");
                }

            }
        });
        t.start();

    }
    private boolean isInternetAvailable() {
        return internetEn;
    }

    private void add_photo() {
        final Intent intent = new Intent();
        final AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myAlertDialog.setCancelable(true);
                        mLocation = null;
                        mImageUri = null;
                    }
                });
        myAlertDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PICTURE_REQUEST);

                    }
                });
        myAlertDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go

                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                                Log.e(TAG, "Error when creating the file for the photo");
                                myAlertDialog.setCancelable(true);
                                return;
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                mImageUri = Uri.fromFile(photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                //stackoverflow
                                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                StrictMode.setVmPolicy(builder.build());
                                startActivityForResult(intent, CAMERA_REQUEST);
                                Log.i(TAG, "Taking and uploading photo from camera");
                            } else {
                                Toast.makeText(context, "Photo not saved.Please try again", Toast.LENGTH_LONG).show();
                                myAlertDialog.setCancelable(true);
                                arg0.dismiss();
                            }
                        }


                    }
                });
        if(!(camera_en && storage_en)) {
            require_permission();
        }
        else myAlertDialog.show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        image.deleteOnExit();
        return image;
    }

    private void uploadImage(final Uri imageUri, final PhotoPost p) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReference();
        StorageReference reference = storageReference.child("images/" + imageUri.getLastPathSegment());

        reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(final StorageMetadata storageMetadata) { //image exists already, just DB
                FirebaseStorage.getInstance().getReference().child("images/" + imageUri.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i(TAG, "Image was saved to Storage");
                        if (p != null) p.imgPath = storageMetadata.getPath();
                        updateDBAfterUpload(imageUri, p, uri);
                        mLocation = null;
                        profile_en = false;
                        mProgress.dismiss();
                    }
                }); //without fail because if this fail it will surely trigger the below one
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int errorCode = ((StorageException) e).getErrorCode();
                if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    doUploadTask(imageUri, p);
                    mLocation = null;
                    profile_en = false;
                }

            }
        });


    }

    private String doUploadTask(final Uri imageUri, final PhotoPost p) {
        String path = null;
        try {
            mProgress.setTitle("Uploading...");
            mProgress.show();
            InputStream stream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            path = "images/" + imageUri.getLastPathSegment();
            if (p != null) p.imgPath = path;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data_img = baos.toByteArray();
            final FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference();
            final StorageReference reference = storageReference.child(path);
            final UploadTask uploadTask = reference.putBytes(data_img);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseStorage.getInstance().getReference().child("images/" + imageUri.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.i(TAG, "Got url, update DB");
                            updateDBAfterUpload(imageUri, p, uri);
                            mProgress.dismiss();
                        }
                    }); //without fail because if this fail it will surely trigger other fail

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Image wasn't saved. Exception: " + e.getMessage());
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_LONG).show();
                    profile_en = false;
                    mProgress.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Still here");
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    mProgress.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception at getting the bitmap from camera or other");
            e.printStackTrace();
            Toast.makeText(context, "Image not taken. Try again!", Toast.LENGTH_LONG).show();
            mImageUri = null;
            profile_en = false;
            if (photoFile != null) photoFile.delete();
            photoFile = null;
        } finally {
            return path;
        }

    }

    private void updateDBAfterUpload(Uri imageUri, final PhotoPost p, Uri uri) {
        if (p != null && profile_en == false) {
            p.downloadImgPath = uri.toString();
            p.imgPath = "images/" + imageUri.getLastPathSegment();
            databaseReference.child("posts").child(auth.getCurrentUser().getUid()).child(p.timestamp).setValue(p).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Photo uploaded successfully", Toast.LENGTH_LONG).show();
                    mImageUri = null;
                    mLocation = null;
                    if (photoFile != null) photoFile.delete();
                    photoFile = null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error at uploading after storage of photo");
                    e.printStackTrace();
                    Toast.makeText(context, "Error at uploading photo", Toast.LENGTH_LONG).show();
                    FirebaseStorage.getInstance().getReference().child(p.imgPath).delete().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to delete file after uploaded. Creep.");
                            e.printStackTrace();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "File after uploaded succcesfully deleted");
                        }
                    });
                }
            });
        } else {
            Log.d(TAG, "Putting the data for profile picture");
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("img_path", "images/" + imageUri.getLastPathSegment());
            map.put("downloadImgPath", uri.toString());
            db.collection("users").document(auth.getCurrentUser().getUid()).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(PhotoActivity.this, "Profile picture updated succesfully!", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PhotoActivity.this, "Error at updating profile picture. Try again later!", Toast.LENGTH_LONG).show();
                }
            });
            addProfileImgToUser(auth.getCurrentUser(),uri);
            mImageUri = null;
            profile_en = false;
        }
    }

    private void addProfileImgToUser(FirebaseUser user,Uri uri) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile picture updated.");
                        }
                        else {
                            //Toast.makeText(context,"Username failed to save. Update it in setting menu",Toast.LENGTH_LONG).show();
                            Log.e(TAG,"User profile picture update failed");
                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int ok = 0;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST: {
                    if (mImageUri != null) ok = 1;
                    break;
                }
                case GALLERY_PICTURE_REQUEST: {
                    mImageUri = data.getData();
                    if (mImageUri != null) ok = 1;
                    else ok = 0;
                    break;
                }
                case REQUEST_CHECK_SETTINGS: {
                    buttonLoc_en = true;
                    ok = 0;
                    break;
                }
                default: {
                    ok = 0;
                }
                ;
            }
            if (ok == 1) {
                //temporarly solution
                if (profile_en == true) {
                    Log.d(TAG, "Here in result " + mImageUri.toString());
                    uploadImage(mImageUri, null);
                } else {
                    require_permission();
                    createDialogForUpload();
                }

            }
        } else {
            if (requestCode == REQUEST_CHECK_SETTINGS) buttonLoc_en = false;
            //Toast.makeText(this,"Cannot take picture(from camera or galery). Function disabled!",Toast.LENGTH_LONG).show();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

    }

    private Boolean gpsEnabled() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    private void createDialogForUpload() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.layout_dialog_photo, null);
        final EditText tDescription = dialogLayout.findViewById(R.id.eDescription);
        final ImageView image = dialogLayout.findViewById(R.id.dialogPhotoImg);
        final AlertDialog dialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                if (mImageUri != null) {
                    PhotoPost p = new PhotoPost();
                    p.uid = auth.getCurrentUser().getUid();
                    p.timestamp = getCurrentTimeDate();
                    p.description = tDescription.getText().toString();
                    p.downloadImgPath = null;
                    if(mLocation!=null) {
                        p.latitude = String.valueOf(mLocation.getLatitude());
                        p.longitude = String.valueOf(mLocation.getLongitude());
                    }
                    uploadImage(mImageUri, p);

                } else {
                    Toast.makeText(context, "Photo doesn't exist. Try again!", Toast.LENGTH_LONG).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder.setCancelable(true);
                mImageUri = null;
                mLocation = null;
                if (photoFile != null) photoFile.delete();
                photoFile = null;
            }
        });
        dialog = builder.create();

        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tLocation = (TextView) dialogLayout.findViewById(R.id.tLocation);
        checkBox = ((CheckBox) dialogLayout.findViewById(R.id.cLocation));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    LocationSettingsRequest.Builder locBuilder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(mLocationRequest);
                    SettingsClient client = LocationServices.getSettingsClient(context);
                    Task<LocationSettingsResponse> task = client.checkLocationSettings(locBuilder.build());
                    task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            boolean gps_enabled = false;
                            boolean network_enabled = false;

                            try {
                                gps_enabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                            } catch(Exception ex) {}

                            try {
                                network_enabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                            } catch(Exception ex) {}

                            if(network_enabled && isInternetAvailable()) {
                                mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocListener);
                            }
                            else if(gps_enabled) {
                                mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
                            }
                            else {
                                checkBox.setChecked(false);
                                Intent gpsOptionsIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                            }
                        }
                    });

                    task.addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mLocation = null;
                            checkBox.setChecked(false);
                            if (e instanceof ResolvableApiException) {
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    ResolvableApiException resolvable = (ResolvableApiException) e;
                                    resolvable.startResolutionForResult(PhotoActivity.this,
                                            REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sendEx) {
                                    // Ignore the error.

                                }
                            }
                            else {
                                checkBox.setEnabled(false);
                            }
                        }
                    });
                }
                else {
                    mLocation = null;
                    tLocation.setText("");
                    mLocManager.removeUpdates(mLocListener);
                }
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mLocManager.removeUpdates(mLocListener);
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                if(mImageUri != null) {
                    Picasso.with(context).load(mImageUri).resize(250, 0).centerCrop()
                            .placeholder(R.drawable.placeholder_img)
                            .error(R.drawable.error_img)
                            .into(image);
                }
                else {
                    d.dismiss();
                }
            }
        });

        dialog.show();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Location Requested");
            mLocation = null;
            //dialog.dismiss();
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST);

            return;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    camera_en = true;
                    storage_en = true;
                    add_photo();
                } else {
                    camera_en = false;
                    storage_en = false;
                }
                return;
            }
            case GALLERY_PICTURE_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storage_en = true;
                } else {
                    storage_en = false;
                }
                return;
            }
            case LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location_en = true;
                    checkBox.setEnabled(true);
                } else {
                    location_en = false;
                    checkBox.setEnabled(false);
                }
                //createDialogForUpload();
                return;
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_photo);
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
            nextActivity = CalendarActivity.class;
        } else if (id == R.id.nav_friends) {
            nextActivity = FriendsActivity.class;
        } else if (id == R.id.nav_requests) {
            nextActivity = Requests.class;
        } else if (id == R.id.nav_settings) {
            nextActivity = MainActivity.class;
        } else if (id == R.id.nav_photos) {
            nextActivity = PhotoActivity.class;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_photo);
        drawer.closeDrawer(GravityCompat.START);
        if(nextActivity!=null && nextActivity!=this.getClass()) {
            checkConn = false;
            startActivity(new Intent(this, nextActivity));
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
        checkConn = true;
        profileRegistration = db.collection("users").document(auth.getCurrentUser().getUid()).addSnapshotListener(profileListener);
        auth.addAuthStateListener(authListener);
        databaseReference.child("posts").child(auth.getCurrentUser().getUid()).addChildEventListener(photoEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        checkConn = false;
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        if(profileRegistration!=null) {
            profileRegistration.remove();
            profileRegistration = null;
        }
        if(photoEventListener!= null) {
            databaseReference.child("posts").child(auth.getCurrentUser().getUid()).removeEventListener(photoEventListener);
        }
    }

    public static String getCurrentTimeDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return sdfDate.format(now);
    }



}
