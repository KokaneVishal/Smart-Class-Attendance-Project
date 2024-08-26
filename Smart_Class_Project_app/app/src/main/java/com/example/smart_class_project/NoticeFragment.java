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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NoticeFragment extends Fragment {

    ListView noticeListView;
    FirebaseFirestore db;
    List<uploadFileClass> fetchFileNotice;
    static final String COLLECTION_NOTES = "notice";

    public NoticeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);

        // Check network availability
        if (!checkNetworkClass.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

        noticeListView = view.findViewById(R.id.noticeListView);
        fetchFileNotice = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        viewAllNotice();

        noticeListView.setOnItemClickListener((parent, view1, position, id) -> {
            uploadFileClass uploadFile = fetchFileNotice.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uploadFile.getUrl()));
            startActivity(intent);
        });

        return view;
    }

    private void viewAllNotice() {
        db.collection(COLLECTION_NOTES).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fetchFileNotice.clear();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    uploadFileClass uploadFile = document.toObject(uploadFileClass.class);
                    fetchFileNotice.add(0, uploadFile);
                }

                String[] uploads = new String[fetchFileNotice.size()];
                for (int i = 0; i < uploads.length; i++) {
                    uploads[i] = fetchFileNotice.get(i).getName();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(), R.layout.list_item_text, uploads);
                noticeListView.setAdapter(adapter);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
