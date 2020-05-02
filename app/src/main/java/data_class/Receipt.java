package data_class;

import android.media.Image;

import java.util.List;

public class Receipt {
    private List<String> items;
    private String address;
    private String storeType;
    private String image;
    private String totalPrice;
    private String barcode;

    public Receipt (List items,String address,String storeType,String image, String totalPrice,String barcode){
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

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }
}
