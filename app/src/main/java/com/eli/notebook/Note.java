package com.eli.notebook;
import java.util.HashMap;
import java.util.Map;

public class Note {
    private String id;
    private String title;
    private String description;
    private String userId; // User ID associated with the note
    private long createdAt; // Timestamp when the note was created
    private long updatedAt; // Timestamp when the note was last updated

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNoteUpdate() {
        return noteUpdate;
    }

    public void setNoteUpdate(String noteUpdate) {
        this.noteUpdate = noteUpdate;
    }

    private String noteUpdate;

    public Note() {
        // Default constructor required for Firebase
    }

    public Note(String title, String description, String userId) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis(); // Set the creation timestamp
        this.updatedAt = createdAt; // Initially, set updated timestamp to creation timestamp
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = System.currentTimeMillis(); // Update the timestamp when the title is updated
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis(); // Update the timestamp when the description is updated
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    // Convert the Note object to a Map for Firebase database updates
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("description", description);
        result.put("userId", userId);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        result.put("noteUpdate", noteUpdate);
        return result;
    }
}
