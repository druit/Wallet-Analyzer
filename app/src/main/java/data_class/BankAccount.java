package data_class;

import java.io.Serializable;

public class BankAccount  implements Serializable {
    private String bankTitle;
    private int active;
    private String description;
    private double salary;
    private String id;

//    private int image;


    public BankAccount(){ }

    public BankAccount(String id,String bank, String description, double salary,int active) {
        this.active = active;
        this.bankTitle = bank;
        this.id = id;
//        this.image = image;
        this.description = description;
        this.salary = salary;
    }


    public String getDescription() {
        return description;
    }

    public double getSalary() {
        return salary;
    }

//    public int getImages() {
//        return image;
//    }

    public String getBankTitle() {
        return bankTitle;
    }

    public void setBankTitle(String bankTitle) {
        this.bankTitle = bankTitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public void setImages(int images) {
//        this.image = images;
//    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public int isActive() {
        return active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
