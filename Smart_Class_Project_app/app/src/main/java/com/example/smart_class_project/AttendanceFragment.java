package com.example.smart_class_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AttendanceFragment extends Fragment {

    ListView attendanceListView;
    FirebaseFirestore db;
    List<uploadFileClass> fetchFileAttendance;
    static final String COLLECTION_ATTENDANCE = "attendance";

    public AttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        // Check network availability
        if (!checkNetworkClass.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

        attendanceListView = view.findViewById(R.id.attendanceListView);
        fetchFileAttendance = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        viewAllAttendance();

        attendanceListView.setOnItemClickListener((parent, view1, position, id) -> {
            uploadFileClass uploadFile = fetchFileAttendance.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uploadFile.getUrl()));
            startActivity(intent);
        });

        return view;
    }

    private void viewAllAttendance() {
        db.collection(COLLECTION_ATTENDANCE).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fetchFileAttendance.clear();
                for (DocumentChange document : task.getResult().getDocumentChanges()) {
                    uploadFileClass uploadFile = document.getDocument().toObject(uploadFileClass.class);

                    try {
                        fetchFileAttendance.add(0, uploadFile);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                String[] uploads = new String[fetchFileAttendance.size()];
                for (int i = 0; i < uploads.length; i++) {
                    uploadFileClass currentUploadFile = fetchFileAttendance.get(i);
                    uploads[i] = currentUploadFile != null && currentUploadFile.getName() != null ? currentUploadFile.getName() : "Unknown";
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(), R.layout.list_item_text, uploads);
                attendanceListView.setAdapter(adapter);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}