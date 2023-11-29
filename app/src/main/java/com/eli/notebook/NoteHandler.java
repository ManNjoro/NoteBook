package com.eli.notebook;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class NoteHandler {
    private DatabaseReference notesReference;


    public NoteHandler() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notesReference = database.getReference("notes");
    }


    // CREATE operation
    public String create(Note note) {
        DatabaseReference newNoteRef = notesReference.push();
        newNoteRef.setValue(note);
        return newNoteRef.getKey();
    }

    // READ operation for user-specific notes
    public Query readUserNotes(String userId) {
        // Query the notes for a specific user based on their user ID
        return notesReference.orderByChild("userId").equalTo(userId);
    }

    // UPDATE operation
    public void update(String noteKey, Note updatedNote) {
        notesReference.child(noteKey).setValue(updatedNote);
    }

    // DELETE operation
    public void delete(String noteKey) {
        notesReference.child(noteKey).removeValue();
    }

    public void countUserNotes(String userId, ValueEventListener listener) {
        DatabaseReference userNotesRef = FirebaseDatabase.getInstance().getReference("notes");
        Query userNotesQuery = userNotesRef.orderByChild("userId").equalTo(userId);

        userNotesQuery.addValueEventListener(listener);
    }
}
