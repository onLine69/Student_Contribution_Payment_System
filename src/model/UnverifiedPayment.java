package model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Information of the unverified payments.
 */
public class UnverifiedPayment {
    private final SimpleStringProperty id_number;
    private final SimpleStringProperty first_name;
    private final SimpleStringProperty middle_name;
    private final SimpleStringProperty last_name;
    private final SimpleStringProperty suffix_name;
    private final SimpleStringProperty status;
    private long transaction_id;

    /**
     * Object representing unverified payment.
     * 
     * @param id_number
     * @param first_name
     * @param middle_name
     * @param last_name
     * @param suffix_name
     * @param status
     * @param transaction_id
     */
    public UnverifiedPayment(String id_number, String first_name, String middle_name, String last_name,
            String suffix_name,
            String status, long transaction_id) {
        this.id_number = new SimpleStringProperty(id_number);
        this.first_name = new SimpleStringProperty(first_name);
        this.middle_name = new SimpleStringProperty(middle_name);
        this.last_name = new SimpleStringProperty(last_name);
        this.suffix_name = new SimpleStringProperty(suffix_name);
        this.status = new SimpleStringProperty(status);
        this.transaction_id = transaction_id;
    }

    public String getId_number() {
        return id_number.get();
    }

    public SimpleStringProperty id_numberProperty() {
        return id_number;
    }

    public String getFirst_name() {
        return first_name.get();
    }

    public SimpleStringProperty first_nameProperty() {
        return first_name;
    }

    public String getMiddle_name() {
        return middle_name.get();
    }

    public SimpleStringProperty middle_nameProperty() {
        return middle_name;
    }

    public String getLast_name() {
        return last_name.get();
    }

    public SimpleStringProperty last_nameProperty() {
        return last_name;
    }

    public String getSuffix_name() {
        return suffix_name.get();
    }

    public SimpleStringProperty suffix_nameProperty() {
        return suffix_name;
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public long getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(long transaction_id) {
        this.transaction_id = transaction_id;
    }
}