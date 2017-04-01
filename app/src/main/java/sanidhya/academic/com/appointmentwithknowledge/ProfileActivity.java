package sanidhya.academic.com.appointmentwithknowledge;


import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sanidhya.academic.com.appointmentwithknowledge.model.Tutor;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE =244;
    private TextView emailTV;
    private EditText nameET;
    private EditText contactNoET;
    private EditText designationET;
    private Button saveB;
    private Button logOutB;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private ImageView profilePictureIV;
    private Button imageChooser;
    private Uri filePath;
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
        nameET = (EditText) findViewById(R.id.profile_name_et);
        contactNoET = (EditText) findViewById(R.id.profile_contact_number_et);
        designationET = (EditText) findViewById(R.id.profile_designation_et);
        profilePictureIV=(ImageView)findViewById(R.id.profile_photo_iv);
        imageChooser=(Button)findViewById(R.id.profile_image_chooser_b);
        logOutB = (Button) findViewById(R.id.profile_log_out_b);
        saveB = (Button) findViewById(R.id.profile_save_b);
        saveB.setOnClickListener(this);
        imageChooser.setOnClickListener(this);
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
                    nameET.setText(user.getDisplayName());
                    emailTV.setText(user.getEmail());
                }
            }
        }

        if(providerFlag!='f')
        {
            if (user != null) {
                emailTV.setText(user.getEmail());
            }

        }

    }

    @Override
    public void onClick(View v) {

        if (v == saveB) {
            String name = nameET.getText().toString().trim();
            String contactNo = contactNoET.getText().toString().trim();
            String designation = designationET.getText().toString().trim();
            if (user != null) {
                Tutor employee = new Tutor(user.getUid(), name, user.getEmail(), "", contactNo, "", designation);
                reference.child(user.getUid()).setValue(employee);

                Toast.makeText(this, "Information Saved!!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Retry signing in!!", Toast.LENGTH_SHORT).show();
                finish();
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

            }

        }
        if (v == logOutB) {

            firebaseAuth.signOut();
            Intent fbLogOutIntent=new Intent(getApplicationContext(), LoginActivity.class);
            fbLogOutIntent.putExtra("auth_nature","LogOut");
            finish();
            startActivity(fbLogOutIntent);
        }

        if(v==imageChooser)
        {

            permissionRequest();
            imageChooser();
        }
    }

    //PERMISSIONS REQUIRED FOR ACCESSING EXTERNAL STORAGE

    private void permissionRequest() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
            }
        }
    }

    //IMAGE PICKER WHEN CHOOSE IMAGE BUTTON IS CLICKED
    private void imageChooser() {

        final File root=new File(Environment.getExternalStorageDirectory()+File.separator+"Field Attendance"+File.separator);
        root.mkdirs();
        final String fname="profpic"+ System.currentTimeMillis()+".jpg";
        final File sdImageMainDirectory=new File(root,fname);
        outputFileUri=Uri.fromFile(sdImageMainDirectory);

        //Camera
        final List<Intent> cameraIntents=new ArrayList<Intent>();
        final Intent captureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager=getPackageManager();
        final List<ResolveInfo> listCam=packageManager.queryIntentActivities(captureIntent,0);
        for(ResolveInfo res:listCam)
        {
            final String packageName=res.activityInfo.packageName;
            final Intent intent=new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName,res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent,PICK_IMAGE_REQUEST_CODE);


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
                try {
                    Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImageUri);
                    profilePictureIV.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
