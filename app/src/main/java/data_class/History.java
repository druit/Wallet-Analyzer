package data_class;

import java.io.Serializable;

public class History implements Serializable, Comparable {

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

    @Override
    public int compareTo(Object o) {
        Receipt receiptArg = ((History) o).getReceipt();

        // this way the older receipts are added to the end of the ArrayList and the newer on the front of the ArrayList
        if (this.getReceipt().getDate().before(receiptArg.getDate())) {
            return 1;
        } else {
            return -1;
        }
    }
}