package fi.qvik.demo.realtrans;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.qvik.demo.realtrans.fragment.MyImagesFragment;
import fi.qvik.demo.realtrans.fragment.RecentImagesFragment;
import fi.qvik.demo.realtrans.models.Upload;
import fi.qvik.demo.realtrans.models.User;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by jja on 24/08/16.
 */
public class MainActivity extends BaseActivity implements
        EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MainActivity";

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]
    // [START declare_storage_ref]
    private StorageReference mStorageRef;
    // [END declare_storage_ref]

    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;
    private static final int THUMBNAIL_SIZE = 85;
    private final boolean UPLOAD_THUMBNAILS_ONLY = false;
    private final boolean GENERATE_THUMBNAIL_FROM_FILE = true;

    private User currentUserModel = null;

    // Full file upload related varaibles
    private Uri mFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        // [START initialize_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END initialize_storage_ref]

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new RecentImagesFragment(),
                    new MyImagesFragment(),
                    //new MyTopPostsFragment(),
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_images),
                    //getString(R.string.heading_my_top_images)
            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Button fetches current user and launches Camera
        findViewById(R.id.fab_new_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserModel != null) {
                    Log.d(TAG, "current user model is set: " + currentUserModel.nickname);
                    takePicture();
                } else {
                    Log.d(TAG, "current user model not yet set");
                    // [START single_value_read]
                    final String userId = getUid();
                    mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get user value
                                    currentUserModel = dataSnapshot.getValue(User.class);
                                    Log.d(TAG, "current user model is now set: " + currentUserModel.nickname);
                                    // [START_EXCLUDE]
                                    if (currentUserModel == null) {
                                        // User is null, error out
                                        Log.e(TAG, "User " + userId + " is unexpectedly null");
                                        Toast.makeText(MainActivity.this,
                                                "Error: could not fetch user.",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        takePicture();
                                    }
                                    // [END_EXCLUDE]
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                }
                            });
                    // [END single_value_read]
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (UPLOAD_THUMBNAILS_ONLY) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    uploadFromBitmap(imageBitmap);

                } else {
                    uploadFromUri(mFileUri);
                }
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {}

    @AfterPermissionGranted(RC_STORAGE_PERMS)
    private void takePicture() {
        Log.d(TAG, "takePicture");

        mFileUri = null;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!UPLOAD_THUMBNAILS_ONLY) {
            // Check that we have permission to read images from external storage.
            String perm = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (!EasyPermissions.hasPermissions(this, perm)) {
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                        RC_STORAGE_PERMS, perm);
                return;
            }
            // Choose file storage location, must be listed in res/xml/file_paths.xml
            File dir = new File(Environment.getExternalStorageDirectory() + "/images");
            File file = new File(dir, UUID.randomUUID().toString() + ".jpg");

            try {
                // Create directory if it does not exist.
                if (!dir.exists()) {
                    dir.mkdir();
                }
                boolean created = file.createNewFile();
                Log.d(TAG, "file.createNewFile:" + file.getAbsolutePath() + ":" + created);
            } catch (IOException e) {
                Log.e(TAG, "file.createNewFile" + file.getAbsolutePath() + ":FAILED", e);
            }

            // Create content:// URI for file, required since Android N
            // See: https://developer.android.com/reference/android/support/v4/content/FileProvider.html
            mFileUri = FileProvider.getUriForFile(this,
                    "fi.qvik.demo.realtrans.fileprovider", file);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        }

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, RC_TAKE_PICTURE);
        }
    }

    // [START upload_from_bitmap]
    private void uploadFromBitmap(Bitmap thumbImage) {
        Log.d(TAG, "uploadFromBitmap " + thumbImage.getByteCount());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(UUID.randomUUID().toString() + ".jpg");
        // [END get_child_ref]

        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]

        // Upload thumbnail data to Firebase Storage
        photoRef.putBytes(data)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromBitmap:onSuccess");

                        Upload upld = new Upload(
                                taskSnapshot.getMetadata().getDownloadUrl().toString(),
                                getUid(),
                                currentUserModel.nickname
                        );
                        writeUploadToDatabase(upld);

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromBitmap:onFailure", exception);

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(MainActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_bitmap]

    // [START upload_from_uri]
    private void uploadFromUri(final Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        if (GENERATE_THUMBNAIL_FROM_FILE) {
            Bitmap thumbImage;
            try {
                thumbImage = createThumbnailFromFileUri(fileUri, 800, 1000);
            } catch (Exception e) {
                return;
            }
            uploadFromBitmap(thumbImage);
            return;
        }

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        // Upload file to Firebase Storage
        photoRef.putFile(fileUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        Upload upld = new Upload(
                                taskSnapshot.getMetadata().getDownloadUrl().toString(),
                                getUid(),
                                currentUserModel.nickname
                        );
                        writeUploadToDatabase(upld);

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        removeOriginalImageFile(fileUri);
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        removeOriginalImageFile(fileUri);
                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(MainActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]

    private void removeOriginalImageFile(Uri fileUri) {
        File file = new File(fileUri.toString());
        file.delete();
    }

    /**
     * Code found from http://blog-emildesign.rhcloud.com/?p=590
     */
    private Bitmap createThumbnailFromFileUri(final Uri fileUri, int reqWidth, int reqHeight) throws FileNotFoundException {
        FileDescriptor fileDescriptor;
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(fileUri, "r");
            fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        } catch (FileNotFoundException e) {
            throw e;
        }

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;
        if (expectedWidth > reqWidth) {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        /*
        Log.d(TAG, "thumbnail:origWidth " + width);
        Log.d(TAG, "thumbnail:origHeight " + height);
        Log.d(TAG, "thumbnail:expectedWidth " + expectedWidth);
        Log.d(TAG, "thumbnail:inSampleSize " + inSampleSize);
        */

        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    private void writeUploadToDatabase(Upload upload) {
        String key = mDatabase.child("uploads").push().getKey();

        Map<String, Object> uploadValues = upload.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/uploads/" + key, uploadValues);
        childUpdates.put("/user-uploads/" + upload.uid + "/" + key, uploadValues);

        mDatabase.updateChildren(childUpdates);
    }
}
