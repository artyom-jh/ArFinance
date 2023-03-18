package am.softlab.arfinance.models;

public class ModelSchedule {
    // using same spellings for model variables as they appear in firebase
    String id, name, uid;
    boolean enabled;
    long startDateTime;

    String walletId;
    boolean isIncome;
    String categoryId;

    double amount;
    int period;
    long lastStartDateTime;
    long timestamp;

    //constructor empty required for firebase
    public ModelSchedule() {
    }

    public ModelSchedule(String id, String name, String uid, boolean enabled, long startDateTime, String walletId, boolean isIncome, String categoryId, double amount, int period, long lastStartDateTime, long timestamp) {
        this.id = id;
        this.name = name;
        this.uid = uid;
        this.enabled = enabled;
        this.startDateTime = startDateTime;
        this.walletId = walletId;
        this.isIncome = isIncome;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
        this.lastStartDateTime = lastStartDateTime;
        this.timestamp = timestamp;
    }

    // GETTERS and SETTERS

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public boolean getIsIncome() {
        return isIncome;
    }

    public void setIsIncome(boolean income) {
        isIncome = income;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }


    public long getLastStartDateTime() {
        return lastStartDateTime;
    }

    public void setLastStartDateTime(long lastStartDateTime) {
        this.lastStartDateTime = lastStartDateTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
