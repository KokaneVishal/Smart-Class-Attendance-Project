package com.example.smart_class_project;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;


public class DashboardActivity extends AppCompatActivity {
    TextView showCurrentUserEmail, userRoleGreeting;
    EditText uploadFileName;
    View dashAdminPanel, dashUserPanel;
    Button viewUsersBtn, addAdminBtn, uploadAttendanceBtn, uploadNoticeBtn, uploadNotesBtn, postAssignment;
    //final String adminEmail = "admin@gmail.com";
    final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseFirestore db;
    StorageReference storageReference;
    final String userEmail;

    {
        assert currentUser != null;
        userEmail = currentUser.getEmail();
    }

    static final String COLLECTION_ATTENDANCE = "attendance";
    static final String COLLECTION_NOTICE = "notice";
    static final String COLLECTION_NOTES = "study_notes";
    static final String COLLECTION_ASSIGNMENT = "assignments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.DashMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uploadFileName = findViewById(R.id.etUploadFileName);
        uploadAttendanceBtn = findViewById(R.id.btnUploadAttendance);
        uploadNoticeBtn = findViewById(R.id.btnUploadNotice);
        uploadNotesBtn = findViewById(R.id.btnUploadNotes);
        postAssignment = findViewById(R.id.btnPostAssignment);

        View dashContent = findViewById(R.id.dashContent);
        viewUsersBtn = findViewById(R.id.viewUsersBtn);
        addAdminBtn = findViewById(R.id.addAdminBtn);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        showCurrentUserEmail = findViewById(R.id.tvCurrentUserMail);
        showCurrentUserEmail.setText("[ " + currentUser.getEmail() + " ]");

        userRoleGreeting = findViewById(R.id.txtRole);
        dashAdminPanel = findViewById(R.id.dashAdminPanelLayout);
        dashUserPanel = findViewById(R.id.dashUserPanelLayout);

        checkAdminOrNot(userEmail);
//        if (userEmail.equals(adminEmail)) {
//            userRoleGreeting.setText("Welcome Admin/Teacher");
//            dashAdminPanel.setVisibility(View.VISIBLE);
//            dashUserPanel.setVisibility(View.GONE);
//        } else {
//            userRoleGreeting.setText("Welcome Student");
//            dashAdminPanel.setVisibility(View.GONE);
//            dashUserPanel.setVisibility(View.VISIBLE);
//
//            fetchProfileDetails();
//
//            Button contactToAdmin = findViewById(R.id.contactToAdminBtn);
//
//            contactToAdmin.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String url = "https://www.google.com";
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse(url));
//                    startActivity(intent);
//                }
//            });
//        }

        dashAdminPanel.setVisibility(View.GONE);
        dashUserPanel.setVisibility(View.GONE);

        viewUsersBtn.setOnClickListener(v -> {
            dashContent.setVisibility(View.GONE);
            replaceFragment(new ViewUsersFragment());
        });

        addAdminBtn.setOnClickListener(v -> showAddAdminDialog());

        uploadAttendanceBtn.setOnClickListener(v -> openFilePicker(1));

        uploadNotesBtn.setOnClickListener(v -> openFilePicker(2));

        uploadNoticeBtn.setOnClickListener(v -> openFilePicker(3));

        postAssignment.setOnClickListener(v -> openFilePicker(4));

        MaterialToolbar toolbar = findViewById(R.id.topAppbar);
        DrawerLayout drawerLayout = findViewById(R.id.DashMain);
        NavigationView navigationView = findViewById(R.id.navView);

        MenuItem versionItem = navigationView.getMenu().findItem(R.id.nav_version);
        String versionName = BuildConfig.VERSION_NAME;
        versionItem.setTitle("App version " + versionName);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            menuItem.setChecked(true);
            drawerLayout.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_dash) {
                startActivity(new Intent(DashboardActivity.this, DashboardActivity.class));
            } else if (id == R.id.nav_files) {
                dashContent.setVisibility(View.GONE);
                replaceFragment(new AttendanceFragment());
            } else if (id == R.id.nav_notice) {
                dashContent.setVisibility(View.GONE);
                replaceFragment(new NoticeFragment());
            } else if (id == R.id.nav_notes) {
                dashContent.setVisibility(View.GONE);
                replaceFragment(new StudyNotesFragment());
            } else if (id == R.id.nav_analysis) {
                dashContent.setVisibility(View.GONE);
                replaceFragment(new AnalysisFragment());
            } else if (id == R.id.nav_assignment) {
                dashContent.setVisibility(View.GONE);
                replaceFragment(new AssignmentFragment());
            } else if (id == R.id.nav_logout) {
                firebaseAuth.signOut();
                finish();
                Toast.makeText(DashboardActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            } else if (id == R.id.nav_about) {
                dashContent.setVisibility(View.GONE);
                replaceFragment(new AboutFragment());
            } else if (id == R.id.nav_feedback) {
                dashContent.setVisibility(View.GONE);
                replaceFragment(new FeedbackFragment());
            }
            return true;
        });
    }

    private void checkAdminOrNot(String userEmail) {
        DocumentReference usersRef = db.collection("users").document(userEmail);
        usersRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                if (isAdmin != null && isAdmin) {
                    // User is an admin
                    // admin activities here
                    userRoleGreeting.setText("Welcome Admin/Teacher");
                    dashAdminPanel.setVisibility(View.VISIBLE);
                    dashUserPanel.setVisibility(View.GONE);

                    Button helpUserBtn2 = findViewById(R.id.helpUserBtn2);
                    helpUserBtn2.setOnClickListener(v -> showHelpDialog());
                } else {
                    // User is not an admin
                    // student activities here
                    userRoleGreeting.setText("Welcome Student");
                    dashAdminPanel.setVisibility(View.GONE);
                    dashUserPanel.setVisibility(View.VISIBLE);
                    // Fetch and display user profile details
                    fetchProfileDetails();
                    // Set OnClickListener for contacting admin
                    Button contactToAdmin = findViewById(R.id.contactToAdminBtn);
                    contactToAdmin.setOnClickListener(v -> {
                        String url = "https://www.google.com";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    });

                    Button helpUserBtn = findViewById(R.id.helpUserBtn);
                    helpUserBtn.setOnClickListener(v -> showHelpDialog());

                }
            } else {
                dashAdminPanel.setVisibility(View.GONE);
                dashUserPanel.setVisibility(View.GONE);
                Toast.makeText(this, "User Email document doesn't exist", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error!" + e.getMessage(), Toast.LENGTH_SHORT).show();

        });
    }


    private void openFilePicker(int requestCode) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Please Select File"), requestCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadFile(data.getData(), COLLECTION_ATTENDANCE, uploadFileName);
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadFile(data.getData(), COLLECTION_NOTES, uploadFileName);
        } else if (requestCode == 3 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadFile(data.getData(), COLLECTION_NOTICE, uploadFileName);
        } else if (requestCode == 4 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadFile(data.getData(), COLLECTION_ASSIGNMENT, uploadFileName);
        }
    }

    private void uploadFile(Uri data, final String collectionPath, final EditText fileNameEditText) {
        String fileName = fileNameEditText.getText().toString().trim();
        if (fileName.isEmpty()) {
            Toast.makeText(DashboardActivity.this, "Enter File Name!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check network availability
        if (!checkNetworkClass.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressBar uploadProgressBar;
        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        uploadProgressBar.setVisibility(View.VISIBLE);
        //final ProgressDialog progressDialog = new ProgressDialog(this);
        //progressDialog.setTitle("Uploading");
        //progressDialog.show();

        StorageReference reference = storageReference.child(collectionPath).child(fileName);

        reference.putFile(data)
                .addOnSuccessListener(taskSnapshot -> {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save the file path to FStore
                        uploadFileClass uploadFile = new uploadFileClass(fileName, uri.toString());
                        uploadFile.setTimestamp(new java.util.Date());
                        db.collection(collectionPath)
                                .add(uploadFile)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(DashboardActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                                        uploadProgressBar.setVisibility(View.GONE);
                                        fileNameEditText.setText("");
                                    } else {
                                        Toast.makeText(DashboardActivity.this, "File Upload Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        uploadProgressBar.setVisibility(View.GONE);

                                    }
                                });
                    });
                })
//                .addOnProgressListener(taskSnapshot -> {
//                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                    progressDialog.setMessage("Upload : " + (int) progress + "%");
//                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DashboardActivity.this, "File Upload Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    //progressDialog.dismiss();
                    uploadProgressBar.setVisibility(View.GONE);

                });
    }

    private void fetchProfileDetails() {
        TextView userProfileFullName, userProfileRollNo, userProfileClass, userProfileEmail;

        userProfileFullName = findViewById(R.id.tvUserFullName);
        userProfileRollNo = findViewById(R.id.tvUserRollNo);
        userProfileClass = findViewById(R.id.tvUserClass);
        userProfileEmail = findViewById(R.id.tvUserEmail);

        db.collection("users").whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            userHelperClass user = documentSnapshot.toObject(userHelperClass.class);

                            userProfileFullName.setText("Name: " + user.getFullName());
                            userProfileRollNo.setText("Roll No: " + user.getRollNo());
                            userProfileClass.setText("Class: " + user.getUserClass());
                            userProfileEmail.setText("Email: " + user.getEmail());
                            break;
                        }
                    } else {
                        Toast.makeText(this, "User document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error!" + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    private void showAddAdminDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_admin);
        Button closeDialogBtn = dialog.findViewById(R.id.dialogCloseBtn);

        Button addAdminBtn = dialog.findViewById(R.id.btnAddAdmin);
        EditText MakeEmailAdmin = dialog.findViewById(R.id.etMakeEmailAdmin);
//        String MakeEmailAsAdmin = MakeEmailAdmin.getText().toString().trim();
        addAdminBtn.setOnClickListener(v -> {
            if (!MakeEmailAdmin.getText().toString().trim().isEmpty()) {
                // Get a reference to the users collection
                CollectionReference usersRef = db.collection("users");

                // Query for the user document with the provided email
                usersRef.whereEqualTo("email", MakeEmailAdmin.getText().toString().trim())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                // Update the isAdmin field to true
                                documentSnapshot.getReference().update("isAdmin", true)
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully updated isAdmin to true
                                            Toast.makeText(DashboardActivity.this, "User promoted to admin Successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Failed to update isAdmin
                                            Toast.makeText(DashboardActivity.this, "Failed to promote user to admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error querying for user document
                            Toast.makeText(DashboardActivity.this, "Error querying for user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(DashboardActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
            }
        });

        closeDialogBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void showHelpDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_help);

        Button btnDialogClose = dialog.findViewById(R.id.btnDialogClose);
        btnDialogClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutFragment, fragment);
        fragmentTransaction.commit();
    }
}