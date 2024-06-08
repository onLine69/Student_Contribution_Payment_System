package model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Information of the students pertaining to the contribution. Use on for
 * TableView.
 */
public class StudentPaymentInfo {
    private final SimpleStringProperty id_number;
    private final SimpleStringProperty first_name;
    private final SimpleStringProperty middle_name;
    private final SimpleStringProperty last_name;
    private final SimpleStringProperty suffix_name;
    private final SimpleStringProperty first_sem_status;
    private final SimpleStringProperty second_sem_status;
    private String year_level;

    public StudentPaymentInfo(String id_number, String first_name, String middle_name, String last_name,
            String suffix_name,
            String year_level, String first_sem_status, String second_sem_status) {
        this.id_number = new SimpleStringProperty(id_number);
        this.first_name = new SimpleStringProperty(first_name);
        this.middle_name = new SimpleStringProperty(middle_name);
        this.last_name = new SimpleStringProperty(last_name);
        this.suffix_name = new SimpleStringProperty(suffix_name);
        this.year_level = year_level;
        this.first_sem_status = new SimpleStringProperty(first_sem_status);
        this.second_sem_status = new SimpleStringProperty(second_sem_status);
    }

    /*
     * Setters and Getters.
     */

    public String getId_number() {
        return id_number.get();
    }

    public void setId_number(String id_number) {
        this.id_number.set(id_number);
    }

    public SimpleStringProperty id_numberProperty() {
        return id_number;
    }

    public String getFirst_name() {
        return first_name.get();
    }

    public void setFirst_name(String first_name) {
        this.first_name.set(first_name);
    }

    public SimpleStringProperty first_nameProperty() {
        return first_name;
    }

    public String getMiddle_name() {
        return middle_name.get();
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name.set(middle_name);
    }

    public SimpleStringProperty middle_nameProperty() {
        return middle_name;
    }

    public String getLast_name() {
        return last_name.get();
    }

    public void setLast_name(String last_name) {
        this.last_name.set(last_name);
    }

    public SimpleStringProperty last_nameProperty() {
        return last_name;
    }

    public String getSuffix_name() {
        return suffix_name.get();
    }

    public void setSuffix_name(String suffix_name) {
        this.suffix_name.set(suffix_name);
    }

    public SimpleStringProperty suffix_nameProperty() {
        return suffix_name;
    }

    public String getYear_level() {
        return year_level;
    }

    public void setYear_level(String year_level) {
        this.year_level = year_level;
    }

    public String getFirst_sem_status() {
        return first_sem_status.get();
    }

    public void setFirst_sem_status(String first_sem_status) {
        this.first_sem_status.set(first_sem_status);
    }

    public SimpleStringProperty first_sem_statusProperty() {
        return first_sem_status;
    }

    public String getSecond_sem_status() {
        return second_sem_status.get();
    }

    public void setSecond_sem_status(String second_sem_status) {
        this.second_sem_status.set(second_sem_status);
    }

    public SimpleStringProperty second_sem_statusProperty() {
        return second_sem_status;
    }
}