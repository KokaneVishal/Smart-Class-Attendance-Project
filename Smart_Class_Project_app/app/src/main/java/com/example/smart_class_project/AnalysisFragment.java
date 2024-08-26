package com.example.smart_class_project;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AnalysisFragment extends Fragment {
    private StorageReference storageReference; // Initialize storageReference
    private FirebaseStorage storage; // Initialize FirebaseStorage

    public AnalysisFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize storageReference
        storageReference = FirebaseStorage.getInstance().getReference();
        // Initialize FirebaseStorage
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        // Check network availability
        if (!checkNetworkClass.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

        ImageView imgView1 = view.findViewById(R.id.reportImgView1);
        ImageView imgView2 = view.findViewById(R.id.reportImgView2);
        ImageView imgView3 = view.findViewById(R.id.reportImgView3);
        ImageView imgView4 = view.findViewById(R.id.reportImgView4);
        Button analysisBtn = view.findViewById(R.id.analysisBtn);
        TextView showCurrentDateTime = view.findViewById(R.id.tvCurrentDateTime);

        // Get the current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("hh:mm aa | dd-MM-yyyy", Locale.getDefault());
        String currentDateTime = dateTimeFormat.format(calendar.getTime());
        showCurrentDateTime.setText("[ " + currentDateTime + " ]");

        analysisBtn.setOnClickListener(v -> {
            // Get the download URL for the file
            StorageReference fileRef = storageReference.child("analysis/Attendance_Percentage_By_Lecture.csv");
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Create an Intent to open the URL in a web browser
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // Set the Intent to open in Chrome explicitly
                intent.setPackage("com.android.chrome");
                // Add the FLAG_ACTIVITY_NEW_TASK flag to open in a new task
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // Verify if there's an activity that can handle the Intent
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    // Start the Intent to open the URL in Chrome
                    startActivity(intent);
                } else {
                    // If Chrome is not available, fallback to any available web browser
                    intent.setPackage(null);
                    startActivity(intent);
                }
            }).addOnFailureListener(exception -> {
                String errorMessage = "Error getting download URL: " + exception.getMessage();
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                Log.e("DownloadFile", errorMessage, exception); // Log the error for debugging
            });
        });

        StorageReference storageRef1 = storage.getReference().child("reports/attendance_report.png");
        StorageReference storageRef2 = storage.getReference().child("reports/bar_report.png");
        StorageReference storageRef3 = storage.getReference().child("reports/overall_attendance.png");
        StorageReference storageRef4 = storage.getReference().child("reports/meeting_criteria_students.png");

        // Load images from Firebase Storage
        loadImageFromStorage(storageRef1, imgView1);
        loadImageFromStorage(storageRef2, imgView2);
        loadImageFromStorage(storageRef3, imgView3);
        loadImageFromStorage(storageRef4, imgView4);

        return view;
    }

    private void loadImageFromStorage(StorageReference storageRef, ImageView imageView) {
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), LENGTH_SHORT).show());
    }
}
