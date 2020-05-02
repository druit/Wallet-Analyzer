package data_class;

import android.net.Uri;

public class User {
    private String firstName,lastName,language;
    private Uri image;

    public User(String firstName, String lastName, Uri image,String language) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
        this.language = language;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
