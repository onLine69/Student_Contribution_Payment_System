package control;

import model.DataManager;
import model.UnverifiedPayment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Facilitates the verification process of the BUFICOM to the payments.
 */
public class VerifyPaymentsControl {
    private static Connection connect;
    @FXML
    private ComboBox<String> program_code_combobox;
    @FXML
    private ComboBox<String> year_level_combobox;
    @FXML
    private TextField search_id_textfield;
    @FXML
    private Button search_student_button;
    @FXML
    private ComboBox<String> contribution_code_combobox;
    @FXML
    private TableView<UnverifiedPayment> unverified_payments_table;
    @FXML
    private TableColumn<UnverifiedPayment, String> id_column;
    @FXML
    private TableColumn<UnverifiedPayment, String> first_name_column;
    @FXML
    private TableColumn<UnverifiedPayment, String> middle_name_column;
    @FXML
    private TableColumn<UnverifiedPayment, String> last_name_column;
    @FXML
    private TableColumn<UnverifiedPayment, String> suffix_name_column;
    @FXML
    private TableColumn<UnverifiedPayment, String> status_column;
    private String org_code;
    private String academic_year;

    public VerifyPaymentsControl() {
    }

    /**
     * Initialize the display and data necessary for the frame.
     *
     * @param org_code
     */
    public void initialize(String org_code) {
        connect = DataManager.getConnect();
        this.org_code = org_code;
        academic_year = DataManager.getAcademic_year();

        setupContributions();
        displayPayments();
        setupSearchBlock();
        searchSetup();
    }

    /**
     * Enable the search button when an input is provided in the textfield.
     */
    private void searchSetup() {
        search_id_textfield.textProperty().addListener((ov, t, textField) -> {
            if (textField.isEmpty()) {
                search_student_button.setDisable(true);
                refresh_button_clicked();
            } else {
                search_student_button.setDisable(false);
            }
        });
    }

    /**
     * Set up the contribution codes.
     */
    private void setupContributions() {
        try {
            String contribution_code_query = "SELECT `contribution_code` "
                    + "FROM `contributions` "
                    + "WHERE `collecting_org_code` = '" + org_code + "' "
                    + "AND `academic_year` = '" + academic_year + "';";
            PreparedStatement get_contribution_code = connect.prepareStatement(contribution_code_query);
            ResultSet result = get_contribution_code.executeQuery();
            ObservableList<String> contribution_list = FXCollections.observableArrayList();
            while (result.next()) {
                contribution_list.add(result.getString("contribution_code"));
            }
            contribution_code_combobox.setItems(contribution_list);
            contribution_code_combobox.getSelectionModel().selectFirst();
            // refresh the parameters for search when changing contributions to be displayed
            contribution_code_combobox.setOnAction(e -> {
                refresh_button_clicked();
            });
            get_contribution_code.close();
        } catch (SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }
    }

    /**
     * Fetch and display the unverified payments.
     */
    private void displayPayments() {
        unverified_payments_table.getItems().clear();
        ObservableList<UnverifiedPayment> unverified_list = FXCollections.observableArrayList();
        try {
            String unverified_payments_query = "SELECT `id_number`, `first_name`, `middle_name`, `last_name`, `suffix_name`, `status`, `transaction_id` "
                    + "FROM `pays` AS p LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                    + "WHERE p.`contribution_code` = '"
                    + contribution_code_combobox.getSelectionModel().getSelectedItem() + "' "
                    + "AND p.`status` = 'Pending';";
            PreparedStatement get_unverified_payments = connect.prepareStatement(unverified_payments_query);
            ResultSet result = get_unverified_payments.executeQuery();
            while (result.next()) {
                String id_number = result.getString("id_number");
                String first_name = result.getString("first_name");
                String middle_name = result.getString("middle_name");
                String last_name = result.getString("last_name");
                String suffix_name = result.getString("suffix_name");
                String status = result.getString("status");
                long transaction_id = result.getLong("transaction_id");

                unverified_list.add(new UnverifiedPayment(id_number, first_name, middle_name, last_name, suffix_name,
                        status, transaction_id));
            }
            setupData(unverified_list);
            get_unverified_payments.close();
        } catch (SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }
    }

    /**
     * Set up the table where unverified payments are to be displayed.
     *
     * @param data
     */
    private void setupData(ObservableList<UnverifiedPayment> data) {
        unverified_payments_table.getItems().clear();

        id_column.setCellValueFactory(new PropertyValueFactory<>("id_number"));
        first_name_column.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        middle_name_column.setCellValueFactory(new PropertyValueFactory<>("middle_name"));
        last_name_column.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        suffix_name_column.setCellValueFactory(new PropertyValueFactory<>("suffix_name"));
        status_column.setCellValueFactory(new PropertyValueFactory<>("status"));
        id_column.setStyle("-fx-alignment: CENTER;");
        first_name_column.setStyle("-fx-alignment: CENTER;");
        middle_name_column.setStyle("-fx-alignment: CENTER;");
        last_name_column.setStyle("-fx-alignment: CENTER;");
        suffix_name_column.setStyle("-fx-alignment: CENTER;");
        status_column.setStyle("-fx-alignment: CENTER;");

        unverified_payments_table.setItems(data);
    }

    /**
     * Set up the necessary parameters for searching blocks.
     */
    private void setupSearchBlock() {
        ObservableList<String> block_list = FXCollections.observableArrayList();
        block_list.add("--Select Program--");
        try {
            String program_code_query = "SELECT DISTINCT `program_code` "
                    + "FROM `members` AS m LEFT JOIN `students` AS s ON m.`member_id` = s.`id_number` "
                    + "WHERE m.`organization_code` = '" + org_code + "';";
            PreparedStatement get_program_code = connect.prepareStatement(program_code_query);
            ResultSet result = get_program_code.executeQuery();
            while (result.next()) {
                block_list.add(result.getString("program_code"));
            }
            get_program_code.close();
        } catch (SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }
        program_code_combobox.setItems(block_list);
        program_code_combobox.getSelectionModel().selectFirst();

        ObservableList<String> year_list = FXCollections.observableArrayList();
        year_list.addAll("--Select Year--", "1st Year", "2nd Year", "3rd Year", "4th Year");
        year_level_combobox.setItems(year_list);
        year_level_combobox.getSelectionModel().selectFirst();
    }

    /**
     * Facilitates the searching of the block based on the parameters.
     */
    @FXML
    private void search_block_button_clicked() {
        if (!program_code_combobox.getValue().equals("--Select Program--")
                && !year_level_combobox.getValue().equals("--Select Year--")) {
            displayPayments();
            search_id_textfield.clear();

            ObservableList<UnverifiedPayment> unverified_list = FXCollections.observableArrayList();
            try {
                String unverified_payments_query = "SELECT `id_number`, `first_name`, `middle_name`, `last_name`, `suffix_name`, `status`, `transaction_id` "
                        + "FROM `pays` AS p LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                        + "WHERE s.`program_code` = '" + program_code_combobox.getSelectionModel().getSelectedItem()
                        + "' "
                        + "AND s.`year_level` = '" + year_level_combobox.getSelectionModel().getSelectedItem() + "' "
                        + "AND p.`contribution_code` = '"
                        + contribution_code_combobox.getSelectionModel().getSelectedItem() + "' "
                        + "AND p.`status` = 'Pending';";
                PreparedStatement get_unverified_payments = connect.prepareStatement(unverified_payments_query);
                ResultSet result = get_unverified_payments.executeQuery();
                while (result.next()) {
                    String id_number = result.getString("id_number");
                    String first_name = result.getString("first_name");
                    String middle_name = result.getString("middle_name");
                    String last_name = result.getString("last_name");
                    String suffix_name = result.getString("suffix_name");
                    String status = result.getString("status");
                    long transaction_id = result.getLong("transaction_id");

                    unverified_list.add(new UnverifiedPayment(id_number, first_name, middle_name, last_name,
                            suffix_name, status, transaction_id));
                }
                setupData(unverified_list);
                get_unverified_payments.close();
            } catch (SQLException e) {
                Alert connection_error = new Alert(Alert.AlertType.ERROR);
                connection_error.setTitle("Database Connection Error");
                connection_error.setHeaderText("Check your connection.");
                connection_error.setContentText(e.toString());
                connection_error.showAndWait();
                System.exit(0);
            }
        } else {
            Border error_border = new Border(
                    new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
            if (program_code_combobox.getValue().equals("--Select Program--"))
                program_code_combobox.setBorder(error_border);
            if (year_level_combobox.getValue().equals("--Select Program--"))
                year_level_combobox.setBorder(error_border);
        }
    }

    /**
     * Facilitates the searching of the payment based on the payer's id.
     */
    @FXML
    private void search_id_button_clicked() {
        displayPayments();
        program_code_combobox.getSelectionModel().selectFirst();
        year_level_combobox.getSelectionModel().selectFirst();

        ObservableList<UnverifiedPayment> payer_with_id_list = FXCollections.observableArrayList();
        try {
            String search_id_query = "SELECT `id_number`, `first_name`, `middle_name`, `last_name`, `suffix_name`, `status`, `transaction_id` "
                    + "FROM `pays` AS p LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                    + "WHERE p.`payer_id` LIKE '%" + search_id_textfield.getText() + "%' "
                    + "AND p.`contribution_code` = '" + contribution_code_combobox.getSelectionModel().getSelectedItem()
                    + "' "
                    + "AND p.`status` = 'Pending';";

            PreparedStatement get_student_id = connect.prepareStatement(search_id_query);
            ResultSet result = get_student_id.executeQuery();
            while (result.next()) {
                String id_number = result.getString("id_number");
                String first_name = result.getString("first_name");
                String middle_name = result.getString("middle_name");
                String last_name = result.getString("last_name");
                String suffix_name = result.getString("suffix_name");
                String status = result.getString("status");
                long transaction_id = result.getLong("transaction_id");

                payer_with_id_list.add(new UnverifiedPayment(id_number, first_name, middle_name, last_name, suffix_name,
                        status, transaction_id));
            }
            setupData(payer_with_id_list);
            get_student_id.close();
        } catch (SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }
    }

    /**
     * Refresh the display.
     */
    @FXML
    private void refresh_button_clicked() {
        displayPayments();
        program_code_combobox.getSelectionModel().selectFirst();
        year_level_combobox.getSelectionModel().selectFirst();
        search_id_textfield.clear();
    }

    /**
     * Display and facilitates the verify transaction form.
     */
    @FXML
    private void verify_contribution() {
        UnverifiedPayment payment = unverified_payments_table.getSelectionModel().getSelectedItem();
        if (payment != null) {
            try {
                Stage transaction_stage = new Stage();
                transaction_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
                transaction_stage.setResizable(false);
                transaction_stage.initModality(Modality.APPLICATION_MODAL);
                FXMLLoader verify_form_loader = new FXMLLoader(
                        getClass().getResource("/view/BUFICOM-FRAMES/verify-transaction-form.fxml"));
                Parent transaction_parent = verify_form_loader.load();
                VerifyTransactionControl transaction_process = verify_form_loader.getController();
                transaction_process.initialize(payment);

                Scene transaction_scene = new Scene(transaction_parent);
                transaction_stage.setTitle("Verify Payment");
                transaction_stage.setScene(transaction_scene);
                transaction_stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Alert non_selected = new Alert(Alert.AlertType.ERROR);
            non_selected.setTitle("No Payment Selected");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Please Select a Payment.");
            non_selected.showAndWait();
        }
    }
}
