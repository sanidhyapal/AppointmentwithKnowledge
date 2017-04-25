package sanidhya.academic.com.appointmentwithknowledge;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import sanidhya.academic.com.appointmentwithknowledge.model.Student;
import sanidhya.academic.com.appointmentwithknowledge.model.Tutor;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private final static int PROVIDER_FIREBASE = 0;
    private final static int PROVIDER_FACEBOOK = 1;
    private final static int PROVIDER_GOOGLE = 2;
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
    private int authenticationResult = 0;
    private DatabaseReference instanceReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
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
        firebaseAuth = FirebaseAuth.getInstance();
        instanceReference = FirebaseDatabase.getInstance().getReference();
        user = firebaseAuth.getCurrentUser();
        fbCallbackManager = CallbackManager.Factory.create();
        String authNature = getIntent().getStringExtra("auth_nature");
        if (user != null) {
            String userId = user.getUid();

            if (authNature != null) {
                if (authNature.equals("log_out")) {
                    firebaseAuth.signOut();
                    LoginManager.getInstance().logOut();
                } else if (authNature.equals("student_login")) {
                    authenticateUser("student", userId);
                } else if (authNature.equals("tutor_login")) {
                    authenticateUser("tutor", userId);
                }
            } else {
                if (user.getDisplayName() != null) {
                    switch (user.getDisplayName()) {
                        case "student":
                            finish();
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            break;
                        case "tutor":
                            finish();
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            break;
                    }
                }
            }
        } else
            selectRoleAlertBox();
    }

    private void fbLoginUser() {
        fbLoginB.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Toast.makeText(LoginActivity.this, "in call back manager", Toast.LENGTH_SHORT).show();

                final GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        JSONObject jsonObject = response.getJSONObject();
                        try {
                            if (jsonObject != null) {
                                String userEmail = jsonObject.getString("email");
                                Log.d("userEmail", userEmail);

                                firebaseAuth.fetchProvidersForEmail(userEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                        List<String> providerList = task.getResult().getProviders();
                                        if (providerList.isEmpty()) {
                                            fbHandleUserAccessToken(loginResult.getAccessToken());
                                        } else if (providerList.contains("facebook.com")) {
                                            progressDialog.setMessage("Contacting Mark Zuckerberg!!");
                                            progressDialog.show();
                                            AuthCredential fbCredential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                            firebaseAuth.signInWithCredential(fbCredential).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        progressDialog.dismiss();
                                                        user = firebaseAuth.getCurrentUser();
                                                        int roleId = roleRG.getCheckedRadioButtonId();
                                                        String userRole = user.getDisplayName();
                                                        RadioButton roleRB = (RadioButton) findViewById(roleId);
                                                        String selectedRole = roleRB.getText().toString().toLowerCase();
                                                        if (userRole.equals(selectedRole))
                                                            toProfileActivity(PROVIDER_FACEBOOK);
                                                        else {
                                                            firebaseAuth.signOut();
                                                            LoginManager.getInstance().logOut();
                                                            Toast.makeText(LoginActivity.this, "No " + selectedRole + " found with this account!! Please check role", Toast.LENGTH_LONG).show();

                                                        }
                                                    } else {
                                                        progressDialog.dismiss();
                                                        firebaseAuth.signOut();
                                                        LoginManager.getInstance().logOut();
                                                        Toast.makeText(LoginActivity.this, "Facebook Login Failed!!", Toast.LENGTH_LONG).show();
                                                    }

                                                }
                                            });
                                        }
                                        else
                                        {
                                            firebaseAuth.signOut();
                                            LoginManager.getInstance().logOut();
                                            Toast.makeText(LoginActivity.this, "Email id already registered!!Try another email", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,email,picture");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void selectRoleAlertBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage("Select Your Role:")
                .setPositiveButton("TUTOR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioButton tutorRB = (RadioButton) findViewById(R.id.login_tutor_rb);
                        tutorRB.setChecked(true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("STUDENT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioButton studentRB = (RadioButton) findViewById(R.id.login_student_rb);
                        studentRB.setChecked(true);
                        dialog.dismiss();
                    }
                });
        AlertDialog roleAlertDialog = builder.create();
        roleAlertDialog.setCanceledOnTouchOutside(false);
        roleAlertDialog.show();
    }

    private void fbHandleUserAccessToken(AccessToken accessToken) {

        progressDialog.setMessage("Contacting Mark Zuckerberg!!");
        progressDialog.show();
        AuthCredential fbCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(fbCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    user = firebaseAuth.getCurrentUser();
                    int roleId = roleRG.getCheckedRadioButtonId();
                    if (roleId == -1) {
                        firebaseAuth.signOut();
                        LoginManager.getInstance().logOut();
                        Toast.makeText(LoginActivity.this, "Select your role", Toast.LENGTH_SHORT).show();
                    } else {
                        RadioButton roleRB = (RadioButton) findViewById(roleId);
                        if (roleRB.getId() == R.id.login_student_rb) {
                            fbLoginDetailsSave("student");
                        } else if (roleRB.getId() == R.id.login_tutor_rb) {
                            fbLoginDetailsSave("tutor");
                        }
                    }

                } else {
                    progressDialog.dismiss();
                    firebaseAuth.signOut();
                    LoginManager.getInstance().logOut();
                    Toast.makeText(LoginActivity.this, "Facebook Login Failed!!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void fbLoginDetailsSave(final String role) {
        progressDialog.setMessage("Saving your details in database");
        progressDialog.show();
        DatabaseReference userDetails = instanceReference.child(role).child(user.getUid());
        if (role.equals("student")) {
            Student newStudent = new Student(user.getUid(), user.getDisplayName(), "", "", user.getEmail());
            userDetails.setValue(newStudent);
        } else if (role.equals("tutor")) {
            Tutor newTutor = new Tutor(user.getUid(), user.getDisplayName(), user.getEmail(), "", "");
            userDetails.setValue(newTutor);
        }
        DatabaseReference userRole = instanceReference.child("role").child(role);
        userRole.child(user.getUid()).setValue(user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(role)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "One more lazy " + role, Toast.LENGTH_SHORT).show();
                                        toProfileActivity(PROVIDER_FACEBOOK);
                                    }
                                    else {
                                        progressDialog.dismiss();
                                        firebaseAuth.signOut();
                                        LoginManager.getInstance().logOut();
                                        Toast.makeText(LoginActivity.this, "Facebook Login Failed!!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
                else {
                    progressDialog.dismiss();
                    firebaseAuth.signOut();
                    LoginManager.getInstance().logOut();
                    Toast.makeText(LoginActivity.this, "Facebook Login Failed!!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void toProfileActivity(int providerName) {
        int roleId = roleRG.getCheckedRadioButtonId();
        RadioButton roleRB = (RadioButton) findViewById(roleId);
        if (roleId == -1) {
            firebaseAuth.signOut();
            LoginManager.getInstance().logOut();
            Toast.makeText(this, "Select your role", Toast.LENGTH_SHORT).show();

        } else if (roleRB.getId() == R.id.login_student_rb) {
            if (providerName == PROVIDER_FIREBASE) {
                authenticateUser("student", user.getUid());
            } else if (providerName == PROVIDER_FACEBOOK) {
                finish();
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        } else if (roleRB.getId() == R.id.login_tutor_rb) {
            if (providerName == PROVIDER_FIREBASE) {
                authenticateUser("tutor", user.getUid());
            } else if (providerName == PROVIDER_FACEBOOK) {
                finish();
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
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
                            user = task.getResult().getUser();
                            toProfileActivity(PROVIDER_FIREBASE);
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
            int roleId = roleRG.getCheckedRadioButtonId();
            if (roleId == -1) {
                Toast.makeText(this, "Select your role", Toast.LENGTH_SHORT).show();
            } else
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

    private void authenticateUser(final String role, final String userId) {
        DatabaseReference roleReference = FirebaseDatabase.getInstance().getReference().child("role").child(role);
        progressDialog.setMessage("Authenticating User!!");
        progressDialog.show();
        roleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                progressDialog.dismiss();
                if (map != null) {
                    Set<String> userIds = map.keySet();
                    if (userIds.contains(userId))
                        authenticationResult = 1;
                    else
                        authenticationResult = 0;
                }
                authenticateUserResponse(role, authenticationResult);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void authenticateUserResponse(String role, int authenticationResult) {
        switch (role) {
            case "student":
                switch (authenticationResult) {
                    case 0:
                        firebaseAuth.signOut();
                        Toast.makeText(this, "No student of matching email is found!!", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        finish();
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                }
                break;
            case "tutor":
                switch (authenticationResult) {
                    case 0:
                        firebaseAuth.signOut();
                        Toast.makeText(this, "No tutor of matching email is found!!", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        finish();
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                }
                break;
        }

    }
}
