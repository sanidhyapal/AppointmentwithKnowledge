package sanidhya.academic.com.appointmentwithknowledge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sanidhya.academic.com.appointmentwithknowledge.model.Tutor;

/**
 * Created by Sanidhya on 23-Apr-17.
 */

public class TutorSignUpFragment extends Fragment implements View.OnClickListener {
    
    private Button registerB;
    private EditText tutorEmailET;
    private TextInputEditText tutorPasswordET;
    private TextInputEditText tutorConfirmPasswordET;
    private EditText tutorNameET;
    private EditText tutorContactNumberET;
    private EditText tutorExperienceET;
    private TextView loginTV;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Activity activity;
    private FirebaseUser user;
    private DatabaseReference instanceReference;

    public TutorSignUpFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tutor_fragment_sign_up, null, false);
        activity = getActivity();
        instanceReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            activity.finish();
            startActivity(new Intent(activity, ProfileActivity.class));
        }
        registerB = (Button) v.findViewById(R.id.tutor_sign_up_register_b);
        registerB.setOnClickListener(this);
        tutorEmailET = (EditText) v.findViewById(R.id.tutor_sign_up_email_et);
        tutorContactNumberET = (EditText) v.findViewById(R.id.tutor_sign_up_contact_et);
        tutorExperienceET = (EditText) v.findViewById(R.id.tutor_sign_up_experience_et);
        tutorNameET = (EditText) v.findViewById(R.id.tutor_sign_up_name_et);
        tutorPasswordET = (TextInputEditText) v.findViewById(R.id.tutor_sign_up_password_et);
        tutorConfirmPasswordET = (TextInputEditText) v.findViewById(R.id.tutor_sign_up_confirm_password_et);
        loginTV = (TextView) v.findViewById(R.id.tutor_sign_up_login_tv);
        loginTV.setOnClickListener(this);
        progressDialog = new ProgressDialog(getActivity());

        return v;
    }

    private void registerUser() {

        final String email = tutorEmailET.getText().toString().trim();
        final String password = tutorPasswordET.getText().toString().trim();
        final String confirmPassword = tutorConfirmPasswordET.getText().toString().trim();
        final String tutorName = tutorNameET.getText().toString().trim();
        final String tutorContactNumber = tutorContactNumberET.getText().toString().trim();
        final String tutorExperience = tutorExperienceET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(activity, "email can not be empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(activity, "Password can not be empty!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tutorContactNumber)) {
            Toast.makeText(activity, "contact number can not be empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(activity, "Type Password again!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(activity, "passwords do not match!!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering user");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(activity, "user registered!!", Toast.LENGTH_SHORT).show();
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            DatabaseReference userDetails =instanceReference.child("tutor").child(user.getUid());
                            Tutor newTutor = new Tutor(user.getUid(), tutorName, email, tutorContactNumber,tutorExperience);
                            userDetails.setValue(newTutor);
                            DatabaseReference userRole=instanceReference.child("role").child("tutor");
                            userRole.child(user.getUid()).setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName("tutor")
                                            .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(activity, "Hahaha Welcome Tutor", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        Intent toLoginActivity=new Intent(activity,LoginActivity.class);
                                        toLoginActivity.putExtra("auth_nature","tutor_login");
                                        activity.finish();
                                        startActivity(toLoginActivity);
                                    }
                                }
                            });

                        } else {
                            progressDialog.dismiss();

                            progressDialog.dismiss();
                            if (!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(activity, "Weak Password!!", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(activity, "Invalid Credential!!", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(activity, "Email id is already registered", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(activity, "user registration failed!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {

        if (v == registerB) {
            registerUser();
        } else if (v == loginTV) {
            activity.finish();
            startActivity(new Intent(activity, LoginActivity.class));
        }
    }

}
