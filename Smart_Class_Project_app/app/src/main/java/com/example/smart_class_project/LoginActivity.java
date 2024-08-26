package com.example.smart_class_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText loginEmail, loginPassword;
    Button LoginBtn;
    private ProgressBar loginProgressBar;
    TextView attempt;
    private int counter = 3;
    final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LoginMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check network availability
        if (!checkNetworkClass.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        loginEmail = findViewById(R.id.etEmail);
        loginPassword = findViewById(R.id.etPassword);
        LoginBtn = findViewById(R.id.loginBtn);
        attempt = findViewById(R.id.tvAttempt);
        TextView registration = findViewById(R.id.tvRegister);
        TextView aboutView = findViewById(R.id.aboutView);
        loginProgressBar = findViewById(R.id.loginProgressBar);

        String attemptTxt = "No of attempt remaining: 3";
        attempt.setText(attemptTxt);
        //progressDialog = new ProgressDialog(this);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
        }

        LoginBtn.setOnClickListener(view -> validate(loginEmail.getText().toString(), loginPassword.getText().toString()));

        registration.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        View visibleLoginLayout = findViewById(R.id.visibleLoginLayout);
        aboutView.setOnClickListener(view -> {
            visibleLoginLayout.setVisibility(View.GONE);
            AboutFragment aboutFragment = new AboutFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.loginLayoutFragment, aboutFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            transaction.replace(R.id.loginLayoutFragment, aboutFragment, "AboutFragment");
        });

    }

    @Override
    public void onBackPressed() {
        // Get the AboutFragment instance if it exists
        Fragment aboutFragment = getSupportFragmentManager().findFragmentByTag("AboutFragment");

        // If AboutFragment is visible, remove it and show the login UI
        if (aboutFragment != null && aboutFragment.isVisible()) {
            getSupportFragmentManager().popBackStack();
            findViewById(R.id.visibleLoginLayout).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void validate(String userName, String userPassword) {

        if (userName.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please Enter All Details", Toast.LENGTH_SHORT).show();
        } else {

            //progressDialog.setMessage("Please Wait");
            //progressDialog.show();
            loginProgressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //progressDialog.dismiss();
                    loginProgressBar.setVisibility(View.GONE);
                    finish();
                    Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    counter--;
                    attempt.setText("No of attempt remaining: " + counter);
                    //progressDialog.dismiss();
                    loginProgressBar.setVisibility(View.GONE);
                    if (counter == 0) {
                        LoginBtn.setEnabled(false);
                        LoginBtn.setText("Try After Sometime");
                    }
                }
            });
        }
    }
}