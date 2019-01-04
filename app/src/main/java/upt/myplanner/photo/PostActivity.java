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
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import upt.myplanner.MainActivity;
import upt.myplanner.R;
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
    public static String uid;
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
        setContentView(R.layout.activity_post);

        database = FirebaseDatabase.getInstance();
        dbStore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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
                else {

                }
            }
        };
        if(auth.getCurrentUser().getUid() == null || auth.getCurrentUser().getUid().isEmpty()) {
            startActivity(new Intent(PostActivity.this, LoginActivity.class));
            finish();
        }
        else {
            uid = auth.getCurrentUser().getUid();
        }

        try {
            if(getIntent().getExtras() != null) {
                if (getIntent().getStringExtra("Uid") != null && !getIntent().getStringExtra("Uid").equals("")) {
                    uid = getIntent().getStringExtra("Uid");
                }
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
            return PlaceholderFragment.newInstance(position,context,userNameString);
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
    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber,Context myContext,String myUser) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        context = myContext;
        userNameString = myUser;
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

        userName.setText(userNameString);
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
                            intent.putExtra("Uid", PostActivity.uid);
                            intent.putExtra("Username", userNameString);
                            intent.putExtra("List", PostActivity.photoList);
                            startActivity(intent);
                        }
                    });
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
