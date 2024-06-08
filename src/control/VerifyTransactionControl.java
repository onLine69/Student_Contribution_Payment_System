package control;

import model.DataManager;
import model.EmailSender;
import model.UnverifiedPayment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Facilitates the verification form.
 */
public class VerifyTransactionControl {
    private static Connection connect;
    @FXML
    private Label transaction_label;
    @FXML
    private TextField transaction_payer_id;
    @FXML
    private TextField transaction_payer_name;
    @FXML
    private TextField transaction_id;
    @FXML
    private TextField transaction_datetime;
    @FXML
    private TextField transaction_amount;
    @FXML
    private ComboBox<String> transaction_payment_mode;
    @FXML
    private Hyperlink receipt_link;
    @FXML
    private TextField transaction_status;
    @FXML
    private TextArea transaction_comments;
    private UnverifiedPayment payment;
    private Blob receipt_image;

    public VerifyTransactionControl() {
    }

    /**
     * Initialize the necessary data and connection for the transaction.
     * 
     * @param payment
     */
    public void initialize(UnverifiedPayment payment) {
        connect = DataManager.getConnect();
        this.payment = payment;
        setupData();
    }

    /**
     * Set up the data of the transaction.
     */
    private void setupData() {
        try {
            String payment_info_query = "SELECT p.`contribution_code`, `transaction_datetime`, `amount`, `payment_mode`, `payer_receipt`, `transaction_message` "
                    +
                    "FROM `pays` AS p LEFT JOIN `contributions` AS c ON p.`contribution_code` = c.`contribution_code` "
                    +
                    "WHERE p.`transaction_id` =" + payment.getTransaction_id() + ";";
            PreparedStatement get_payment_info = connect.prepareStatement(payment_info_query);
            ResultSet result = get_payment_info.executeQuery();
            if (result.next()) {
                transaction_label.setText(result.getString("contribution_code") + " Contribution");
                transaction_payer_id.setText(payment.getId_number());
                transaction_payer_name.setText(payment.getLast_name() + ", " + payment.getFirst_name() + " "
                        + payment.getMiddle_name() + " " + payment.getSuffix_name());
                transaction_id.setText(String.valueOf(payment.getTransaction_id()));
                transaction_datetime.setText(result.getTimestamp("transaction_datetime").toString());
                transaction_amount.setText(String.valueOf(result.getInt("amount")));

                ObservableList<String> payment_mode = FXCollections.observableArrayList();
                payment_mode.add(result.getString("payment_mode"));
                transaction_payment_mode.setItems(payment_mode);
                transaction_payment_mode.getSelectionModel().selectFirst();

                receipt_image = result.getBlob("payer_receipt");
                if (transaction_payment_mode.getValue().equals("Cash"))
                    receipt_link.setText("No Receipt.");
                else
                    receipt_link.setText("Transaction #" + payment.getTransaction_id() + " Receipt.");
                receipt_link.setDisable(transaction_payment_mode.getValue().equals("Cash"));
                receipt_link.setOnAction(e -> {
                    viewReceipt();
                });

                transaction_status.setText(payment.getStatus());

                String transaction_message = result.getString("transaction_message");
                if (!result.wasNull())
                    transaction_comments.setText(transaction_message);
            }
            get_payment_info.close();
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
     * Set up the frame that will display the receipt of the payment.
     */
    private void viewReceipt() {
        Stage receipt_stage = new Stage();
        receipt_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
        receipt_stage.setResizable(false);
        receipt_stage.initModality(Modality.APPLICATION_MODAL);
        try {
            InputStream stream = receipt_image.getBinaryStream();
            Image image = new Image(stream);

            // Creating the image view
            ImageView imageView = new ImageView();

            // Setting image to the image view
            imageView.setImage(image);

            // Setting the image view parameters
            imageView.setX(0);
            imageView.setY(0);
            imageView.setFitHeight(600);
            imageView.setFitWidth(500);
            imageView.setPreserveRatio(true);

            // Setting the Scene object
            Group root = new Group(imageView);
            Scene scene = new Scene(root);
            receipt_stage.setTitle("Payment Receipt.");
            receipt_stage.setScene(scene);
            receipt_stage.show();
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
     * Facilitates the transaction when accepted.
     * 
     * @param event
     */
    @FXML
    private void accept_button_clicked(ActionEvent event) {
        // ask for confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Is everything good to accept?");
        alert.setContentText("Finalize the payment?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // if confirmed
                String accept_payment_query = "UPDATE `pays` SET `status` = 'Accepted', `transaction_message` = ? WHERE `transaction_id` = ?;";
                PreparedStatement update_payment = connect.prepareStatement(accept_payment_query);
                update_payment.setString(1, transaction_comments.getText());
                update_payment.setLong(2, payment.getTransaction_id());
                update_payment.executeUpdate();
                update_payment.close();

                Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                confirmation.setTitle("Confirmation Dialog");
                confirmation.setHeaderText(null);
                confirmation
                        .setContentText("Payment (Transaction ID = " + payment.getTransaction_id() + ") is accepted.");
                confirmation.showAndWait();

                // notify the payer via email
                notifyPayer("Thank you for paying.", "Accepted");
            } catch (SQLException e) {
                Alert connection_error = new Alert(Alert.AlertType.ERROR);
                connection_error.setTitle("Database Connection Error");
                connection_error.setHeaderText("Check your connection.");
                connection_error.setContentText(e.toString());
                connection_error.showAndWait();
                System.exit(0);
            }
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); // close the frame
        }
    }

    /**
     * Facilitates the transaction when rejected.
     * 
     * @param event
     */
    @FXML
    private void reject_button_clicked(ActionEvent event) {
        if (!transaction_comments.getText().isEmpty()) {
            // ask for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Is everything good to reject?");
            alert.setContentText("Finalize the payment?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // if confirmed
                    String accept_payment_query = "UPDATE `pays` SET `status` = 'Rejected', `transaction_message` = ? WHERE `transaction_id` = ?;";
                    PreparedStatement update_payment = connect.prepareStatement(accept_payment_query);
                    update_payment.setString(1, transaction_comments.getText());
                    update_payment.setLong(2, payment.getTransaction_id());
                    update_payment.executeUpdate();
                    update_payment.close();

                    Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                    confirmation.setTitle("Confirmation Dialog");
                    confirmation.setHeaderText(null);
                    confirmation.setContentText(
                            "Payment (Transaction ID = " + payment.getTransaction_id() + ") is rejected.");
                    confirmation.showAndWait();

                    // notify the payer via email
                    notifyPayer("Please contact your Classroom Representative or a BUFICOM Officer from "
                            + transaction_label.getText().replace(" Contribution", "").split("_")[0]
                            + " for clarification. Thank you.", "Rejected");
                } catch (SQLException e) {
                    Alert connection_error = new Alert(Alert.AlertType.ERROR);
                    connection_error.setTitle("Database Connection Error");
                    connection_error.setHeaderText("Check your connection.");
                    connection_error.setContentText(e.toString());
                    connection_error.showAndWait();
                    System.exit(0);
                }
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); // close the frame
            }
        } else {
            Border error_border = new Border(
                    new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
            transaction_comments.setBorder(error_border);
            transaction_comments.setPromptText("Please provide a reason.");
        }
    }

    /**
     * Notify the payer via Email about the result of the transaction.
     * 
     * @param end_message
     * @param status
     */
    private void notifyPayer(String end_message, String status) {
        try {
            // get the email address of the payer
            String payer_email_query = "SELECT `email` "
                    + "FROM `students` "
                    + "WHERE `id_number` = '" + transaction_payer_id.getText() + "';";
            PreparedStatement get_payer_email = connect.prepareStatement(payer_email_query);
            ResultSet result = get_payer_email.executeQuery();
            result.next();
            String payer_email = result.getString("email");

            // create the subject of the message
            String[] contribution_code_data = transaction_label.getText().replace(" Contribution", "").split("_");
            String subject_message = "Payment for " + contribution_code_data[0] + " A.Y. " + contribution_code_data[1]
                    + ", Semester " + contribution_code_data[2];

            // create the body of the message, along with the receipt photo if available
            String receipt_id = " None ";
            File file = null;
            if (!transaction_payment_mode.getValue().equals("Cash")) { // since receipt photo is only available when
                                                                       // payment mode isn't Cash
                receipt_id = "<img height=\"300\" width=\"200\" src=\"cid:image\" >"; // html format for the image
                                                                                      // insertion

                // create a temporary file where the receipt will be stored
                file = File.createTempFile("temp_receipt", ".png");

                // output the Blob from the database to the temporary file
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(receipt_image.getBinaryStream().readAllBytes());
                outputStream.close();
            }

            // get the current date and time when the payment is verified
            DateTimeFormatter date_time = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            String message_to_student = "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Transaction Report</title>\n" +
                    "    <style>\n" +
                    "        body {\n" +
                    "            font-family: Arial, sans-serif;\n" +
                    "            line-height: 1.6;\n" +
                    "            margin: 20px;\n" +
                    "        }\n" +
                    "        .transaction-table {\n" +
                    "            width: 70%;\n" +
                    "            border-collapse: collapse;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .transaction-table, .transaction-table th, .transaction-table td {\n" +
                    "            border: 2px solid #000;\n" +
                    "        }\n" +
                    "        .transaction-table th, .transaction-table td {\n" +
                    "            padding: 10px;\n" +
                    "            text-align: left;\n" +
                    "        }\n" +
                    "        .remarks {\n" +
                    "            font-style: italic;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "<p>Dear " + transaction_payer_name.getText() + ",</p>\n" +
                    "\n" +
                    "<p>Please find below the details of your recent transaction:</p>\n" +
                    "\n" +
                    "<table class=\"transaction-table\">\n" +
                    "    <tr>\n" +
                    "        <th>FIELD</th>\n" +
                    "        <th>DETAILS</th>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Transaction ID</td>\n" +
                    "        <td>" + transaction_id.getText() + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Student ID</td>\n" +
                    "        <td>" + transaction_payer_id.getText() + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Student Name</td>\n" +
                    "        <td>" + transaction_payer_name.getText() + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Amount</td>\n" +
                    "        <td>" + transaction_amount.getText() + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Payment Mode</td>\n" +
                    "        <td>" + transaction_payment_mode.getValue() + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Receipt</td>\n" +
                    "        <td>" + receipt_id + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Status</td>\n" +
                    "        <td>" + status + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Comments</td>\n" +
                    "        <td>" + transaction_comments.getText() + "</td>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td>Verified</td>\n" +
                    "        <td>" + date_time.format(now) + "</td>\n" +
                    "    </tr>" +
                    "</table>\n" +
                    "\n" +
                    "<p class=\"remarks\">" + end_message + "</p>\n" +
                    "\n" +
                    "<p>Best regards,<br>\n" +
                    "<strong>" + contribution_code_data[0] + " BUFICOM</strong></p>\n" +
                    "\n" +
                    "</body>";

            // send the email in a separate thread to prevent lags
            EmailSender sender = new EmailSender(payer_email, subject_message,
                    message_to_student, file);
            Thread sender_thread = new Thread(sender);
            sender_thread.start();
        } catch (IOException | SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
            throw new RuntimeException(e);
        }
    }
}
