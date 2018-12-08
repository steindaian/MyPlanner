package upt.myplanner.photo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import upt.myplanner.MainActivity;
import upt.myplanner.R;

public class PhotoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE_REQUEST = 1;
    private static final int LOCATION_REQUEST = 3;
    private final Context context = this;
    private final Activity activity = this;
    private String currImagePath;
    File photoFile = null;
    private Uri mImageUri = null;
    private final double THUMBNAIL_SIZE = 1.0 ;
    private boolean camera_en = false;
    private boolean storage_en = false;
    private boolean location_en = false;
    private ProgressDialog mProgress;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;

    private final String TAG = "PhotoActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //require permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST);
        }
        else {
            camera_en = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    GALLERY_PICTURE_REQUEST);
        }
        else {
            storage_en = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            mLocation = null;
        }
        else {
            location_en = true;
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                if(storage_en) {
                    myAlertDialog.setPositiveButton("Gallery",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    Intent intent = new Intent();
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PICTURE_REQUEST);

                                }
                            });
                }
                if(camera_en) {
                    myAlertDialog.setNegativeButton("Camera",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    // Ensure that there's a camera activity to handle the intent
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        // Create the File where the photo should go

                                        try {
                                            photoFile = createImageFile();
                                        } catch (IOException ex) {
                                            // Error occurred while creating the File
                                            Log.e(TAG,"Error when creating the file for the photo");
                                            myAlertDialog.setCancelable(true);
                                            return;
                                        }
                                        // Continue only if the File was successfully created
                                        if (photoFile != null) {
                                            mImageUri = Uri.fromFile(photoFile);
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                            //stackoverflow
                                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                            StrictMode.setVmPolicy(builder.build());
                                            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                                            Log.i(TAG,"Taking and uploading photo from camera");
                                        }
                                        else {
                                            Toast.makeText(context,"Photo not saved.Please try again",Toast.LENGTH_LONG).show();
                                            myAlertDialog.setCancelable(true);

                                        }
                                    }



                                }
                            });
                }
                myAlertDialog.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        // Save a file: path for use with ACTION_VIEW intents
        currImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int ok = 0;
        if(resultCode == RESULT_OK ) {
            switch (requestCode) {
                case CAMERA_REQUEST: {
                    if(mImageUri != null) ok =1;
                    break;
                }
                case GALLERY_PICTURE_REQUEST: {
                    mImageUri = data.getData();
                    if(mImageUri != null) ok = 1;
                    break;
                }
                default: ok =0; break;
            }
            if(ok == 1) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mImageUri != null) {
                            uploadImage(mImageUri);
                            //take location description time and image name and save to the user in db
                        }
                        else {
                            Toast.makeText(context,"Error at uploading the photo",Toast.LENGTH_LONG).show();
                        }
                        mImageUri = null;
                        mLocation = null;
                        if(photoFile!= null) photoFile.delete();
                        photoFile = null;
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        builder.setCancelable(true);
                        mImageUri = null;
                        mLocation = null;
                        if(photoFile!= null) photoFile.delete();
                        photoFile = null;
                    }
                });
                final AlertDialog dialog = builder.create();
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.layout_dialog_photo, null);
                dialog.setView(dialogLayout);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                ((CheckBox)dialogLayout.findViewById(R.id.cLocation)).setEnabled(location_en);
                ((CheckBox)dialogLayout.findViewById(R.id.cLocation)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_REQUEST);
                                mLocation = null;
                                dialog.setCancelable(true);
                            }
                            else {
                                location_en = true;
                                mFusedLocationClient.getLastLocation()
                                        .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                mLocation = location;
                                                Log.e(TAG,"Latitude is "+location.getLatitude()+" and longitutde is "+location.getLongitude());
                                            }
                                        });
                            }
                        }
                        else {
                            mLocation = null;
                        }
                    }
                });

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface d) {
                        ImageView image = (ImageView) dialog.findViewById(R.id.dialogPhotoImg);
                                Picasso.with(context).load(mImageUri).resize(250,0).centerCrop()
                                        .placeholder(R.mipmap.ic_launcher_round)
                                        .error(R.mipmap.ic_launcher)
                                        .into(image);
                    }
                });

                dialog.show();
            }
        }
        else if(resultCode != RESULT_CANCELED){
            Toast.makeText(this,"Cannot take picture(from camera or galery). Function disabled!",Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(Uri imageUri){
        try{
            Log.i(TAG, imageUri.toString());

            mProgress.setTitle("Uploading...");
            mProgress.show();
            InputStream stream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            String path = "images/" + imageUri.getLastPathSegment();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data_img = baos.toByteArray();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference reference = storageReference.child(path);
            UploadTask uploadTask = reference.putBytes(data_img);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "Image was saved");
                    Toast.makeText(context,"Photo uploaded",Toast.LENGTH_LONG).show();
                    mProgress.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Image wasn't saved. Exception: " + e.getMessage());
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_LONG).show();
                    mProgress.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    mProgress.setMessage("Uploaded "+(int)progress+"%");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception at getting the bitmap from camera");
            e.printStackTrace();
        }
        finally {
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
                } else {
                    camera_en = false;
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
                } else {
                    location_en = false;
                }
                return;
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
