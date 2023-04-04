package am.softlab.arfinance.models;

import java.io.Serializable;

import am.softlab.arfinance.Constants;

public class ModelSettings implements Serializable {
    private long st_id;
    private String user_id;
    private String st_enc_pass;
    private int st_max_pie_sectors;

    public ModelSettings() {
        this(-1, "", "", Constants.MAX_PIE_SECTORS_LIMIT);
    }

    public ModelSettings(ModelSettings model) {
        this(model.getSt_id(), model.getUser_id(), model.getSt_enc_pass(), model.st_max_pie_sectors);
    }

    public ModelSettings(long st_id, String user_id, String st_enc_pass, int st_max_pie_sectors) {
        this.st_id = st_id;
        this.user_id = user_id;
        this.st_enc_pass = st_enc_pass;
        this.st_max_pie_sectors = st_max_pie_sectors;
    }

    public long getSt_id() {
        return st_id;
    }

    public void setSt_id(long st_id) {
        this.st_id = st_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSt_enc_pass() {
        return st_enc_pass;
    }

    public void setSt_enc_pass(String st_enc_pass) {
        this.st_enc_pass = st_enc_pass;
    }

    public int getSt_max_pie_sectors() {
        return st_max_pie_sectors;
    }

    public void setSt_max_pie_sectors(int st_max_pie_sectors) {
        this.st_max_pie_sectors = st_max_pie_sectors;
    }
}
