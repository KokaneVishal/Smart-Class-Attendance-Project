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

public class StudyNotesFragment extends Fragment {

    ListView notesListView;
    FirebaseFirestore db;
    List<uploadFileClass> fetchFileNotes;
    static final String COLLECTION_NOTES = "study_notes";

    public StudyNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_notes, container, false);

        // Check network availability
        if (!checkNetworkClass.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

        notesListView = view.findViewById(R.id.notesListView);
        fetchFileNotes = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        viewAllNotes();

        notesListView.setOnItemClickListener((parent, view1, position, id) -> {
            uploadFileClass uploadFile = fetchFileNotes.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uploadFile.getUrl()));
            startActivity(intent);
        });

        return view;
    }

    private void viewAllNotes() {
        db.collection(COLLECTION_NOTES).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fetchFileNotes.clear();
                for (DocumentChange document : task.getResult().getDocumentChanges()) {
                    uploadFileClass uploadFile = document.getDocument().toObject(uploadFileClass.class);
                    fetchFileNotes.add(0, uploadFile);
                }

                String[] uploads = new String[fetchFileNotes.size()];
                for (int i = 0; i < uploads.length; i++) {
                    uploads[i] = fetchFileNotes.get(i).getName();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(), R.layout.list_item_text, uploads);
                notesListView.setAdapter(adapter);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
