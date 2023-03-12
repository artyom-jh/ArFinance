package am.softlab.arfinance.models;

public class ModelWallet {

    // using same spellings for model variables as they appear in firebase
    String id, walletName, notes, uid;
    String currencyName, currencyCode, currencySymbol;
    double balance;
    int usageCount;
    long timestamp;

    //constructor empty required for firebase
    public ModelWallet() {
    }

    //constructor with parameters

    public ModelWallet(String id, String walletName, String notes, String uid, String currencyName, String currencyCode, String currencySymbol, double balance, int usageCount, long timestamp) {
        this.id = id;
        this.walletName = walletName;
        this.notes = notes;
        this.uid = uid;
        this.currencyName = currencyName;
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.balance = balance;
        this.usageCount = usageCount;
        this.timestamp = timestamp;
    }


    // GETTERS and SETTERS


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
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

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
