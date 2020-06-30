package settings;

public class Language {
    private String currentLanguage;


    public Language(String currentLanguage) {
        this.currentLanguage = currentLanguage;
    }
    public Language(){

    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(String currentLanguage) {
        this.currentLanguage = currentLanguage;
    }
}
