package am.softlab.arfinance.models;

public class ModelCategory {
    // using same spellings for model variables as they appear in firebase
    String id, category, notes, uid;
    Boolean isIncome;
    long timestamp;

    //constructor empty required for firebase
    public ModelCategory() {
    }

    //constructor with parameters
    public ModelCategory(String id, String category, String notes, String uid, Boolean isIncome, long timestamp) {
        this.id = id;
        this.category = category;
        this.notes = notes;
        this.uid = uid;
        this.isIncome = isIncome;
        this.timestamp = timestamp;
    }

    // GETTERS and SETTERS

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getIsIncome() {
        return isIncome;
    }

    public void setIsIncome(Boolean income) {
        isIncome = income;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
