package com.somesteak.finalm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
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

public class EditBook extends AppCompatActivity {

    public static final int READ_EXTERNAL_STORAGE = 0;
    private final int GALLERY_INTENT = 2 ;
    private Uri mImageUri = null;
    private boolean isEditClick = false;
    private boolean removeClick = false;

    private Button editBtn, cancelBtn, removeBtn;
    private String title, author, volume, owned;
    private TextInputEditText txtTitle, txtAuthor, txtLatest, txtOwned;
    private ImageView imageView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private FirebaseStorage storage;
    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = database.getReference(mAuth.getCurrentUser().getUid());

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        txtTitle = findViewById(R.id.editTitle);
        txtAuthor = findViewById(R.id.editAuthor);
        txtLatest = findViewById(R.id.editLatest);
        txtOwned = findViewById(R.id.editOwned);
        imageView = findViewById(R.id.editImage);

        progressBar = findViewById(R.id.edit_spinner);
        cancelBtn = findViewById(R.id.edit_cancelBtn);
        editBtn = findViewById(R.id.editBtn);
        removeBtn = findViewById(R.id.remove);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            title = intent.getStringExtra("title");
            author = intent.getStringExtra("author");
            volume = intent.getStringExtra("volume");
            owned = intent.getStringExtra("owned");

            txtTitle.setText(title);
            txtAuthor.setText(author);
            txtLatest.setText(volume);
            txtOwned.setText(owned);
            if(intent.getStringExtra("image").length() != 0) {
                Glide.with(this)
                        .load(intent.getStringExtra("image"))
                        .into(imageView);
            }
//            byte[] b = intent.getExtras().getByteArray("image");
//            Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
//            imageView.setImageBitmap(bmp);
        }

        cancelBtn.setOnClickListener(v -> finish());

        editBtn.setOnClickListener(v -> {
            databaseReference.child(title).child("latest").setValue(txtLatest.getText().toString());
            databaseReference.child(title).child("owned").setValue(txtOwned.getText().toString());
            if(imgUrl.length() != 0 || imgUrl != null) {
                databaseReference.child(title).child("image").setValue(imgUrl);
            }
            isEditClick = true;
        });

        removeBtn.setOnClickListener(v -> {
            databaseReference.child(title).removeValue();
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView, "Remove Success", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
            finish();
        });

        imageView.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Call for permission", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                }
            } else {
                callGallery();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(isEditClick){
                        View rootView = findViewById(android.R.id.content);
                        Snackbar.make(rootView, "Success", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 4000);

                    }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                View rootView = findViewById(android.R.id.content);
                Snackbar.make(rootView, "Failed to read value.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
            editBtn.setEnabled(false);
            mImageUri = data.getData();
            final StorageReference ref = storageRef.child("images/"+mImageUri.getLastPathSegment());

            UploadTask uploadTask = ref.putFile(mImageUri);
            progressBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
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
                        editBtn.setEnabled(true);
                        imageView.setImageURI(mImageUri);
                        imageView.setVisibility(View.VISIBLE);
                        imgUrl = task.getResult().toString();
                        Log.d("asd", imgUrl);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        editBtn.setEnabled(true);
                        imageView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);


                        View rootView = findViewById(android.R.id.content);
                        Snackbar.make(rootView, "Upload Failed.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
            });
        }
    }
}
