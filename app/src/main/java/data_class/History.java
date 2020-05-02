package data_class;

import android.media.Image;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class History {

    private ArrayList<String>  receipts;

    public History( ArrayList<String>  receipts) {
        this.receipts = receipts;
    }

    public ArrayList<String> getReceipts() {
        return receipts;
    }

    public void setReceipts(ArrayList<String> receipts) {
        this.receipts = receipts;
    }
}
