package am.softlab.arfinance.models;

public class ModelOperation {
    // using same spellings for model variables as they appear in firebase
    String id, categoryId, notes, uid;
    boolean isIncome;
    double amount;
    long operationTimestamp, timestamp;

    //constructor empty required for firebase
    public ModelOperation() {
    }

    //constructor with parameters
    public ModelOperation(String id, long operationTimestamp, String categoryId, String notes, String uid, boolean isIncome, double amount, long timestamp) {
        this.id = id;
        this.operationTimestamp = operationTimestamp;
        this.categoryId = categoryId;
        this.notes = notes;
        this.uid = uid;
        this.isIncome = isIncome;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // GETTERS and SETTERS

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getOperationTimestamp() {
        return operationTimestamp;
    }

    public void setOperationTimestamp(long operationTimestamp) {
        this.operationTimestamp = operationTimestamp;
    }
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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

    public boolean getIsIncome() {
        return isIncome;
    }

    public void setIsIncome(boolean isIncome) {
        this.isIncome = isIncome;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
