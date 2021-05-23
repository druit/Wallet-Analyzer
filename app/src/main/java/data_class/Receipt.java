package data_class;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Receipt implements Serializable {
    private List<Item> items;
    private String address;
    private String storeType;
    private String image;
    private double totalPrice;
    private String barcode;
    private Date date;
    private String storeName;

    public Receipt() {}

    public Receipt(List<Item> items, String address, String storeType, String image, double totalPrice, String barcode, Date date, String storeName) {
        this.items = items;
        this.address = address;
        this.storeType = storeType;
        this.image = image;
        this.totalPrice = totalPrice;
        this.barcode = barcode;
        this.date = date;
        this.storeName = storeName;
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

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double updateTotalPrice() {
        double tempTotalPrice = 0;
        for (Item tempItem : items) {
            tempTotalPrice += tempItem.getPrice();
        }
        this.totalPrice = tempTotalPrice;
        return tempTotalPrice;
    }
}
