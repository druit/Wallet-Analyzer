package data_class;

import java.io.Serializable;

public class History implements Serializable {

    private Receipt receipt;
    private String id;

    public History() {}

    public History(String id, Receipt receipt) {
        this.id = id;
        this.receipt = receipt;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}