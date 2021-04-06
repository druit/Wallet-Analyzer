package data_class;

import java.io.Serializable;
import java.util.Date;

public class Salary implements Serializable {
    private Date updateDate;
    private double currentSalary;
    private double salaryAdd;
    private Date lastUpdate;


    public Salary(Date updateDate, double currentSalary, double salaryAdd, Date lastUpdate) {
        this.updateDate = updateDate;
        this.currentSalary = currentSalary;
        this.salaryAdd = salaryAdd;
        this.lastUpdate = lastUpdate;
    }

    public  Salary(){ }


    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public double getSalaryAdd() {
        return salaryAdd;
    }

    public void setSalaryAdd(double salaryAdd) {
        this.salaryAdd = salaryAdd;
    }

    public double getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(double currentSalary) {
        this.currentSalary = currentSalary;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
