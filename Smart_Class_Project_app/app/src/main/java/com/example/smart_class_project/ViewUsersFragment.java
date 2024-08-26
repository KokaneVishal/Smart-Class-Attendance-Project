package com.example.smart_class_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewUsersFragment extends Fragment {
    private static final String COLLECTION_USERS = "users";
    private ListView userListView;
    private List<String> userList;

    public ViewUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_users, container, false);

        // Initialize
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Initialize ListView
        userListView = view.findViewById(R.id.usersListView);
        userList = new ArrayList<>();

        // Fetch user data
        db.collection(COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get user data as a String along with UID and add to the list
                            String userData = "UID: " + document.getId() + "\n\n" + document.getData().toString();
                            userList.add(userData);
                        }
                        // Create an ArrayAdapter to display the user data in the ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(), R.layout.list_item_text, userList);
                        userListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });

        return view;
    }
}
