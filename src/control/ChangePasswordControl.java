package control;

import model.DataManager;
import model.Security;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Facilitates the changing of password.
 */
public class ChangePasswordControl {
    @FXML
    private PasswordField new_password;
    @FXML
    private PasswordField retype_password;
    @FXML
    private TextField new_password_show;
    @FXML
    private TextField retype_password_show;
    @FXML
    private RadioButton show_password_rbutton;
    @FXML
    private RadioButton show_retype_password_rbutton;
    private static Connection connect;
    private String user_id;

    /**
     * Fetch and set up the necessary data.
     *
     * @param user_id
     */
    public void initialize(String user_id) {
        connect = DataManager.getConnect();
        this.user_id = user_id;
    }

    /**
     * To properly get the data from the new password field.
     *
     * @return password
     */
    private String newPasswordValue() {
        return show_password_rbutton.isSelected() ? new_password_show.getText() : new_password.getText();
    }

    /**
     * To properly get the data from the retype password field.
     *
     * @return password
     */
    private String retypePasswordValue() {
        return show_password_rbutton.isSelected() ? retype_password_show.getText() : retype_password.getText();
    }

    /**
     * Check if the new password is valid, if valid change the stored password in
     * the database, else show error message.
     *
     * @param event
     */
    @FXML
    private void verify_new_password(ActionEvent event) {
        if (newPasswordValue().length() >= 8 && newPasswordValue().equals(retypePasswordValue())
                && !newPasswordValue().equals(user_id)) {
            try {
                String hashedPassword = Security.hashPassword(newPasswordValue());
                String change_password_query = "UPDATE `users` SET `password` = ? WHERE `user_id` = ?;";
                PreparedStatement change_password = connect.prepareStatement(change_password_query);
                change_password.setString(1, hashedPassword);
                change_password.setString(2, user_id);
                change_password.executeUpdate();
                change_password.close();

                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
                Alert password_changed = new Alert(Alert.AlertType.CONFIRMATION, "You may now proceed to use the app.",
                        yes, no);
                password_changed.setTitle("Password Change Successful");
                password_changed.setHeaderText("Proceed to login?");
                Optional<ButtonType> buttons = password_changed.showAndWait();
                buttons.ifPresent(res -> {
                    // close the login stage
                    ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
                    if (buttons.get() == yes) {
                        try {
                            Stage login_stage = new Stage();
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
                    } else {
                        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
                    }
                });
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

            if (newPasswordValue().length() < 8) {
                if (show_password_rbutton.isSelected())
                    new_password_show.setBorder(error_border);
                else
                    new_password.setBorder(error_border);

                Alert password_too_small = new Alert(Alert.AlertType.ERROR);
                password_too_small.setTitle("Insufficient Password Length");
                password_too_small.setHeaderText("Password too short.");
                password_too_small.setContentText("Please use password with length >= 8.");
                password_too_small.showAndWait();
            } else if (newPasswordValue().equals(user_id)) {
                if (show_password_rbutton.isSelected())
                    new_password_show.setBorder(error_border);
                else
                    new_password.setBorder(error_border);

                Alert pass_same_old = new Alert(Alert.AlertType.ERROR);
                pass_same_old.setTitle("Password is the old one.");
                pass_same_old.setHeaderText("Password must not be the same with the old one.");
                pass_same_old.setContentText("Please make sure to change the password");
                pass_same_old.showAndWait();
            } else {
                if (show_retype_password_rbutton.isSelected())
                    retype_password_show.setBorder(error_border);
                else
                    retype_password.setBorder(error_border);

                Alert password_not_match = new Alert(Alert.AlertType.ERROR);
                password_not_match.setTitle("Password doesn't match.");
                password_not_match.setHeaderText("Check retype password.");
                password_not_match.setContentText("Please make sure the new and retyped password matches.");
                password_not_match.showAndWait();
            }
        }
    }

    /**
     * Show new password functionality.
     */
    @FXML
    private void show_password_rbutton_clicked() {
        new_password.setVisible(!show_password_rbutton.isSelected());
        new_password_show.setVisible(show_password_rbutton.isSelected());

        if (show_password_rbutton.isSelected()) {
            new_password_show.setText(new_password.getText());
        } else {
            new_password.setText(new_password_show.getText());
        }
    }

    /**
     * Show retype password functionality.
     */
    @FXML
    private void show_retype_password_rbutton_clicked() {
        retype_password.setVisible(!show_retype_password_rbutton.isSelected());
        retype_password_show.setVisible(show_retype_password_rbutton.isSelected());

        if (show_retype_password_rbutton.isSelected()) {
            retype_password_show.setText(retype_password.getText());
        } else {
            retype_password.setText(retype_password_show.getText());
        }
    }

    /**
     * Provide random generated password.
     */
    @FXML
    private void random_password_button_clicked() {
        String random_pass = Security.randomPasswordGenerator();
        new_password.setText(random_pass);
        new_password_show.setText(random_pass);
    }
}