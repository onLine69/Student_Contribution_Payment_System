package control;

import model.DataManager;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
 * Facilitates the process of the BUFICOM frame.
 */
public class BuficomInfoControl {
    private static Connection connect;
    @FXML
    private TextField user_position_textfield;
    @FXML
    private TextField org_code_textfield;
    @FXML
    private TextField org_name_textfield;
    @FXML
    private ImageView org_image;
    @FXML
    private Button verify_payments_button;
    @FXML
    private Button transaction_history_button;
    @FXML
    private Button students_records_button;
    private String user_id_number;
    private HBox parent;

    public BuficomInfoControl() {
    }

    /**
     * Set the initial display. Along with the information of the BUFICOM user.
     *
     * @param parent
     * @param user_id_number
     * @param user_position
     */
    public void initialize(HBox parent, String user_id_number, String user_position) {
        connect = DataManager.getConnect();

        // get the parent scene for later transition based on the selected button
        this.parent = parent;

        // just use this default photo because we don't have enough time to implement
        // changing of profile. Bitaw kapoy na :<<
        File file = new File("src/main/resources/Image/profile (1).png");
        Image image = new Image(file.toURI().toString());
        org_image.setImage(image);

        this.user_id_number = user_id_number;
        user_position_textfield.setText(user_position);
        user_position_textfield.setTooltip(new Tooltip(this.user_id_number));
        getUserOrgInfo();
    }

    /**
     * Get the organization code of the user.
     *
     * @return organization_code
     */
    public String getOrg_code() {
        return org_code_textfield.getText();
    }

    /**
     * Change the appearance of the buttons based on the clicked button.
     *
     * @param clicked
     */
    private void currentlyDisplayed(Button clicked) {
        verify_payments_button.setDisable(verify_payments_button.equals(clicked));
        verify_payments_button.setUnderline(verify_payments_button.equals(clicked));
        transaction_history_button.setDisable(transaction_history_button.equals(clicked));
        transaction_history_button.setUnderline(transaction_history_button.equals(clicked));
        students_records_button.setDisable(students_records_button.equals(clicked));
        students_records_button.setUnderline(students_records_button.equals(clicked));
    }

    /**
     * Facilitates the changing of the dashboard to Verify Payments Dashboard
     */
    @FXML
    private void verify_payments_clicked() {
        currentlyDisplayed(verify_payments_button);

        // remove the display on the right side
        ObservableList<Node> children = parent.getChildren();
        parent.getChildren().remove(children.get(1));
        try {
            // load and add the scene to the parent to be displayed
            FXMLLoader dashboard_loader = new FXMLLoader(
                    getClass().getResource("/view/BUFICOM-FRAMES/verify-payments.fxml"));
            AnchorPane dashboard = dashboard_loader.load();
            VerifyPaymentsControl dashboard_control = dashboard_loader.getController();
            dashboard_control.initialize(org_code_textfield.getText());

            parent.getChildren().add(dashboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Facilitates the changing of the dashboard to Transaction History Dashboard
     */
    @FXML
    private void transaction_history_clicked() {
        currentlyDisplayed(transaction_history_button);

        // remove the display on the right side
        ObservableList<Node> children = parent.getChildren();
        parent.getChildren().remove(children.get(1));
        try {
            // load and add the scene to the parent to be displayed
            FXMLLoader dashboard_loader = new FXMLLoader(
                    getClass().getResource("/view/BUFICOM-FRAMES/transaction-history.fxml"));
            AnchorPane dashboard = dashboard_loader.load();
            TransactionHistoryControl dashboard_control = dashboard_loader.getController();
            dashboard_control.initialize(org_code_textfield.getText());

            parent.getChildren().add(dashboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Facilitates the changing of the dashboard to Students' Records Dashboard
     */
    @FXML
    private void students_records_clicked() {
        currentlyDisplayed(students_records_button);

        // remove the display on the right side
        ObservableList<Node> children = parent.getChildren();
        parent.getChildren().remove(children.get(1));
        try {
            // load and add the scene to the parent to be displayed
            FXMLLoader dashboard_loader = new FXMLLoader(
                    getClass().getResource("/view/BUFICOM-FRAMES/students-records.fxml"));
            AnchorPane dashboard = dashboard_loader.load();
            StudentsRecordsControl dashboard_control = dashboard_loader.getController();
            dashboard_control.initialize(org_code_textfield.getText());

            parent.getChildren().add(dashboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Display and facilitates the Add Contribution form.
     */
    @FXML
    private void edit_contribution_button_clicked() {
        try {
            // create and set up the stage
            Stage edit_contribution_stage = new Stage();
            edit_contribution_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
            edit_contribution_stage.setResizable(false);
            edit_contribution_stage.setTitle("Edit Contributions");
            edit_contribution_stage.initModality(Modality.APPLICATION_MODAL);

            // load the display
            FXMLLoader edit_contribution_loader = new FXMLLoader(
                    getClass().getResource("/view/BUFICOM-FRAMES/edit-contribution-form.fxml"));
            Parent edit_contribution_parent = edit_contribution_loader.load();
            EditContributionControl edit_contribution_control = edit_contribution_loader.getController();
            edit_contribution_control.initialize(org_code_textfield.getText());

            // create the scene
            Scene edit_contribution_scene = new Scene(edit_contribution_parent);
            edit_contribution_stage.setScene(edit_contribution_scene);
            edit_contribution_stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the organization code of the user.
     */
    private void getUserOrgInfo() {
        try {
            String org_code_query = "SELECT m.`organization_code`, o.`organization_name` "
                    + "FROM `manages` AS m LEFT JOIN `organizations` AS o "
                    + "ON m.`organization_code` = o.`organization_code` "
                    + "WHERE `officer_id` =  '" + user_id_number + "';";
            PreparedStatement get_org_code = connect.prepareStatement(org_code_query);
            ResultSet result = get_org_code.executeQuery();
            if (result.next()) {
                org_code_textfield.setText(result.getString("organization_code"));
                org_name_textfield.setText(result.getString("organization_name"));
            }
            get_org_code.close();
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
     * Display again the login stage when the user logs out.
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