package control;

import model.ContributionProperties;
import model.DataManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Since at the beginning of the A.Y., societies aren't collecting
 * contributions.
 * If the societies decided to collect contributions, use this to edit the
 * collecting contribution.
 * Just be careful on changing as this will not record the previous set amounts
 * and the paid contributions before the change will not be recorded.
 */
public class EditContributionControl {
    private static Connection connect;
    @FXML
    private TextField organization_code_textfield;
    @FXML
    private TextField academic_year_textfield;
    @FXML
    private ComboBox<String> semester_combobox;
    @FXML
    private TextField amount_textfield;
    @FXML
    private TableView<ContributionProperties> contribution_data_table;
    @FXML
    private TableColumn<ContributionProperties, String> code_column;
    @FXML
    private TableColumn<ContributionProperties, String> academic_year_column;
    @FXML
    private TableColumn<ContributionProperties, String> sem_column;
    @FXML
    private TableColumn<ContributionProperties, Integer> amount_column;
    @FXML
    private Button edit_contribution_button;
    private String org_code;
    private String academic_year;

    public EditContributionControl() {
    }

    /**
     * Set up the initial display and fetch the necessary data for the
     * contributions.
     *
     * @param org_code
     */
    public void initialize(String org_code) {
        connect = DataManager.getConnect();
        this.org_code = org_code;
        academic_year = DataManager.getAcademic_year();

        organization_code_textfield.setText(this.org_code);
        academic_year_textfield.setText(academic_year);
        semester_combobox.getSelectionModel().selectFirst();

        ObservableList<ContributionProperties> contribution_list = FXCollections.observableArrayList();
        try {
            String contribution_info_query = "SELECT `contribution_code`, `semester`, `amount` "
                    + "FROM `contributions` "
                    + "WHERE `collecting_org_code` = '" + this.org_code + "' "
                    + "AND `academic_year` = '" + academic_year + "';";
            PreparedStatement get_contribution_info = connect.prepareStatement(contribution_info_query);
            ResultSet result = get_contribution_info.executeQuery();
            while (result.next()) {
                String contribution_code = result.getString("contribution_code");
                String semester = result.getString("semester");
                Integer amount = result.getInt("amount");

                contribution_list.add(new ContributionProperties(contribution_code, academic_year, semester, amount));
            }
            result.close();
            setupContributionData(contribution_list);
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
     * Set up and display the contributions collected by the organization in this
     * academic year.
     * 
     * @param data
     */
    private void setupContributionData(ObservableList<ContributionProperties> data) {
        code_column.setCellValueFactory(new PropertyValueFactory<>("contribution_code"));
        academic_year_column.setCellValueFactory(new PropertyValueFactory<>("academic_year"));
        sem_column.setCellValueFactory(new PropertyValueFactory<>("contribution_sem"));
        amount_column.setCellValueFactory(new PropertyValueFactory<>("contribution_amount"));
        code_column.setStyle("-fx-alignment: CENTER;");
        academic_year_column.setStyle("-fx-alignment: CENTER;");
        sem_column.setStyle("-fx-alignment: CENTER;");
        amount_column.setStyle("-fx-alignment: CENTER;");

        contribution_data_table.setOnMouseClicked(event -> {
            // Make sure the user clicked on a populated item
            ContributionProperties contribution = contribution_data_table.getSelectionModel().getSelectedItem();
            if (contribution != null) {
                organization_code_textfield.setText(contribution.getContribution_code().split("_")[0]);
                academic_year_textfield.setText(contribution.getAcademic_year());
                semester_combobox.getItems().clear();
                semester_combobox.getItems().add(contribution.getContribution_sem());
                semester_combobox.getSelectionModel().selectFirst();
                amount_textfield.setText(String.valueOf(contribution.getContribution_amount()));
                edit_contribution_button.setDisable(false);
            }
        });
        contribution_data_table.setItems(data);
    }

    /**
     * Clear the form.
     */
    @FXML
    private void clear_selection_button_clicked() {
        amount_textfield.clear();
        edit_contribution_button.setDisable(true);
        semester_combobox.getItems().clear();
        contribution_data_table.getSelectionModel().clearSelection();
    }

    /**
     * Edit the values in the database.
     */
    @FXML
    private void edit_contribution_button_clicked() {
        if (!amount_textfield.getText().isEmpty()) {
            // only change if the user verifies
            Alert edit_confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            edit_confirmation.setTitle("Edit Confirmation");
            edit_confirmation.setHeaderText(
                    "This will change the contribution amount. Any payments accepted before this change will not be affected.");
            edit_confirmation.setContentText("Are you sure?");
            Optional<ButtonType> confirm = edit_confirmation.showAndWait();
            confirm.ifPresent(choice -> {
                if (confirm.get() == ButtonType.OK) {
                    try {
                        String edit_contribution_query = "UPDATE `contributions` "
                                + "SET `semester` = ?, `amount` = ? "
                                + "WHERE `contribution_code` = ?;";
                        PreparedStatement edit_contribution = connect.prepareStatement(edit_contribution_query);
                        edit_contribution.setString(1, semester_combobox.getValue());
                        edit_contribution.setInt(2, Integer.parseInt(amount_textfield.getText()));
                        edit_contribution.setString(3,
                                contribution_data_table.getSelectionModel().getSelectedItem().getContribution_code());
                        edit_contribution.executeUpdate();
                    } catch (SQLException e) {
                        Alert connection_error = new Alert(Alert.AlertType.ERROR);
                        connection_error.setTitle("Database Connection Error");
                        connection_error.setHeaderText("Check your connection.");
                        connection_error.setContentText(e.toString());
                        connection_error.showAndWait();
                        System.exit(0);
                    }
                    initialize(this.org_code);
                    clear_selection_button_clicked();
                }
            });
        } else {
            Border error_border = new Border(
                    new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
            amount_textfield.setBorder(error_border);
        }
    }
}
