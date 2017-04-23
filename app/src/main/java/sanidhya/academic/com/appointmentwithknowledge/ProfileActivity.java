package sanidhya.academic.com.appointmentwithknowledge;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private TextView emailTV;
    private TextView nameTV;
    private TextView contactNumberTV;
    private Button saveB;
    private Button logOutB;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private ImageView profilePictureIV;
    private Button imageChooserB;
    private Uri imageHold=null;
    private Uri outputFileUri;
    private FirebaseUser user;
    char providerFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        user=firebaseAuth.getCurrentUser();
        emailTV = (TextView) findViewById(R.id.profile_email_tv);
        nameTV = (TextView) findViewById(R.id.profile_name_et);
        contactNumberTV = (TextView) findViewById(R.id.profile_contact_number_et);
        profilePictureIV=(ImageView)findViewById(R.id.profile_photo_iv);
        imageChooserB =(Button)findViewById(R.id.profile_image_chooser_b);
        logOutB = (Button) findViewById(R.id.profile_log_out_b);
        saveB = (Button) findViewById(R.id.profile_save_b);
        saveB.setVisibility(View.GONE);
        saveB.setOnClickListener(this);
        imageChooserB.setOnClickListener(this);
        logOutB.setOnClickListener(this);
        if(user!=null)
        {

            for(UserInfo userInfo:user.getProviderData())
            {
                if(userInfo.getProviderId().equals("facebook.com"))
                {
                    providerFlag='f';
                    Toast.makeText(this, "In Facebook", Toast.LENGTH_LONG).show();
                    Picasso.with(this).load(user.getPhotoUrl()).into(profilePictureIV);
                    nameTV.setText(user.getDisplayName());
                    emailTV.setText(user.getEmail());
                }
            }
        }

        if(providerFlag!='f')
        {
            if (user != null) {
                emailTV.setText(user.getEmail());
                nameTV.setText("Ye Baad Mein aayega");
                contactNumberTV.setText("Ye Bbi Baad Mein Aayega ye");
                Picasso.with(this).load(user.getPhotoUrl()).into(profilePictureIV);
            }

        }

    }

    @Override
    public void onClick(View v) {

        if (v == saveB) {
//            String name = nameTV.getText().toString().trim();
//            String contactNo = contactNumberTV.getText().toString().trim();
//            if (user != null) {
//                Tutor employee = new Tutor(user.getUid(), name, user.getEmail(), "", contactNo, "", designation);
//                reference.child(user.getUid()).setValue(employee);
//
//                Toast.makeText(this, "Information Saved!!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Retry signing in!!", Toast.LENGTH_SHORT).show();
//                finish();
//                firebaseAuth.signOut();
//                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//
//            }

        }
        if (v == logOutB) {

            firebaseAuth.signOut();
            Intent fbLogOutIntent=new Intent(getApplicationContext(), LoginActivity.class);
            if(providerFlag=='f')
            {fbLogOutIntent.putExtra("auth_nature","log_out");}
            finish();
            startActivity(fbLogOutIntent);
        }

        if(v== imageChooserB)
        {
           RequestPermissions.storagePermissionRequest(this);
            imageChooser();
        }
    }

    //IMAGE PICKER WHEN CHOOSE IMAGE BUTTON IS CLICKED
    private void imageChooser() {

        File root = new File(Environment.getExternalStorageDirectory() + File.separator + "AWK" + File.separator + "Store Picture" + File.separator);
        root.mkdirs();
        final String fname = "profilePic" + System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        //Camera
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final String localPackageName = res.activityInfo.loadLabel(packageManager).toString();
            if (localPackageName.toLowerCase().equals("camera")) {
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                cameraIntents.add(intent);
            }
        }
        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_CODE);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                CropImage.activity(selectedImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    imageHold = result.getUri();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageHold);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                        byte[] bytesBitmap = byteArrayOutputStream.toByteArray();
                        File temp = File.createTempFile("store", "pic.jpg");
                        FileOutputStream fileOutputStream = new FileOutputStream(temp);
                        fileOutputStream.write(bytesBitmap);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        imageHold = Uri.fromFile(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    profilePictureIV.setImageURI(imageHold);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(this, "CROP ERROR:"+error.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
