package com.eli.notebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditNote extends AppCompatActivity {

    EditText edtTitle, edtDescription;
    Button btnCancel, btnSave;
    LinearLayout linearLayout;
    String userId;
    String noteId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        Intent intent = getIntent();

        userId = intent.getStringExtra("userId");
        noteId = intent.getStringExtra("noteId");
        linearLayout = findViewById(R.id.btn_holder);
        edtDescription = findViewById(R.id.edit_edit_description);
        edtTitle = findViewById(R.id.edit_edit_title);

        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteId != null && !noteId.isEmpty()) {
                    // Create a reference to the specific note in your Firebase Realtime Database
                    DatabaseReference notesReference = FirebaseDatabase.getInstance().getReference("notes").child(noteId);
                    // Update the note with the new data
                    notesReference.child("title").setValue(edtTitle.getText().toString());
                    notesReference.child("description").setValue(edtDescription.getText().toString());
                    notesReference.child("updatedAt").setValue(System.currentTimeMillis())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditNote.this, "Note updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(EditNote.this, "Failed updating", Toast.LENGTH_SHORT).show();
                                    }

                                    onBackPressed();
                                }
                            });
                } else {
                    // Handle the case when noteId is not valid
                    Toast.makeText(EditNote.this, "Invalid note ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        edtDescription.setText(intent.getStringExtra("description"));
        edtTitle.setText(intent.getStringExtra("title"));
    }

    @Override
    public void onBackPressed() {
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(linearLayout);
        super.onBackPressed();
    }
}