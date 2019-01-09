package upt.myplanner.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import upt.myplanner.R;
import upt.myplanner.login.LoginActivity;

import static upt.myplanner.photo.PhotoActivity.getCurrentTimeDate;

public class AddEventActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DatabaseReference databaseReference;
    private String uid;
    private FirebaseAuth.AuthStateListener authListener;

    public static final String LOG_TAG =
            AddEventActivity.class.getSimpleName();
    private TextView tName;
    private TextView tDescription;
    private TextView tStart;
    private TextView tEnd;
    private EditText eStart;
    private EditText eEnd;
    private EditText eDescription;
    private EditText eName;
    private String year;
    private String month;
    private String day;
    private TextView tDate;
    private String start;
    private String end;
    private String name;
    private String description;
    private String timestamp;

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
                    Intent intent = new Intent(AddEventActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
        if(getIntent().getExtras() !=null) {
            year = getIntent().getStringExtra("year");
            month = getIntent().getStringExtra("month");
            day = getIntent().getStringExtra("day");

            start = getIntent().getStringExtra("start");
            end = getIntent().getStringExtra("end");
            name = getIntent().getStringExtra("name");
            description = getIntent().getStringExtra("description");
            timestamp = getIntent().getStringExtra("timestamp");
        }
        else {
            this.onBackPressed();
            finish();
        }
        setContentView(R.layout.activity_add_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tName = (TextView) findViewById(R.id.tName);
        tDescription = (TextView) findViewById(R.id.tDescr);
        tStart = (TextView) findViewById(R.id.tStart);
        tEnd = (TextView) findViewById(R.id.tEnd);
        eStart = (EditText) findViewById(R.id.eStart);
        eEnd = (EditText) findViewById(R.id.eEnd);
        eDescription = (EditText) findViewById(R.id.eDescr);
        eName = (EditText) findViewById(R.id.eName);
        tDate = (TextView) findViewById(R.id.tDate);
        tDate.setText(day + "/" + month + "/" + year);
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

        if(timestamp!=null) {
            eName.setText(name);
            eDescription.setText(description);
            eStart.setText(start);
            eEnd.setText(end);
        }
        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if(eName.getText().toString().equals("") || eStart.getText().toString().equals("") || eEnd.getText().toString().equals("")) {
                    Toast.makeText(AddEventActivity.this,"Fields aren't allowed to be empty, except description",Toast.LENGTH_LONG).show();
                    return;
                }
                if (!checkTime(eStart.getText().toString()) && !checkTime(eEnd.getText().toString())) {
                    Toast.makeText(AddEventActivity.this, "Start Time or End Time are not well formated", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!checktimings(eStart.getText().toString(),eEnd.getText().toString())) {
                    Toast.makeText(AddEventActivity.this, "Start Time is greater or equal than End Time", Toast.LENGTH_LONG).show();
                    return;
                }

                MyEvent event = new MyEvent(eName.getText().toString(), eDescription.getText().toString(), eStart.getText().toString(), eEnd.getText().toString(), year, month, day);
                event.uid = uid;
                if (timestamp != null) {
                    event.timestamp = timestamp;
                    Map<String, Object> map = getMap(event);
                    if (map == null) return;
                    db.collection("events").document(timestamp).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                AddEventActivity.this.onBackPressed();
                            } else {
                                Log.e(LOG_TAG, "Error at creating a new event");
                                Toast.makeText(AddEventActivity.this, "Error at creating a new event", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    event.timestamp = getCurrentTimeDate();
                    db.collection("events").document(getCurrentTimeDate()).set(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                AddEventActivity.this.onBackPressed();
                            } else {
                                Log.e(LOG_TAG, "Error at creating a new event");
                                Toast.makeText(AddEventActivity.this, "Error at creating a new event", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });


    }


    public Map<String, Object> getMap(Object o) {
        Map<String, Object> result = new HashMap<String, Object>();
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                result.put(field.getName(), field.get(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    public boolean checkTime(String time) {
        try {
            DateTimeFormatter strictTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);
            LocalTime.parse(time, strictTimeFormatter);
            return true;
        } catch (DateTimeParseException | NullPointerException e) {
            return false;
        }
    }


    public boolean checktimings(String startTime, String endTime) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        startTime = eStart.getText().toString();
        endTime = eEnd.getText().toString();
        try {
            Date date1 = sdf.parse(startTime);
            Date date2 = sdf.parse(endTime);

            if(date1.before(date2)) {
                return true;
            } else {

                return false;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
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
