package com.somesteak.finalm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Timer;
import java.util.TimerTask;

public class addBook extends AppCompatActivity {

    private ImageView image;
    public static final int READ_EXTERNAL_STORAGE = 0;
    private final int GALLERY_INTENT = 2 ;
    private Uri mImageUri = null;
    private StorageReference storageRef;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private Button addButton, cancleBtn;
    private TextInputEditText title;
    private TextInputEditText author;
    private TextInputEditText latest;
    private TextInputEditText owned;
    private ProgressBar progressBar;
    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(currentUser.getUid());

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        addButton = findViewById(R.id.addBtn);
        cancleBtn = findViewById(R.id.cancelBtn);
        image = findViewById(R.id.imageSelect);

        title = findViewById(R.id.txtTitle);
        author = findViewById(R.id.txtAuthor);
        latest = findViewById(R.id.txtLatest);
        owned = findViewById(R.id.txtOwned);

        progressBar = findViewById(R.id.loading_spinner);

        image.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Call for permission", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                }
            } else {
                callGallery();
            }
        });

        addButton.setOnClickListener(v -> {
            myRef.child(title.getText().toString()).child("title").setValue(title.getText().toString());
            myRef.child(title.getText().toString()).child("author").setValue(author.getText().toString());
            myRef.child(title.getText().toString()).child("latest").setValue(latest.getText().toString());
            myRef.child(title.getText().toString()).child("owned").setValue(owned.getText().toString());
            myRef.child(title.getText().toString()).child("image").setValue(imgUrl);

            cancleBtn.setEnabled(false);
        });

        cancleBtn.setOnClickListener(v -> finish());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                boolean check = false;
                for(DataSnapshot child : dataSnapshot.getChildren() ){
                    if(child.getKey().equals(title.getText().toString())){
                        check = true;
                        View rootView = findViewById(android.R.id.content);
                        Snackbar.make(rootView, "Success", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 4000);

                        break;

                    }
                }

                if(!check) {
                    cancleBtn.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
               Log.w("TAG", "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE:
                if(grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callGallery();
                    return;
                }
                Toast.makeText(getApplicationContext(), "...", Toast.LENGTH_LONG);
        }
    }

    private void callGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            addButton.setEnabled(false);
            mImageUri = data.getData();
            final StorageReference ref = storageRef.child("images/"+mImageUri.getLastPathSegment());

            UploadTask uploadTask = ref.putFile(mImageUri);
            progressBar.setVisibility(View.VISIBLE);
            image.setVisibility(View.INVISIBLE);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        image.setImageURI(mImageUri);
                        image.setVisibility(View.VISIBLE);
                        imgUrl = task.getResult().toString();
                        progressBar.setVisibility(View.GONE);
                        addButton.setEnabled(true);
                    } else {
                        addButton.setEnabled(true);
                        image.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                        View rootView = findViewById(android.R.id.content);
                        Snackbar.make(rootView, "Upload Failed", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    }
                }
            });
        }
    }

}
