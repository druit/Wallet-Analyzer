package data_class;

import java.util.List;

public class History {

    private Receipt receipts;
    private String id;

    public History(String id,Receipt receipts) {
        this.id = id;
        this.receipts = receipts;
    }

    public Receipt getReceipts() {
        return receipts;
    }

    public void setReceipts(Receipt receipts) {
        this.receipts = receipts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}