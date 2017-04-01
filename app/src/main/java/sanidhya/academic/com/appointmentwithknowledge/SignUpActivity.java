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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {


    private Button registerB;
    private EditText emailET;
    private EditText passwordET;
    private TextView loginTV;
    private ProgressDialog pdialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, ProfileActivity.class));
        }
        registerB = (Button) findViewById(R.id.sign_up_register_b);
        registerB.setOnClickListener(this);
        emailET = (EditText) findViewById(R.id.sign_up_email_et);
        passwordET = (EditText) findViewById(R.id.sign_up_password_et);
        loginTV = (TextView) findViewById(R.id.sign_up_login_tv);
        loginTV.setOnClickListener(this);
        pdialog = new ProgressDialog(this);

    }

    private void registerUser() {

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

        pdialog.setMessage("Registering User");
        pdialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            pdialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "user registered!!", Toast.LENGTH_SHORT).show();
                        } else {
                            pdialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "user registration failed!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {

        if (v == registerB) {
            registerUser();
        } else if (v == loginTV) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
