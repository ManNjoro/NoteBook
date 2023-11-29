package com.eli.notebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private ArrayList<Note> notes = new ArrayList<>();
    private String userId; // User's unique ID
    private NoteHandler noteHandler;
    private ProgressBar progressBar;
    private TextView notesCountTextView;

    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        recyclerView = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progressBar);
        imageButton = findViewById(R.id.img_add);
        notesCountTextView = findViewById(R.id.notes_count);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(notes, this, new NoteAdapter.ItemClicked() {
            @Override
            public void onClick(int position, View view) {
                // Handle item click, e.g., editing the note
                editNotePosition(position, view);
            }
        });
        recyclerView.setAdapter(noteAdapter);

        // Check if the user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid(); // Get the user's unique ID
            noteHandler = new NoteHandler();
            loadUserNotes();
        } else {
            // Redirect to the login screen if the user is not authenticated
            startActivity(new Intent(NotesActivity.this, MainActivity.class));
            finish();
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) NotesActivity.this.getSystemService((Context.LAYOUT_INFLATER_SERVICE));
                View viewInput = inflater.inflate(R.layout.note_input, null, false);
                EditText editTitle = viewInput.findViewById(R.id.edit_title);
                EditText editDescription = viewInput.findViewById(R.id.edit_description);
                new AlertDialog.Builder(NotesActivity.this)
                        .setView(viewInput)
                        .setTitle("Add Note")
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String title = editTitle.getText().toString();
                                String description = editDescription.getText().toString();
                                Note note = new Note(title, description, userId);
                                DatabaseReference notesReference = FirebaseDatabase.getInstance().getReference("notes");
                                String noteKey = notesReference.push().getKey();
                                note.setId(noteKey);
                                notesReference.child(noteKey).setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(NotesActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(NotesActivity.this, "unable to save", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                dialog.cancel();
                            }
                        }).show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(NotesActivity.this));
        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                new NoteHandler().delete(notes.get(viewHolder.getAdapterPosition()).getId());
                notes.remove(viewHolder.getAdapterPosition());
                noteAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        loadUserNotes();
        displayUserNotesCount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(NotesActivity.this, Profile.class));
        }
        if (item.getItemId() == R.id.deleteAll) {
            deleteAllUserNotes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserNotes() {
        Query userNotesQuery = noteHandler.readUserNotes(userId);

        userNotesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notes.clear(); // Clear the existing notes
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    notes.add(note);
                }
                // Sort the notes based on updatedAt before notifying the adapter
                Collections.sort(notes, new Comparator<Note>() {
                    @Override
                    public int compare(Note note1, Note note2) {
                        // Compare notes based on updatedAt in descending order
                        return Long.compare(note2.getUpdatedAt(), note1.getUpdatedAt());
                    }
                });
                noteAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NotesActivity.this, "Failed to retrieve user notes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editNotePosition(int position, View view) {
        Note note = notes.get(position);

        // Start EditNote activity and pass the note ID
        Intent intent = new Intent(NotesActivity.this, EditNote.class);
        intent.putExtra("userId", note.getUserId());
        intent.putExtra("noteId", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("description", note.getDescription());
        intent.putExtra("updated_at", note.getUpdatedAt());
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(NotesActivity.this,view, ViewCompat.getTransitionName(view));
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1){
            loadUserNotes();
        }
    }

    private void deleteAllUserNotes() {
        // Assuming you have a reference to your database
        DatabaseReference notesReference = FirebaseDatabase.getInstance().getReference("notes");

        // Create a query to retrieve all notes of the current user
        Query userNotesQuery = notesReference.orderByChild("userId").equalTo(userId);

        userNotesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    // Delete the note from the database
                    noteSnapshot.getRef().removeValue();
                }

                // Show a message to indicate that all notes have been deleted
                Toast.makeText(NotesActivity.this, "All notes deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors, if any
                Toast.makeText(NotesActivity.this, "Failed to delete notes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserNotesCount() {
        noteHandler.countUserNotes(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long notesCount = dataSnapshot.getChildrenCount();
                // Display the notes count, e.g., in a TextView
                String countText = "You have " + notesCount + " notes";
                notesCountTextView.setText(countText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors, if any
            }
        });
    }


}
