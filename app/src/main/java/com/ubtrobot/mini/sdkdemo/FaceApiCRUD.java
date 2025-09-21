package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtrobot.commons.ResponseListener;

import java.util.ArrayList;

public class FaceApiCRUD extends Activity {

    private EditText etPage, etPageSize, etFaceId, etNewName, etDeleteIds;
    private Button btnQuery, btnUpdate, btnDelete;
    private ListView lvFaces;
    private TextView tvStatus;

    private ArrayList<FaceInfo> currentFaces = new ArrayList<>();
    private ArrayAdapter<FaceInfo> adapter;

    // Assuming you have a face service instance
    private FaceApi faceService; // This would be initialized from your SDK

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_api_crud);
        faceService = FaceApi.get();

        // Initialize views
        etPage = findViewById(R.id.etPage);
        etPageSize = findViewById(R.id.etPageSize);
        etFaceId = findViewById(R.id.etFaceId);
        etNewName = findViewById(R.id.etNewName);
        etDeleteIds = findViewById(R.id.etDeleteIds);
        btnQuery = findViewById(R.id.btnQuery);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        lvFaces = findViewById(R.id.lvFaces);
        tvStatus = findViewById(R.id.tvStatus);

        // Initialize face service (replace with actual initialization from your SDK)
        // faceService = YourSDK.getFaceService();

        // Setup list adapter
        adapter = new ArrayAdapter<FaceInfo>(this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                currentFaces) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                FaceInfo face = currentFaces.get(position);
                text1.setText(face.getName());
                text2.setText("ID: " + face.getId() + " | Age: " + face.getAge() + " | Gender: " + getGenderString(face.getGender()) + " | Avatar: " + face.getAvtar());
                return view;
            }
        };
        lvFaces.setAdapter(adapter);

        // Set up click listeners
        btnQuery.setOnClickListener(v -> queryFaces());
        btnUpdate.setOnClickListener(v -> updateFace());
        btnDelete.setOnClickListener(v -> deleteFaces());

        lvFaces.setOnItemClickListener((parent, view, position, id) -> {
            FaceInfo selectedFace = currentFaces.get(position);
            etFaceId.setText(selectedFace.getId());
            etNewName.setText(selectedFace.getName());
            etDeleteIds.setText(selectedFace.getId());
        });
    }

    private String getGenderString(int gender) {
        switch (gender) {
            case 1: return "Male";
            case 2: return "Female";
            default: return "Unknown";
        }
    }

    private void queryFaces() {
        try {
            int page = Integer.parseInt(etPage.getText().toString());
            int pageSize = Integer.parseInt(etPageSize.getText().toString());

            tvStatus.setText("Querying faces...");

            // Replace with actual SDK call
            faceService.query(page, pageSize, new ResponseListener<ArrayList<FaceInfo>>() {
                @Override
                public void onResponseSuccess(ArrayList<FaceInfo> faceInfos) {
                    runOnUiThread(() -> {
                        currentFaces.clear();
                        currentFaces.addAll(faceInfos);
                        adapter.notifyDataSetChanged();
                        tvStatus.setText("Found " + faceInfos.size() + " faces");
                    });
                }

                @Override
                public void onFailure(int errorCode, @NonNull String errorMessage) {
                    runOnUiThread(() -> {
                        tvStatus.setText("Query failed: " + errorMessage);
                        Toast.makeText(FaceApiCRUD.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (NumberFormatException e) {
            tvStatus.setText("Invalid page numbers");
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFace() {
        String faceId = etFaceId.getText().toString().trim();
        String newName = etNewName.getText().toString().trim();

        if (faceId.isEmpty() || newName.isEmpty()) {
            tvStatus.setText("Please enter both ID and name");
            Toast.makeText(this, "Please enter both ID and name", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("Updating face...");

        // Replace with actual SDK call
        faceService.update(faceId, newName, new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void result) {
                runOnUiThread(() -> {
                    tvStatus.setText("Face updated successfully");
                    Toast.makeText(FaceApiCRUD.this, "Face updated", Toast.LENGTH_SHORT).show();
                    // Refresh the list
                    queryFaces();
                });
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMessage) {
                runOnUiThread(() -> {
                    tvStatus.setText("Update failed: " + errorMessage);
                    Toast.makeText(FaceApiCRUD.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteFaces() {
        String idsText = etDeleteIds.getText().toString().trim();

        if (idsText.isEmpty()) {
            tvStatus.setText("Please enter IDs to delete");
            Toast.makeText(this, "Please enter IDs to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse comma-separated IDs
        String[] idArray = idsText.split(",");
        ArrayList<String> idsToDelete = new ArrayList<>();
        for (String id : idArray) {
            String trimmedId = id.trim();
            if (!trimmedId.isEmpty()) {
                idsToDelete.add(trimmedId);
            }
        }

        if (idsToDelete.isEmpty()) {
            tvStatus.setText("No valid IDs to delete");
            return;
        }

        tvStatus.setText("Deleting " + idsToDelete.size() + " faces...");

        // Replace with actual SDK call
        faceService.delete(idsToDelete, new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void result) {
                runOnUiThread(() -> {
                    tvStatus.setText("Faces deleted successfully");
                    Toast.makeText(FaceApiCRUD.this, "Faces deleted", Toast.LENGTH_SHORT).show();
                    // Clear delete field and refresh list
                    etDeleteIds.setText("");
                    queryFaces();
                });
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMessage) {
                runOnUiThread(() -> {
                    tvStatus.setText("Delete failed: " + errorMessage);
                    Toast.makeText(FaceApiCRUD.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-query on resume
        queryFaces();
    }
}