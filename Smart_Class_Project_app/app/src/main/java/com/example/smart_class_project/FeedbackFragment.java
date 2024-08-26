package com.example.smart_class_project;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackFragment extends Fragment {

    private static final String COLLECTION_FEEDBACK = "feedback";
    private ListView feedbackListView;
    private List<String> feedbackList;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FeedbackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        EditText etFeedback = view.findViewById(R.id.etFeedback);
        Button submitFeedbackBtn = view.findViewById(R.id.submitFeedbackBtn);

        feedbackListView = view.findViewById(R.id.feedbackListView);
        feedbackList = new ArrayList<>();

        submitFeedbackBtn.setOnClickListener(view1 -> {
            String feedbackText = etFeedback.getText().toString();

            if (!feedbackText.trim().isEmpty()) {
                // Create a new HashMap to store feedback details
                Map<String, Object> feedback = new HashMap<>();
                feedback.put("feedbackText", feedbackText);
                feedback.put("timestamp", FieldValue.serverTimestamp()); // Add timestamp

                CollectionReference feedbackCollection = db.collection(COLLECTION_FEEDBACK);

                feedbackCollection.add(feedback)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(view1.getContext(), "Thank you for the Feedback!", Toast.LENGTH_SHORT).show();
                            etFeedback.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(view1.getContext(), "Error saving feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else {
                Toast.makeText(view1.getContext(), "Please enter your Feedback", Toast.LENGTH_SHORT).show();
            }
        });

        db.collection(COLLECTION_FEEDBACK)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract only the feedback text
                            String feedbackText = document.getString("feedbackText");
                            feedbackList.add(feedbackText);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(), R.layout.list_item_text, feedbackList);
                        feedbackListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch Feedback", Toast.LENGTH_SHORT).show();
                    }
                });


        return view;
    }


}