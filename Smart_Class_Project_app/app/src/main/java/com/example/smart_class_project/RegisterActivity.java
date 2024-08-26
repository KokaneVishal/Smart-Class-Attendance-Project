package com.example.smart_class_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText regRollNo = findViewById(R.id.etRollNo);
        EditText regFullName = findViewById(R.id.etFullName);
        EditText regEmail = findViewById(R.id.etEmail);
        EditText regPassword = findViewById(R.id.etPassword);
        Spinner spinnerClass = findViewById(R.id.spinnerClass);
//        EditText regClass = findViewById(R.id.etClass);
        Button registerBtn = findViewById(R.id.registerBtn);
        TextView backToLogin = findViewById(R.id.tvBackToLogin);

        View nameLayout = findViewById(R.id.nameLayout);
        View regHorizontal = findViewById(R.id.regHorizontal);
        nameLayout.setVisibility(View.GONE);
        regHorizontal.setVisibility(View.GONE);

        backToLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        registerBtn.setOnClickListener(view -> {
            String userEmail = regEmail.getText().toString().trim();
            String userPassword = regPassword.getText().toString().trim();
            String userRollNo = regRollNo.getText().toString().trim();
            String userFullName = regFullName.getText().toString().trim();
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.class_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClass.setAdapter(adapter);
            String userClass = spinnerClass.getSelectedItem().toString();
//            String userClass = regClass.getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            } else {

                db.collection("users").document(userEmail).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            registerUser(userEmail, userPassword);
                        } else {
                            //Toast.makeText(this, "User Details Not Exist!. Please add Details", Toast.LENGTH_SHORT).show();
                            nameLayout.setVisibility(View.VISIBLE);
                            regHorizontal.setVisibility(View.VISIBLE);
                            if (userRollNo.isEmpty() || userFullName.isEmpty() || userClass.equals("Class")) {
                                Toast.makeText(RegisterActivity.this, "Please fill all details.", Toast.LENGTH_SHORT).show();
                            } else {
                                saveUserDetails(userEmail, userPassword, userRollNo, userFullName, userClass);
                            }
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error : " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void saveUserDetails(String userEmail, String userPassword, String userRollNo, String userFullName, String userClass) {
        userHelperClass user = new userHelperClass(userRollNo, userFullName, userEmail, userClass);
        db.collection("users").document(userEmail).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "User details saved successfully.", Toast.LENGTH_SHORT).show();
                registerUser(userEmail, userPassword);
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to add user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void registerUser(String userEmail, String userPassword) {
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener(authResult -> {
            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
