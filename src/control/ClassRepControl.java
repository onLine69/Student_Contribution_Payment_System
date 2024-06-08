package control;

import model.ContributionProperties;
import model.DataManager;
import model.StudentPaymentInfo;
import model.UserData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Facilitates the process of the Class Rep frame.
 */
public class ClassRepControl {
    private static Connection connect;
    @FXML
    private TextField user_position;
    @FXML
    private ImageView user_image;
    @FXML
    private TextField user_name;
    @FXML
    private TextField org_code;
    @FXML
    private TextField block_code;
    @FXML
    private TextField paid_students1;
    @FXML
    private TextField paid_students2;
    @FXML
    private TextField money_collected1;
    @FXML
    private TextField money_collected2;
    @FXML
    private TextField contribution_info;
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
    private Button ec_button;
    @FXML
    private Button society_button;
    @FXML
    private TextField search_id;
    @FXML
    private Button search_button;
    @FXML
    private MenuItem show_first_sem_transaction;
    @FXML
    private MenuItem show_second_sem_transaction;
    private String academic_year;
    private UserData user;

    public ClassRepControl() {
    }

    /**
     * Set the initial display. Along with the information of the class rep user.
     *
     * @param user_id_number
     */
    public void initialize(String user_id_number) {
        connect = DataManager.getConnect();

        // get the current academic year
        academic_year = DataManager.getAcademic_year();

        // get the user details
        getUserData(user_id_number);

        // set up the user data display based on the fetched information
        setupUser();

        // display the society first
        setupData(user.getSociety_code());

        // search id setup
        searchSetup();
    }

    /**
     * Display the information of the user
     */
    private void setupUser() {
        /*
         * Just use this default photo because we don't have enough time to implement
         * changing of profile.
         * Bitaw kapoy na :<<
         */
        File file = new File("src/main/resources/Image/profile (1).png");
        Image image = new Image(file.toURI().toString());
        user_image.setImage(image);

        // set up the user details to be shown in the frame
        user_position.setText(user.getPosition());
        String user_full_name = user.getFirst_name() + " " + user.getMiddle_name() + " " + user.getLast_name() + " "
                + user.getSuffix_name();
        user_name.setText(user_full_name);
        user_name.setTooltip(new Tooltip(user.getId_number()));
        block_code.setText(user.getProgram_code() + "-" + user.getYear_level().charAt(0));
    }

    /**
     * Enable the search button only when an input is provided in the textfield.
     */
    private void searchSetup() {
        search_id.textProperty().addListener((ov, t, textField) -> {
            if (textField.isEmpty()) {
                search_button.setDisable(true);
                resetSearch();
            } else {
                search_button.setDisable(false);
            }
        });
    }

    /**
     * Fetch and display the statistics of the students.
     */
    private void getStatistics() {
        try {
            // prepare the queries
            String students_paid_1_query = "SELECT COUNT(p.`status`) AS `Students Paid` "
                    + "FROM `pays` AS p LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                    + "WHERE p.`contribution_code` = '"
                    + contribution_data_table.getItems().get(0).getContribution_code() + "' "
                    + "AND s.`program_code` = '" + user.getProgram_code() + "' "
                    + "AND s.`year_level` = '" + user.getYear_level() + "' "
                    + "AND p.`status` = 'Accepted';";
            String students_paid_2_query = "SELECT COUNT(p.`status`) AS `Students Paid` "
                    + "FROM `pays` AS p LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                    + "WHERE p.`contribution_code` = '"
                    + contribution_data_table.getItems().get(1).getContribution_code() + "' "
                    + "AND s.`program_code` = '" + user.getProgram_code() + "' "
                    + "AND s.`year_level` = '" + user.getYear_level() + "' "
                    + "AND p.`status` = 'Accepted';";
            String students_total_query = "SELECT COUNT(`id_number`) AS `Students Total` "
                    + "FROM `students` "
                    + "WHERE `program_code` = '" + user.getProgram_code() + "' "
                    + "AND `year_level` = '" + user.getYear_level() + "';";

            // get the number of students in the block that already paid first sem
            PreparedStatement get_students_paid_1 = connect.prepareStatement(students_paid_1_query);
            ResultSet result = get_students_paid_1.executeQuery();
            int student_paid_1_count = 0;
            if (result.next()) {
                student_paid_1_count += result.getInt("Students Paid");
            }
            get_students_paid_1.close();

            // get the number of students in the block that already paid second sem
            PreparedStatement get_students_paid_2 = connect.prepareStatement(students_paid_2_query);
            result = get_students_paid_2.executeQuery();
            int student_paid_2_count = 0;
            if (result.next()) {
                student_paid_2_count += result.getInt("Students Paid");
            }
            get_students_paid_2.close();

            // get the count of students in the same block as the user
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
     * Get the information about the user from the database.
     *
     * @param user_id_number
     */
    private void getUserData(String user_id_number) {
        try {
            // set up the query
            String user_data_query = "SELECT `first_name`, `middle_name`, `last_name`, `suffix_name`, `user_id`, `year_level`, `program_code`, `position`, `organization_code` "
                    + "FROM `users` AS u LEFT JOIN `students` AS s ON u.`user_id` = s.`id_number` "
                    + "LEFT JOIN `manages` AS o ON u.`user_id` = o.`officer_id` "
                    + "WHERE `user_id` = '" + user_id_number + "';";

            // prepare then execute the query
            PreparedStatement setup_user_data = connect.prepareStatement(user_data_query);
            ResultSet result = setup_user_data.executeQuery();

            // get the first line of the result
            result.next();
            String first_name_data = result.getString("first_name");
            String middle_name_data = result.getString("middle_name");
            String last_name_data = result.getString("last_name");
            String suffix_name_data = result.getString("suffix_name");
            String user_id_data = result.getString("user_id");
            String year_level_data = result.getString("year_level");
            String program_code_data = result.getString("program_code");
            String position_data = result.getString("position");
            String ec_org_code = result.getString("organization_code");
            /*
             * Since a classroom rep can collect up to 2 contribution from 2 different
             * organizations, get the second one also.
             */
            result.next();
            String society_org_code = result.getString("organization_code");

            // in case the second row has the ec org_code
            if (society_org_code.endsWith("-EC")) {
                // swap the two strings
                ec_org_code = ec_org_code + society_org_code;
                society_org_code = ec_org_code.substring(0, (ec_org_code.length() - society_org_code.length()));
                ec_org_code = ec_org_code.substring(society_org_code.length());
            }

            user = new UserData(first_name_data, middle_name_data, last_name_data, suffix_name_data, user_id_data,
                    year_level_data, program_code_data, position_data, ec_org_code, society_org_code);

            setup_user_data.close();
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
        // fetch the data from the database then store here
        ObservableList<ContributionProperties> contributions_list = FXCollections.observableArrayList();
        try {
            String user_data_query = "SELECT `contribution_code`, `semester`,`amount` "
                    + "FROM `contributions` "
                    + "WHERE `academic_year` = '" + academic_year + "' "
                    + "AND `collecting_org_code` = '" + org_code.getText() + "' "
                    + "ORDER BY `contribution_code` ASC;";
            PreparedStatement get_contributions_data = connect.prepareStatement(user_data_query);
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
     * Set up the data needed for the frame.
     *
     * @param org_code
     */
    private void setupData(String org_code) {
        // change the contribution info based on the org code
        if (org_code.endsWith("-EC")) {
            contribution_info.setText("EC Contribution");
        } else {
            contribution_info.setText("Society Contribution");
        }
        this.org_code.setText(org_code);

        setupContributionsData();
        setupStudentsData();
        getStatistics();
    }

    /**
     * Set up the contribution data.
     */
    private void setupContributionsData() {
        contribution_data_table.getItems().clear();

        // set up then display the data to the contribution table
        code_column.setCellValueFactory(new PropertyValueFactory<>("contribution_code"));
        sem_column.setCellValueFactory(new PropertyValueFactory<>("contribution_sem"));
        amount_column.setCellValueFactory(new PropertyValueFactory<>("contribution_amount"));
        code_column.setStyle("-fx-alignment: CENTER;");
        sem_column.setStyle("-fx-alignment: CENTER;");
        amount_column.setStyle("-fx-alignment: CENTER;");

        ObservableList<ContributionProperties> contributions_data = getContributions();
        contribution_data_table.setItems(contributions_data);
    }

    /**
     * Set up the student data.
     */
    private void setupStudentsData() {
        student_data_table.getItems().clear();

        // set up then display the data to the students table
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

        ObservableList<StudentPaymentInfo> details_students = getPayersList(null);
        student_data_table.setItems(details_students);
    }

    /**
     * Fetches the data of the students that needs to pay the contributions.
     *
     * @return the list of students/members of the organization
     */
    private ObservableList<StudentPaymentInfo> getPayersList(String id_number_search) {
        // fetch the students' data then store here
        ObservableList<StudentPaymentInfo> member_list = FXCollections.observableArrayList();

        String set_id;
        if (id_number_search == null) // get the list of every member in the organization since there is no id
                                      // specified
            set_id = "";
        else // get the list of the students with similar pattern of the searched ID
            set_id = "AND s.`id_number` LIKE '%" + id_number_search + "%' ";

        String members_data_query = "SELECT `id_number`, `first_name`,`middle_name`, `last_name`, `suffix_name`, `year_level` "
                + "FROM `members` AS m LEFT JOIN `students` AS s ON m.`member_id` = s.`id_number` "
                + "WHERE m.`organization_code` = '" + org_code.getText() + "' "
                + "AND s.`program_code` = '" + user.getProgram_code() + "' "
                + "AND s.`year_level` = '" + user.getYear_level() + "' "
                + set_id
                + "ORDER BY `last_name` ASC, `first_name` ASC, `middle_name` ASC;";

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
            throw new RuntimeException(e);
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
     * Display the data for ec contributions.
     */
    @FXML
    private void ec_button_clicked() {
        currentlyDisplayed(ec_button);
        resetSearch();
        setupData(user.getEc_code());
    }

    /**
     * Display the data for society contributions.
     */
    @FXML
    private void society_button_clicked() {
        currentlyDisplayed(society_button);
        resetSearch();
        setupData(user.getSociety_code());
    }

    /**
     * Change the appearance of the buttons based on the clicked button.
     *
     * @param clicked
     */
    private void currentlyDisplayed(Button clicked) {
        ec_button.setDisable(ec_button.equals(clicked));
        ec_button.setUnderline(ec_button.equals(clicked));
        society_button.setDisable(society_button.equals(clicked));
        society_button.setUnderline(society_button.equals(clicked));
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
        ObservableList<StudentPaymentInfo> member_list = getPayersList(search_id.getText());
        student_data_table.setItems(member_list);
    }

    /**
     * Refreshes the display.
     */
    @FXML
    private void resetSearch() {
        student_data_table.getItems().clear();
        search_id.clear();
        ObservableList<StudentPaymentInfo> details_students = getPayersList(null);
        student_data_table.setItems(details_students);
    }

    /**
     * Disables menus when certain conditions aren't meet.
     */
    @FXML
    private void setupContextMenu() {
        StudentPaymentInfo student_selected = student_data_table.getSelectionModel().getSelectedItem();
        boolean disable_first_menu = (student_selected == null)
                || (student_selected.getFirst_sem_status().equals("Not Paid"));
        boolean disable_second_menu = (student_selected == null)
                || (student_selected.getSecond_sem_status().equals("Not Paid"));
        show_first_sem_transaction.setDisable(disable_first_menu);
        show_second_sem_transaction.setDisable(disable_second_menu);
    }

    /**
     * Log out process.
     *
     * @param event
     */
    @FXML
    private void logout_button_clicked(ActionEvent event) {
        Alert logout_confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        logout_confirmation.setTitle("Logout Confirmation");
        logout_confirmation.setHeaderText("Logging out");
        logout_confirmation.setContentText("Are you sure?");
        Optional<ButtonType> decision = logout_confirmation.showAndWait();
        decision.ifPresent(choice -> {
            if (decision.get() == ButtonType.OK) {
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
                try {
                    Stage login_stage = new Stage();
                    login_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));

                    // starts with the login scene
                    FXMLLoader login_view = new FXMLLoader(getClass().getResource("/view/login-frame.fxml"));
                    Scene login_scene = new Scene(login_view.load());
                    login_stage.setTitle("Login to SCPS");
                    login_stage.setScene(login_scene);
                    login_stage.setResizable(false);
                    login_stage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}