/**
 * Report new event
 */

package com.example.hamburger_w.eventreporter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class EventReportActivity extends AppCompatActivity {
    private static final String TAG = EventReportActivity.class.getSimpleName();
    private EditText mEditTextLocation;
    private EditText mEditTextTitle;
    private EditText mEditTextContent;
    private ImageView mImageViewSend;
    private ImageView mImageViewCamera;
    private ImageView mImageViewLocation;
    private DatabaseReference database;

    private LocationTracker mLocationTracker;
    private Activity mActivity;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static int RESULT_LOAD_IMAGE = 1; // gallery
    private ImageView img_event_picture;
    private Uri mImgUri;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // link to Android system
        setContentView(R.layout.activity_event_report);

        // initialization
        mEditTextLocation = (EditText) findViewById(R.id.edit_text_event_location);
        mEditTextTitle = (EditText) findViewById(R.id.edit_text_event_title);
        mEditTextContent = (EditText) findViewById(R.id.edit_text_event_content);
        mImageViewCamera = (ImageView) findViewById(R.id.img_event_camera);
        mImageViewSend = (ImageView) findViewById(R.id.img_event_report);
        mImageViewLocation = (ImageView) findViewById(R.id.img_event_location);
        database = FirebaseDatabase.getInstance().getReference(); // singleton pattern
        img_event_picture = (ImageView) findViewById(R.id.img_event_picture_capture);
        storage = FirebaseStorage.getInstance(); // singleton pattern
        storageRef = storage.getReference(); // singleton pattern

        mImageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = uploadEvent();
                if (mImgUri != null) {
                    uploadImage(key);
                    mImgUri = null;
                    img_event_picture.setVisibility(View.GONE);
                }
            }
        });

        // check if GPS enabled
        mActivity = this;
        mLocationTracker = new LocationTracker(mActivity);
        mLocationTracker.getLocation();
        final double latitude = mLocationTracker.getLatitude();
        final double longitude = mLocationTracker.getLongitude();

        // location image click
        mImageViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEditTextLocation.getText())) { // if mEditTestLocation is not empty, then clear the content
                                                                       // !! getText() return Editable
                    mEditTextLocation.setText("");
                } else { // if mEditTestLocation is empty, then get auto-location
                    new AsyncTask<Void, Void, Void>() {
                        private List<String> mAddressList = new ArrayList<String>();

                        @Override
                        protected Void doInBackground(Void... urls) {
                            mAddressList = mLocationTracker.getCurrentLocationViaJSON(latitude,longitude);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void input) {
                            if (mAddressList.size() >= 4) { // only use first result in results
                                mEditTextLocation.setText(mAddressList.get(0) + ", " + mAddressList.get(1) +
                                        ", " + mAddressList.get(2) + ", " + mAddressList.get(3));
                            }
                        }
                    }.execute();
                }
            }
        });

        // camera image click
        mImageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // implicit intent, because there are many ways to choose a picture
                                                                                        // need to define action and category, and pass to intent filter
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        // Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mAuth.signInAnonymously().addOnCompleteListener(this,  new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously", task.getException());
                }
            }
        });
    }

    /**
     * Go to another activity to choose a picture and return back to current activity
     * @param requestCode: what activity to transfer
     * @param resultCode: whether it's successful to get data
     * @param data: what data to receive
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData(); // show current file address path in machine
                img_event_picture.setVisibility(View.VISIBLE);
                img_event_picture.setImageURI(selectedImage);
                mImgUri = selectedImage; // mImgUri = path in machine
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * upload data and set path
     * attention:
     * when typing location, follow the form: <street>, <city>, <state>
     */
    private String uploadEvent() {
        String title = mEditTextTitle.getText().toString();
        String location = mEditTextLocation.getText().toString();
        String description = mEditTextContent.getText().toString();
        if (location.equals("") || description.equals("") ||
                title.equals("") || Utils.username == null) {
            return null;
        }
        //create event instance(without set imgUri)
        Event event = new Event();
        event.setTitle(title);
        event.setAddress(location);
        event.setDescription(description);
        event.setTime(System.currentTimeMillis());
        event.setLatitude(mLocationTracker.getLatitude());
        event.setLongitude(mLocationTracker.getLongitude());
        event.setUsername(Utils.username);
        // insert data:
        String key = database.child("events").push().getKey(); // 1: get reference to events node(collection) with child()
        // 2: create an empty new node in events node with a unique key by push() or by specific attribute
        event.setId(key);
        database.child("events").child(key).setValue(event, new DatabaseReference.CompletionListener() { // 3: upload current event to database with all non-null attributes
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) { // check if there is an error when reporting the event
                if (databaseError != null) {
                    Toast toast = Toast.makeText(getBaseContext(),
                            "Reporting event is failed, please check your network status.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getBaseContext(), "The event is reported.", Toast.LENGTH_SHORT);
                    toast.show();
                    mEditTextTitle.setText("");
                    mEditTextLocation.setText("");
                    mEditTextContent.setText("");
                }
            }
        });
        return key;
    }

    /**
     * Upload image and get file path
     */
    private void uploadImage(final String eventId) {
        if (mImgUri == null) {
            return;
        }
        StorageReference imgRef = storageRef.child("images/" + mImgUri.getLastPathSegment() + "_"
                + System.currentTimeMillis()); // set unique id for each image -> folder+path+time

        UploadTask uploadTask = imgRef.putFile(mImgUri); // 1: upload to cloud storage

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.i(TAG, "upload successfully" + eventId);
                database.child("events").child(eventId).child("imgUri").
                        setValue(downloadUrl.toString()); // 2: upload image url to database
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() { // reclaim the resource
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
