package sanidhya.academic.com.appointmentwithknowledge;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private Button loginB;
    private LoginButton fbLoginB;
    private EditText emailET;
    private EditText passwordET;
    private TextView signUpTV;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private CallbackManager fbCallbackManager;
    private RadioGroup roleRG;
    private RadioButton roleRB;
    private boolean authenticationResult = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        fbCallbackManager = CallbackManager.Factory.create();
        String authNature = getIntent().getStringExtra("auth_nature");
        if (authNature != null) {
            if (authNature.equals("log_out")) {
                LoginManager.getInstance().logOut();
            }
        }

        if (user!= null) {
            String userId =user.getUid();
            if (authNature.equals("student_login")) {
                if (authenticateUser("student", userId)) {
                    finish();
                    startActivity(new Intent(this, ProfileActivity.class));
                }
            } else if (authNature.equals("tutor_login")) {
                if (authenticateUser("tutor", userId)) {
                    finish();
                    startActivity(new Intent(this, ProfileActivity.class));
                }
            } else {
                    //TODO something when app opens directly to login
            }
        }

        fbLoginB = (LoginButton) findViewById(R.id.login_facebook_login_b);
        loginB = (Button) findViewById(R.id.login_login_b);
        emailET = (EditText) findViewById(R.id.login_email_et);
        passwordET = (EditText) findViewById(R.id.login_password_et);
        signUpTV = (TextView) findViewById(R.id.login_sign_up_tv);
        roleRG = (RadioGroup) findViewById(R.id.login_role_rg);
        progressDialog = new ProgressDialog(this);
        loginB.setOnClickListener(this);
        signUpTV.setOnClickListener(this);

        fbLoginB.setReadPermissions("email");
        fbLoginB.setOnClickListener(this);

    }

    private void fbLoginUser() {
        fbLoginB.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbHandleUserAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void fbHandleUserAccessToken(AccessToken accessToken) {

        progressDialog.show();
        AuthCredential fbCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(fbCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "User logged in through Facebook !!", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Facebook Login Failed!!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void toProfileActivity() {
        int roleId = roleRG.getCheckedRadioButtonId();
        roleRB = (RadioButton) findViewById(roleId);
        if (roleRB.getText() == "Student"&&authenticateUser("student",user.getUid())) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        } else if (roleRB.getText() == "Tutor"&&authenticateUser("tutor",user.getUid())) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        } else {
            if(roleId==-1){
            Toast.makeText(this, "Select your role", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "No "+roleRB.getText()+" of matching email is found!!", Toast.LENGTH_LONG).show();
            }
    }
    }

    private void simpleLoginUser() {

        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "email can't be empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password can't be empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Signing in User");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            finish();
                            toProfileActivity();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Login Failed!!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public void onClick(View v) {

        if (v == loginB) {
            simpleLoginUser();
        } else if (v == fbLoginB) {
            fbLoginUser();
        } else if (v == signUpTV) {
            finish();
            startActivity(new Intent(this, SignUpActivity.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private boolean authenticateUser(String role, final String userId) {
        DatabaseReference roleReference = FirebaseDatabase.getInstance().getReference().child("role").child(role);
        roleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                ArrayList userIds = (ArrayList) map.keySet();
                if (userIds.contains(userId))
                    authenticationResult = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return authenticationResult;
    }
}
