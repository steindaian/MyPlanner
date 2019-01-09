package upt.myplanner.calendar;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import upt.myplanner.R;
import upt.myplanner.login.LoginActivity;

public class EventActivity extends AppCompatActivity {
    public static final String LOG_TAG =
            EventActivity.class.getSimpleName();
    public String Year;
    public String Month;
    public String Day;
    private RecyclerView listEvents;
    private final List<MyEvent> events = new ArrayList<MyEvent>();
    private Map<String,MyEvent> eventMap = new HashMap<String,MyEvent>();
    private EventsViewAdapter mAdapter;
    private FirebaseFirestore db;
    private String TAG="EventActivity";
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String uid;
    private FirebaseAuth.AuthStateListener authListener;

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
            finish();
        }
        else {
            try {
                uid = auth.getCurrentUser().getUid();
            }
            catch (Exception e) {
                e.printStackTrace();
                super.onBackPressed();
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
                    Intent intent = new Intent(EventActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(getIntent().getExtras() !=null) {
            Year = getIntent().getStringExtra("year");
            Month = getIntent().getStringExtra("month");
            Day = getIntent().getStringExtra("day");


        }
        else{
            Log.d(LOG_TAG,"Printing all the events");

        }
        listEvents = (RecyclerView) findViewById(R.id.my_recycler_view);
        listEvents.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new EventsViewAdapter(this, events);
        listEvents.setAdapter(mAdapter);
        Query q;
        if(Year!=null && Month!=null && Day!=null) {
            q = db.collection("events").whereEqualTo("year",Year).whereEqualTo("month",Month).whereEqualTo("day",Day);
        }
        else {
            q = db.collection("events");
        }

        final TextView noEvents = findViewById(R.id.noEvent);
        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null) {
                    Log.e("EventActivity","Error on fetching data from db. "+e.getMessage());
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    MyEvent event = dc.getDocument().toObject(MyEvent.class);
                    event.uid = uid;
                    event.timestamp = dc.getDocument().getId();
                    switch (dc.getType()) {
                        case ADDED:
                            eventMap.put(dc.getDocument().getId(),event);
                            events.clear();
                            events.addAll(eventMap.values());
                            mAdapter.notifyDataSetChanged();
                            break;
                        case MODIFIED:
                            eventMap.put(dc.getDocument().getId(),event);
                            events.clear();
                            events.addAll(eventMap.values());
                            mAdapter.notifyDataSetChanged();
                            break;
                        case REMOVED:
                            eventMap.remove(dc.getDocument().getId());
                            events.clear();
                            events.addAll(eventMap.values());
                            mAdapter.notifyDataSetChanged();
                            break;
                    }
                    if(events.size()==0) {
                        noEvents.setVisibility(View.VISIBLE);

                    }
                    else{
                        noEvents.setVisibility(View.INVISIBLE);
                    }
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
}
