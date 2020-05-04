package data_class;

import java.util.Date;
import java.util.List;

public class Receipt {
    private List<Item> items;
    private String address;
    private String storeType;
    private String image;
    private double totalPrice;
    private String barcode;
    private Date date;

    public Receipt() {}

    public Receipt(List<Item> items, String address, String storeType, String image, double totalPrice, String barcode, Date date){
        this.items = items;
        this.address = address;
        this.storeType = storeType;
        this.image = image;
        this.totalPrice = totalPrice;
        this.barcode = barcode;
        this.date = date;
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

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
