package app.psy.innergrowth;

public class DataClass {

    private String dataTitle;
    private String dataDesc;
    private String dataLang;
    private String key;

    public DataClass() {
        // Դատարկ կոնստրուկտոր Firebase-ի համար
    }

    public DataClass(String dataTitle, String dataDesc, String dataLang) {
        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.dataLang = dataLang;
    }

    public String getDataTitle() {
        return dataTitle;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public String getDataLang() {
        return dataLang;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
