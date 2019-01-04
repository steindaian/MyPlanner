package upt.myplanner.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.maps.android.clustering.ClusterManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import upt.myplanner.MainActivity;
import upt.myplanner.R;
import upt.myplanner.login.LoginActivity;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private int position = -1;
    private String uid;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseDatabase database;
    private ArrayList<PhotoPost> photoList = new ArrayList<PhotoPost>();
    public static final String TAG = "MapsActivity";
    private boolean loaded = false;
    private String username;
    private ClusterManager<MyClusterMapItem> mClusterManager;
    private Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                    finish();
                }
                else {
                    uid = auth.getCurrentUser().getUid();
                }
            }
        };

        if(auth.getCurrentUser().getUid() == null || auth.getCurrentUser().getUid().isEmpty()) {
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            finish();
        }
        else {
            uid = auth.getCurrentUser().getUid();
        }

        try {
            if (getIntent().getExtras() != null) {
                position = getIntent().getExtras().getInt("Position", -1);
                uid = getIntent().getStringExtra("Uid");
                username = getIntent().getStringExtra("Username");
                photoList = (ArrayList<PhotoPost>) getIntent().getSerializableExtra("List");
                if(photoList==null) {
                    startActivity(new Intent(MapsActivity.this, MainActivity.class));
                    finish();
                }
            }
            else {
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
                finish();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            this.onBackPressed();
            return;
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        //create the clusterManager and customize
//        mClusterManager = new ClusterManager<MyClusterMapItem>(this, mMap);
//
//        // Point the map's listeners at the listeners implemented by the cluster
//        // manager.
//        mMap.setOnCameraIdleListener(mClusterManager);
//        mMap.setOnMarkerClickListener(mClusterManager);
//
//        //addItems to cluster
//        for(int i=0;i<photoList.size();i++) {
//            if(position!=i) {
//                MyClusterMapItem item = new MyClusterMapItem(Double.valueOf(photoList.get(i).latitude), Double.valueOf(photoList.get(i).longitude));
//                mClusterManager.addItem(item);
//            }
//        }
//
//        //TODO: add design to clusters




        addItems();


    }

    private void addItems() {
        final HashMap<String,Uri> markerImgUri = new HashMap<String, Uri>();
        Marker posMarker = null;
        final View mCustomMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
        mMap.setInfoWindowAdapter(new PopupAdapter(MapsActivity.this,getLayoutInflater(),markerImgUri));
        mMap.setOnInfoWindowClickListener(this);
        for(int i=0;i<photoList.size();i++) {
            if(photoList.get(i).latitude!=null && photoList.get(i).longitude!=null && !photoList.get(i).latitude.isEmpty() && !photoList.get(i).longitude.isEmpty() ) {
                final LatLng item = new LatLng(Double.valueOf(photoList.get(i).latitude), Double.valueOf(photoList.get(i).longitude));
                final PhotoPost p = photoList.get(i);
                final int index = i;

                //very important
                final View markerView = getLayoutInflater().inflate(R.layout.custom_marker,null);
                final ImageView markerIcon = (ImageView) markerView.findViewById(R.id.profile_image_marker);
                Picasso.with(getApplicationContext()).load(Uri.parse(photoList.get(i).downloadImgPath))
                        .resize(40,40)
                        .error(R.drawable.error_img)
                        .placeholder(R.drawable.placeholder_img)
                        .into(markerIcon, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Marker m = mMap.addMarker(new MarkerOptions().position(item));
                                m.setTag(p);
                                m.setTitle(getLocationName(Double.valueOf(p.latitude), Double.valueOf(p.longitude)));
                                markerImgUri.put(m.getId(),Uri.parse(p.downloadImgPath));
                                m.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context,markerView)));
                                if(position == index) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(),5));
                                    m.showInfoWindow();
                                }
                            }

                            @Override
                            public void onError() {
                                final Marker m = mMap.addMarker(new MarkerOptions().position(item));
                                m.setTag(p);
                                m.setTitle(getLocationName(Double.valueOf(p.latitude), Double.valueOf(p.longitude)));
                                markerImgUri.put(m.getId(),Uri.parse(p.downloadImgPath));
                                m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.error_img));
                                if(position == index) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(),5));
                                    m.showInfoWindow();
                                }
                            }
                        });
            }
        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
        Intent intent = new Intent(MapsActivity.this,PostActivity.class);
        intent.putExtra("Uid",uid);
        intent.putExtra("Username",username);
        intent.putExtra("List",photoList);
        intent.putExtra("Position",photoList.indexOf(marker.getTag()));
        startActivity(intent);

    }

    private String getLocationName(Double lat,Double lon) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            if(lon != null && lat!=null) {
                addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                return addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
            }
            else {
               return "No-location";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "No-location";
        }
    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
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

//    public void onResume() {
//        super.onResume();
//        if(mMap!=null) {
//            mMap.clear();
//            addItems();
//        }
//    }

}
