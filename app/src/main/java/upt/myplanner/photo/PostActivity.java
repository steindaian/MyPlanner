package upt.myplanner.photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import upt.myplanner.MainActivity;
import upt.myplanner.R;
import upt.myplanner.friends.UserPhotosActivity;
import upt.myplanner.login.LoginActivity;

public class PostActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public static ArrayList<PhotoPost> photoList;
    int startPosition;
    public String uid;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseDatabase database;
    private FirebaseFirestore dbStore;
    public Context context = this;
    public static final String TAG = "PostActivity";
    public String userNameString;
    public static boolean internetAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        dbStore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else {
            try {
                uid = auth.getCurrentUser().getUid();
            }
            catch (Exception e) {
                e.printStackTrace();
                this.onBackPressed();
                finish();
            }
        }
        setContentView(R.layout.activity_post);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent intent = new Intent(PostActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };


        try {
            if(getIntent().getExtras() != null) {
                uid = getIntent().getStringExtra("Uid");
                startPosition = getIntent().getIntExtra("Position", -1);
                if (startPosition == -1) {
                    startPosition = 0;
                }
                userNameString = getIntent().getStringExtra("Username");

                photoList = (ArrayList<PhotoPost>) getIntent().getSerializableExtra("List");
                internetAvailable = getIntent().getBooleanExtra("Internet",false);
            }
            else {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            this.onBackPressed();
            return;
        }


        setTitle(userNameString+" Photos");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(startPosition);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(TAG,"The uid of the page is: "+uid);
                Log.d(TAG,"My uid is: "+auth.getCurrentUser().getUid());
                if(!uid.equals(auth.getCurrentUser().getUid())) {
                    Log.d(TAG,"Here on back");
                    Intent intent = getParentActivityIntent();
                    intent.setClass(this,UserPhotosActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TAS);
                    startActivity(intent);
                }

                else
                    startActivity(new Intent(this,PhotoActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    /**
     * A placeholder fragment containing a simple view.
     */


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position,context,userNameString,uid);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return photoList.size();
        }
    }
}
@SuppressLint("ValidFragment")
class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static Context context;
    private static PlaceholderFragment instance;
    private static String userNameString;
    private static String uid;
    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber,Context myContext,String myUser,String myUid) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        context = myContext;
        userNameString = myUser;
        uid = myUid;
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        final int position = getArguments().getInt(ARG_SECTION_NUMBER);

        TextView userName = (TextView) rootView.findViewById(R.id.postUserName);
        TextView description = (TextView) rootView.findViewById(R.id.postDescription);
        TextView Location = (TextView) rootView.findViewById(R.id.postLocation);
        ImageView img = (ImageView) rootView.findViewById(R.id.postImg);

        //have to modify dimensions
        Picasso.with(context)
                .load(PostActivity.photoList.get(position).downloadImgPath) // thumnail url goes here
                .placeholder(R.drawable.placeholder_img)
                .error(R.drawable.error_img)
                .fit()
                .centerCrop()
                .into(img);

        userName.setText(userNameString+":");
        description.setText(PostActivity.photoList.get(position).description);
        if(PostActivity.photoList.get(position).longitude != null && PostActivity.photoList.get(position).latitude!=null) {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation( Double.valueOf(PostActivity.photoList.get(position).latitude), Double.valueOf(PostActivity.photoList.get(position).longitude), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                Location.setText(addresses.get(0).getLocality()+", "+addresses.get(0).getCountryName());
                Location.setPaintFlags(Location.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
                if(PostActivity.internetAvailable) {
                    Location.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), MapsActivity.class);
                            intent.putExtra("Position", position);
                            intent.putExtra("Uid", uid);
                            intent.putExtra("Username", userNameString);
                            intent.putExtra("List", PostActivity.photoList);
                            intent.putExtra("Internet",PostActivity.internetAvailable);
                            startActivity(intent);
                        }
                    });
                }
                else {
                    Log.e("PostActivity","No internet connection");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Location.setText("No location");
            }
        }
        else{
            Location.setText("No location");
        }

        Log.d(PostActivity.TAG,"Description is: "+PostActivity.photoList.get(position).description+" , name is: "+ userNameString+" , location is: "+Location.getText().toString());
        return rootView;
    }
}
