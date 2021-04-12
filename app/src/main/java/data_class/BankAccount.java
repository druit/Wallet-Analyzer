package data_class;

import java.io.Serializable;
import java.util.ArrayList;

public class BankAccount  implements Serializable {
    private String bankTitle;
    private int active;
    private String description;
    private ArrayList<Salary> salaryArrayList;
    private double salary;
    private String id;
    private boolean isSalaryBank;

//    private int image;


    public BankAccount(){ }

    public BankAccount(String id, String bank, String description, double salary, int active, ArrayList<Salary> salaryArrayList, boolean isSalaryBank) {
        this.active = active;
        this.bankTitle = bank;
        this.id = id;
//        this.image = image;
        this.description = description;
        this.salary = salary;
        this.salaryArrayList = salaryArrayList;
        this.isSalaryBank = isSalaryBank;
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

    public ArrayList<Salary> getSalaryArrayList() {
        return salaryArrayList;
    }

    public void setSalaryArrayList(ArrayList<Salary> salaryArrayList) {
        this.salaryArrayList = salaryArrayList;
    }

    public boolean isSalaryBank() {
        return isSalaryBank;
    }

    public void setSalaryBank(boolean salaryBank) {
        isSalaryBank = salaryBank;
    }
}
