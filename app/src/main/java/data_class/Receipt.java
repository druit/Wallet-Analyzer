package data_class;

import android.media.Image;
import android.net.Uri;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Receipt {
    private List<Item> items;
    private String address;
    private String storeType;
    private String image;
    private double totalPrice;
    private String barcode;

    public Receipt (ArrayList<Item> items, String address, String storeType, String image, double totalPrice, String barcode){
        this.items = items;
        this.address = address;
        this.storeType = storeType;
        this.image = image;
        this.totalPrice = totalPrice;
        this.barcode = barcode;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }
}
