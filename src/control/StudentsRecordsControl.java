package control;

import model.ContributionProperties;
import model.DataManager;
import model.StudentPaymentInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Facilitates the process of the Class Rep frame.
 */
public class StudentsRecordsControl {
    private static Connection connect;
    @FXML
    private TableView<ContributionProperties> contribution_data_table;
    @FXML
    private TableColumn<ContributionProperties, String> code_column;
    @FXML
    private TableColumn<ContributionProperties, String> sem_column;
    @FXML
    private TableColumn<ContributionProperties, Integer> amount_column;
    @FXML
    private TableView<StudentPaymentInfo> student_data_table;
    @FXML
    private TableColumn<StudentPaymentInfo, String> id_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> first_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> middle_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> last_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> suffix_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> first_sem_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> second_sem_column;
    @FXML
    private TextField search_id;
    @FXML
    private Button search_student_button;
    @FXML
    private MenuItem show_first_sem_transaction;
    @FXML
    private MenuItem show_second_sem_transaction;
    @FXML
    private ComboBox<String> program_code_combobox;
    @FXML
    private ComboBox<String> year_level_combobox;
    @FXML
    private TextField paid_students1;
    @FXML
    private TextField paid_students2;
    @FXML
    private TextField money_collected1;
    @FXML
    private TextField money_collected2;
    private String academic_year;
    private String org_code;

    public StudentsRecordsControl() {
    }

    /**
     * Set the initial display along with the necessary information.
     *
     * @param org_code
     */
    public void initialize(String org_code) {
        connect = DataManager.getConnect();
        this.org_code = org_code;
        academic_year = DataManager.getAcademic_year();

        setupSearchBlock();
        setupContributionsData();
        setupStudentsData(getPayersList(null, null, null));
        searchSetup();
        getStatistics(null, null);
    }

    /**
     * Enable the search button when an input is provided in the textField.
     */
    private void searchSetup() {
        search_id.textProperty().addListener((ov, t, textField) -> {
            if (textField.isEmpty()) {
                search_student_button.setDisable(true);
                resetSearch();
            } else {
                search_student_button.setDisable(false);
            }
        });
    }

    /**
     * Fetch and display the statistics of the students.
     */
    private void getStatistics(String program_code, String year_level) {
        try {
            // prepare the queries based on the parameters
            String program_code_set;
            String year_level_set;
            if (program_code == null && year_level == null) { // if there is no specific block to be displayed
                program_code_set = "";
                year_level_set = "";
            } else {
                program_code_set = "AND s.`program_code` = '" + program_code + "' ";
                year_level_set = "AND s.`year_level` = '" + year_level + "' ";
            }

            String students_paid_1_query = "SELECT COUNT(p.`status`) AS `Students Paid` "
                    + "FROM `pays` AS p LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                    + "WHERE p.`contribution_code` = '"
                    + contribution_data_table.getItems().get(0).getContribution_code() + "' "
                    + program_code_set
                    + year_level_set
                    + "AND p.`status` = 'Accepted';";
            String students_paid_2_query = "SELECT COUNT(p.`status`) AS `Students Paid` "
                    + "FROM `pays` AS p LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                    + "WHERE p.`contribution_code` = '"
                    + contribution_data_table.getItems().get(1).getContribution_code() + "' "
                    + program_code_set
                    + year_level_set
                    + "AND p.`status` = 'Accepted';";
            String students_total_query = "SELECT COUNT(m.`member_id`) AS `Students Total` "
                    + "FROM `members` AS m LEFT JOIN `students` AS s "
                    + "ON m.`member_id` = s.`id_number` "
                    + "WHERE m.`organization_code` = '" + org_code + "' "
                    + program_code_set
                    + year_level_set + ";";

            // get the number of paid students in first sem
            PreparedStatement get_students_paid_1 = connect.prepareStatement(students_paid_1_query);
            ResultSet result = get_students_paid_1.executeQuery();
            int student_paid_1_count = 0;
            if (result.next()) {
                student_paid_1_count += result.getInt("Students Paid");
            }
            get_students_paid_1.close();

            // get the number of paid students in second sem
            PreparedStatement get_students_paid_2 = connect.prepareStatement(students_paid_2_query);
            result = get_students_paid_2.executeQuery();
            int student_paid_2_count = 0;
            if (result.next()) {
                student_paid_2_count += result.getInt("Students Paid");
            }
            get_students_paid_2.close();

            // get the total number of students
            PreparedStatement get_students_total = connect.prepareStatement(students_total_query);
            result = get_students_total.executeQuery();
            int student_total_count = 0;
            if (result.next()) {
                student_total_count += result.getInt("Students Total");
            }
            get_students_total.close();

            // set the textfields with the fetched values
            paid_students1.setText(student_paid_1_count + " out of " + student_total_count);
            money_collected1.setText(
                    "Php " + student_paid_1_count * contribution_data_table.getItems().get(0).getContribution_amount());
            paid_students2.setText(student_paid_2_count + " out of " + student_total_count);
            money_collected2.setText(
                    "Php " + student_paid_2_count * contribution_data_table.getItems().get(1).getContribution_amount());
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
     * Get the contribution details of each organization during the current A.Y.
     *
     * @return the list of contributions for the specific class.
     */
    private ObservableList<ContributionProperties> getContributions() {
        ObservableList<ContributionProperties> contributions_list = FXCollections.observableArrayList();
        try {
            String contribution_data_query = "SELECT `contribution_code`, `semester`,`amount` "
                    + "FROM `contributions` "
                    + "WHERE `academic_year` = '" + academic_year + "' "
                    + "AND `collecting_org_code` = '" + org_code + "' "
                    + "ORDER BY `contribution_code` ASC;";
            PreparedStatement get_contributions_data = connect.prepareStatement(contribution_data_query);
            ResultSet result = get_contributions_data.executeQuery();

            while (result.next()) {
                String contribution_code = result.getString("contribution_code");
                String semester = result.getString("semester");
                Integer amount = result.getInt("amount");
                contributions_list.add(new ContributionProperties(contribution_code, semester, amount));
            }
            get_contributions_data.close();
        } catch (SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }
        return contributions_list;
    }

    /**
     * Set up the contributions data needed for the frame.
     */
    private void setupContributionsData() {
        code_column.setCellValueFactory(new PropertyValueFactory<>("contribution_code"));
        sem_column.setCellValueFactory(new PropertyValueFactory<>("contribution_sem"));
        amount_column.setCellValueFactory(new PropertyValueFactory<>("contribution_amount"));
        code_column.setStyle("-fx-alignment: CENTER;");
        sem_column.setStyle("-fx-alignment: CENTER;");
        amount_column.setStyle("-fx-alignment: CENTER;");

        ObservableList<ContributionProperties> details_contribution_ec = getContributions();
        contribution_data_table.setItems(details_contribution_ec);
    }

    /**
     * Set up the necessary parameters for searching blocks.
     */
    private void setupSearchBlock() {
        // store the programs affiliated with the organization here
        ObservableList<String> block_list = FXCollections.observableArrayList();
        block_list.add("--Select Program--");
        try {
            String program_code_query = "SELECT DISTINCT s.`program_code` "
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

        // store the year levels here
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
            student_data_table.getItems().clear();
            search_id.clear();

            ObservableList<StudentPaymentInfo> member_list = getPayersList(null, program_code_combobox.getValue(),
                    year_level_combobox.getValue());

            setupStudentsData(member_list);
            getStatistics(program_code_combobox.getValue(), year_level_combobox.getValue());
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
     * Set up the data needed for the frame.
     */
    private void setupStudentsData(ObservableList<StudentPaymentInfo> details_students) {
        student_data_table.getItems().clear();

        id_column.setCellValueFactory(new PropertyValueFactory<>("id_number"));
        first_name_column.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        middle_name_column.setCellValueFactory(new PropertyValueFactory<>("middle_name"));
        last_name_column.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        suffix_name_column.setCellValueFactory(new PropertyValueFactory<>("suffix_name"));
        first_sem_column.setCellValueFactory(new PropertyValueFactory<>("first_sem_status"));
        second_sem_column.setCellValueFactory(new PropertyValueFactory<>("second_sem_status"));

        id_column.setStyle("-fx-alignment: CENTER;");
        first_name_column.setStyle("-fx-alignment: CENTER;");
        middle_name_column.setStyle("-fx-alignment: CENTER;");
        last_name_column.setStyle("-fx-alignment: CENTER;");
        first_sem_column.setStyle("-fx-alignment: CENTER;");
        second_sem_column.setStyle("-fx-alignment: CENTER;");

        student_data_table.setItems(details_students);
    }

    /**
     * Fetches the data of the students that needs to pay the contributions.
     *
     * @param id_number_search
     * @param program_code_search
     * @param year_level_search
     * @return the list of students/members of the organization
     */
    private ObservableList<StudentPaymentInfo> getPayersList(String id_number_search, String program_code_search,
            String year_level_search) {
        String set_id;
        String set_program_code;
        String set_year_level;
        if (id_number_search == null && program_code_search == null && year_level_search == null) {
            // get the list of every member in the organization
            set_id = "";
            set_program_code = "";
            set_year_level = "";
        } else if (program_code_search == null && year_level_search == null) {
            // get the list of every student with matching id number
            set_id = "AND s.`id_number` LIKE '%" + id_number_search + "%' ";
            set_program_code = "";
            set_year_level = "";
        } else if (id_number_search == null) {
            // get the list of every student in the searched block
            set_id = "";
            set_program_code = "AND s.`program_code` = '" + program_code_search + "' ";
            set_year_level = "AND s.`year_level` = '" + year_level_search + "' ";
        } else {
            return null;
        }

        // store the data here
        ObservableList<StudentPaymentInfo> member_list = FXCollections.observableArrayList();
        String members_data_query = "SELECT `id_number`, `first_name`,`middle_name`, `last_name`, `suffix_name`, `year_level` "
                + "FROM `members` AS m LEFT JOIN `students` AS s ON m.`member_id` = s.`id_number` "
                + "WHERE m.`organization_code` = '" + org_code + "' "
                + set_id
                + set_program_code
                + set_year_level
                + "ORDER BY `last_name` ASC, `first_name` ASC, `middle_name` ASC, `program_code` ASC, `year_level` ASC;";

        try {
            PreparedStatement get_members_data = connect.prepareStatement(members_data_query);
            ResultSet result = get_members_data.executeQuery();
            while (result.next()) {
                String id_number = result.getString("id_number");
                String first_name = result.getString("first_name");
                String middle_name = result.getString("middle_name");
                String last_name = result.getString("last_name");
                String suffix_name = result.getString("suffix_name");
                String year_level = result.getString("year_level");

                String first_sem_status = getPayerStatus(contribution_data_table.getItems().get(0), id_number);
                String second_sem_status = getPayerStatus(contribution_data_table.getItems().get(1), id_number);

                member_list.add(new StudentPaymentInfo(id_number, first_name, middle_name, last_name, suffix_name,
                        year_level, first_sem_status, second_sem_status));
            }
            get_members_data.close();
        } catch (SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }
        return member_list;
    }

    /**
     * Fetch the payment status of the student.
     *
     * @param contribution
     * @param id_number
     * @return status of the student
     * @throws SQLException
     */
    private String getPayerStatus(ContributionProperties contribution, String id_number) {
        /*
         * Set the initial status to Not Paid in case there are no data to be fetched
         * since there was no transaction made still
         */
        String status = "Not Paid";
        if (contribution.getContribution_amount() <= 0) {
            status = "Paid"; // if there is no contribution to be paid
        } else {
            try {
                // get their status for first sem
                String payment_status_query = "SELECT `status` "
                        + "FROM `pays` "
                        + "WHERE `payer_id` = '" + id_number + "' "
                        + "AND `contribution_code` = '" + contribution.getContribution_code() + "' "
                        + "ORDER BY `transaction_id` DESC;";
                PreparedStatement get_payment_status = connect.prepareStatement(payment_status_query);
                ResultSet result = get_payment_status.executeQuery();
                if (result.next()) {
                    status = switch (result.getString("status")) {
                        case "Accepted" -> "Paid";
                        default -> result.getString("status");
                    };
                }
                result.close();
            } catch (SQLException e) {
                Alert connection_error = new Alert(Alert.AlertType.ERROR);
                connection_error.setTitle("Database Connection Error");
                connection_error.setHeaderText("Check your connection.");
                connection_error.setContentText(e.toString());
                connection_error.showAndWait();
                System.exit(0);
            }
        }
        return status;
    }

    /**
     * Facilitates the payment form for the first sem.
     *
     * @throws IOException
     */
    @FXML
    private void menu_first_sem() throws IOException {
        // get the selected payer
        StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
        if (payer != null) {
            if (payer.getFirst_sem_status().equals("Paid")) {
                // if the payer already paid
                Alert non_selected = new Alert(Alert.AlertType.INFORMATION);
                non_selected.setTitle("Student Already Paid");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Student Already Paid. Select another student.");
                non_selected.showAndWait();
            } else if (payer.getFirst_sem_status().equals("Pending")) {
                // if the recent payment is still pending
                Alert non_selected = new Alert(Alert.AlertType.ERROR);
                non_selected.setTitle("Multiple Transaction Error");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Previous payment is still unverified, try again after verification.");
                non_selected.showAndWait();
            } else {
                menuTransactionSet(contribution_data_table.getItems().get(0).getContribution_code(), payer);
            }
        } else {
            Alert non_selected = new Alert(Alert.AlertType.ERROR);
            non_selected.setTitle("No Student Selected");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Please Select a Student.");
            non_selected.showAndWait();
        }
    }

    /**
     * Facilitates the review of the payment for the second sem.
     *
     * @throws IOException
     */
    @FXML
    void view_first_sem() throws IOException {
        viewSet(contribution_data_table.getItems().get(0));
    }

    /**
     * Facilitates the payment form for the second sem.
     *
     * @throws IOException
     */
    @FXML
    private void menu_second_sem() throws IOException {
        // get the selected payer
        StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
        if (payer != null) {
            if (payer.getSecond_sem_status().equals("Paid")) {
                // if the payer already paid
                Alert non_selected = new Alert(Alert.AlertType.INFORMATION);
                non_selected.setTitle("Student Already Paid");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Student Already Paid. Select another student.");
                non_selected.showAndWait();
            } else if (payer.getSecond_sem_status().equals("Pending")) {
                // if the recent payment is still pending
                Alert non_selected = new Alert(Alert.AlertType.ERROR);
                non_selected.setTitle("Multiple Transaction Error");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Previous payment is still unverified, try again after verification.");
                non_selected.showAndWait();
            } else {
                menuTransactionSet(contribution_data_table.getItems().get(1).getContribution_code(), payer);
            }
        } else {
            Alert non_selected = new Alert(Alert.AlertType.ERROR);
            non_selected.setTitle("No Student Selected");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Please Select a Student.");
            non_selected.showAndWait();
        }
    }

    /**
     * Facilitates the review of the payment for the second sem.
     *
     * @throws IOException
     */
    @FXML
    void view_second_sem() throws IOException {
        viewSet(contribution_data_table.getItems().get(1));
    }

    /**
     * Facilitates the transaction for both sem
     *
     * @param contribution_code
     * @throws IOException
     */
    private void menuTransactionSet(String contribution_code, StudentPaymentInfo payer) throws IOException {
        Stage transaction_stage = new Stage();
        transaction_stage.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/transaction-form.fxml"));
        Parent transaction_parent = fxmlLoader.load();
        TransactionProcess transaction_process = fxmlLoader.getController();
        transaction_process.setPayer(payer);
        transaction_process.initialize(contribution_code);

        Scene transaction_scene = new Scene(transaction_parent);
        transaction_stage.setTitle("Contribution Payment Transaction");
        transaction_stage.setScene(transaction_scene);
        transaction_stage.setResizable(false);
        transaction_stage.show();
    }

    /**
     * Facilitates the review of the transaction for both sem.
     *
     * @param contribution
     * @throws IOException
     */
    private void viewSet(ContributionProperties contribution) throws IOException {
        if (contribution.getContribution_amount() > 0) {
            StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
            Stage transaction_stage = new Stage();
            transaction_stage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/review-transaction-form.fxml"));
            Parent transaction_parent = fxmlLoader.load();
            ReviewTransactionControl transaction_process = fxmlLoader.getController();
            transaction_process.initialize(payer.getId_number(), contribution.getContribution_code());

            Scene transaction_scene = new Scene(transaction_parent);
            transaction_stage.setTitle("View Transaction");
            transaction_stage.setScene(transaction_scene);
            transaction_stage.setResizable(false);
            transaction_stage.show();
        } else {
            Alert non_selected = new Alert(Alert.AlertType.INFORMATION);
            non_selected.setTitle("Contribution Not Payable");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Contribution is not being collected. Amount = 0.");
            non_selected.showAndWait();
        }
    }

    /**
     * Facilitates the searching of a student based on id_number.
     */
    @FXML
    private void searchID() {
        student_data_table.getItems().clear();
        program_code_combobox.getSelectionModel().selectFirst();
        year_level_combobox.getSelectionModel().selectFirst();

        ObservableList<StudentPaymentInfo> member_list = getPayersList(search_id.getText(), null, null);
        setupStudentsData(member_list);

        getStatistics(null, null);
    }

    /**
     * Refreshes the display.
     */
    @FXML
    private void resetSearch() {
        student_data_table.getItems().clear();
        search_id.clear();
        program_code_combobox.getSelectionModel().selectFirst();
        year_level_combobox.getSelectionModel().selectFirst();
        setupContributionsData();

        ObservableList<StudentPaymentInfo> details_students = getPayersList(null, null, null);
        student_data_table.setItems(details_students);

        getStatistics(null, null);
    }

    /**
     * Disables menus when certain conditions aren't meet.
     */
    @FXML
    private void setupContextMenu() {
        StudentPaymentInfo student_selected = student_data_table.getSelectionModel().getSelectedItem();
        boolean first_sem = (student_selected == null) || (student_selected.getFirst_sem_status().equals("Not Paid"));
        boolean second_sem = (student_selected == null) || (student_selected.getSecond_sem_status().equals("Not Paid"));

        show_first_sem_transaction.setDisable(first_sem);
        show_second_sem_transaction.setDisable(second_sem);
    }
}