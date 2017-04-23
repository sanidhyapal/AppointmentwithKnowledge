package sanidhya.academic.com.appointmentwithknowledge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sanidhya.academic.com.appointmentwithknowledge.model.Student;

/**
 * Created by Sanidhya on 22-Apr-17.
 */

public class StudentSignUpFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private Button registerB;
    private EditText studentEmailET;
    private TextInputEditText studentPasswordET;
    private TextInputEditText studentConfirmPasswordET;
    private EditText studentNameET;
    private EditText studentContactNumberET;
    private EditText studentSchoolNameET;
    private TextView loginTV;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Activity activity;
    private FirebaseUser user;
    private DatabaseReference instanceReference;

    public StudentSignUpFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.student_fragment_sign_up, null, false);
        activity = getActivity();
        instanceReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            activity.finish();
            startActivity(new Intent(activity, ProfileActivity.class));
        }
        registerB = (Button) v.findViewById(R.id.student_sign_up_register_b);
        registerB.setOnClickListener(this);
        studentEmailET = (EditText) v.findViewById(R.id.student_sign_up_email_et);
        studentContactNumberET = (EditText) v.findViewById(R.id.student_sign_up_contact_et);
        studentSchoolNameET = (EditText) v.findViewById(R.id.student_sign_up_school_et);
        studentNameET = (EditText) v.findViewById(R.id.student_sign_up_name_et);
        studentPasswordET = (TextInputEditText) v.findViewById(R.id.student_sign_up_password_et);
        studentConfirmPasswordET = (TextInputEditText) v.findViewById(R.id.student_sign_up_confirm_password_et);
        loginTV = (TextView) v.findViewById(R.id.student_sign_up_login_tv);
        loginTV.setOnClickListener(this);
        progressDialog = new ProgressDialog(getActivity());

        return v;
    }

    private void registerUser() {

        final String email = studentEmailET.getText().toString().trim();
        final String password = studentPasswordET.getText().toString().trim();
        final String confirmPassword = studentConfirmPasswordET.getText().toString().trim();
        final String studentName = studentNameET.getText().toString().trim();
        final String studentContactNumber = studentContactNumberET.getText().toString().trim();
        final String studentSchoolName = studentSchoolNameET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(activity, "email can not be empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(activity, "Password can not be empty!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(studentContactNumber)) {
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

        progressDialog.setMessage("Registering User");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(activity, "user registered!!", Toast.LENGTH_SHORT).show();
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            DatabaseReference userDetails =instanceReference.child("student").child(user.getUid());
                            Student newStudent = new Student(user.getUid(), studentName, studentContactNumber, studentSchoolName, email);
                            userDetails.setValue(newStudent);
                            DatabaseReference userRole=instanceReference.child("role").child("student");
                            userRole.child(user.getUid()).setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Intent toLoginActivity=new Intent(activity,LoginActivity.class);
                    toLoginActivity.putExtra("auth_nature","student_login");
                    activity.finish();
                    startActivity(toLoginActivity);
                }
            }
        });

    } else {
        progressDialog.dismiss();
        Toast.makeText(activity, "user registration failed!!", Toast.LENGTH_SHORT).show();
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
