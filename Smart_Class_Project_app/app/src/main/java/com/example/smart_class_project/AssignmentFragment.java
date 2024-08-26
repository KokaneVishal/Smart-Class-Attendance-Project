package com.example.smart_class_project;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AssignmentFragment extends Fragment {
    ListView assignmentListView;
    EditText uploadAssignmentNameET;
    Button uploadAssignmentBtn;
    FirebaseFirestore db;
    List<uploadFileClass> fetchFileAssignment;
    static final String COLLECTION_ASSIGNMENT = "assignments";

    public AssignmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);

        // Check network availability
        if (!checkNetworkClass.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

        assignmentListView = view.findViewById(R.id.assignmentListView);
        fetchFileAssignment = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        uploadAssignmentNameET = view.findViewById(R.id.etUploadAssignmentName);
        uploadAssignmentBtn = view.findViewById(R.id.btnUploadAssignment);

        uploadAssignmentBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 1);
        });

        viewAllAssignments();

        assignmentListView.setOnItemClickListener((parent, view1, position, id) -> {
            uploadFileClass uploadFile = fetchFileAssignment.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uploadFile.getUrl()));
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();

            String fileName = uploadAssignmentNameET.getText().toString().trim();
            if (fileName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter assignment name", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadAssignmentBtn.setEnabled(false);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("assignments/uploads/" + fileName);
            storageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            uploadFileClass uploadFile = new uploadFileClass(fileName, downloadUrl);

                            uploadFile.setTimestamp(new java.util.Date());

                            db.collection(COLLECTION_ASSIGNMENT).document(fileName).set(uploadFile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(requireContext(), "Assignment uploaded successfully", Toast.LENGTH_SHORT).show();
                                        uploadAssignmentNameET.setText("");
                                        uploadAssignmentBtn.setEnabled(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to upload assignment details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to upload assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void viewAllAssignments() {
        db.collection(COLLECTION_ASSIGNMENT)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchFileAssignment.clear();
                        for (DocumentChange document : task.getResult().getDocumentChanges()) {
                            uploadFileClass uploadFile = document.getDocument().toObject(uploadFileClass.class);

                            try {
                                fetchFileAssignment.add(0, uploadFile);
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        String[] uploads = new String[fetchFileAssignment.size()];
                        for (int i = 0; i < uploads.length; i++) {
                            uploadFileClass currentUploadFile = fetchFileAssignment.get(i);
                            uploads[i] = currentUploadFile != null && currentUploadFile.getName() != null ? currentUploadFile.getName() : "Unknown";
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(), R.layout.list_item_text, uploads);
                        assignmentListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(requireActivity().getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireActivity().getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}