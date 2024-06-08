package control;

import model.DataManager;
import model.StudentPaymentInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;

/**
 * For recording transactions.
 */
public class TransactionProcess {
    private static Connection connect;
    @FXML
    private AnchorPane transaction_scene;
    @FXML
    private Label transaction_label;
    @FXML
    private TextField transaction_payer_id;
    @FXML
    private TextField transaction_payer_name;
    @FXML
    private ComboBox<String> transaction_payer_year;
    @FXML
    private TextField transaction_amount;
    @FXML
    private TextField transaction_semester;
    @FXML
    private ComboBox<String> transaction_payment_mode;
    @FXML
    private Button add_receipt_button;
    @FXML
    private Hyperlink receipt_link;
    private String contribution_code;
    private String org_code;
    private String semester;
    private String academic_year;
    private StudentPaymentInfo payer;

    public TransactionProcess() {
    }

    /**
     * Set up the initial frame.
     *
     * @param contribution_code
     */
    public void initialize(String contribution_code) {
        connect = DataManager.getConnect();

        // get the information of the contribution from its code
        this.contribution_code = contribution_code;
        String[] contribution_details = this.contribution_code.split("_");
        org_code = contribution_details[0];
        academic_year = contribution_details[1];
        semester = contribution_details[2];

        // set up the title
        transaction_label.setText(org_code + " Transaction");
        transaction_semester.setText(semester);

        setupTransaction();
    }

    /**
     * Fetch and set up the data of the transaction.
     */
    private void setupTransaction() {
        // get the amount
        try {
            Connection connect = DataManager.getConnect();
            String amount_query = "SELECT `amount` "
                    + "FROM `contributions` "
                    + "WHERE `contribution_code` = '" + contribution_code + "';";
            PreparedStatement get_amount = connect.prepareStatement(amount_query);
            ResultSet result_id = get_amount.executeQuery();
            result_id.next();
            String amount = result_id.getString("amount");
            transaction_amount.setText(amount);
            get_amount.close();
        } catch (SQLException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }

        // add the modes of payment
        ObservableList<String> modes = FXCollections.observableArrayList();
        modes.addAll("Cash", "GCash", "Others");
        transaction_payment_mode.setItems(modes);
        transaction_payment_mode.getSelectionModel().selectFirst();

        // disable the receipt input if the mode is Cash
        transaction_payment_mode.getSelectionModel().selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                    // disable the input of receipt when the mode of payment is "Cash"
                    add_receipt_button.setDisable(transaction_payment_mode.getValue().equals("Cash"));
                    receipt_link.setDisable(transaction_payment_mode.getValue().equals("Cash"));
                    receipt_link.setBorder(Border.EMPTY);
                });
    }

    /**
     * For recording of transactions.
     */
    @FXML
    private void recordTransaction() {
        if (!transaction_payment_mode.getValue().equals("Cash") && receipt_link.getText().equals("No File Chosen")) {
            Border error_border = new Border(
                    new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
            receipt_link.setBorder(error_border);

            Alert no_receipt = new Alert(Alert.AlertType.ERROR);
            no_receipt.setTitle("Receipt Required.");
            no_receipt.setHeaderText(null);
            no_receipt.setContentText("Please add receipt.");
            no_receipt.showAndWait();
        } else {
            try {
                String update_payer_status_query = "INSERT INTO `pays` (`contribution_code`, `payer_id`, `payment_mode`, `payer_receipt`, `status`) "
                        + "VALUES (?, ?, ?, ?, 'Pending');";
                PreparedStatement insert_payer_status = connect.prepareStatement(update_payer_status_query);
                insert_payer_status.setString(1, contribution_code);
                insert_payer_status.setString(2, payer.getId_number());
                insert_payer_status.setString(3, transaction_payment_mode.getValue());
                if (transaction_payment_mode.getValue().equals("Cash")) {
                    insert_payer_status.setNull(4, Types.NULL);
                } else {
                    try {
                        File receipt_image = new File(receipt_link.getText());
                        FileInputStream receipt_image_fin = new FileInputStream(receipt_image);
                        insert_payer_status.setBinaryStream(4, receipt_image_fin, (int) receipt_image.length());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                insert_payer_status.executeUpdate();
                insert_payer_status.close();

                // for confirmation
                String transaction_id_query = "SELECT `transaction_id` "
                        + "FROM `pays` "
                        + "WHERE `contribution_code` = '" + contribution_code + "' "
                        + "AND `payer_id` = '" + payer.getId_number() + "' "
                        + "ORDER BY `transaction_id` DESC;";
                PreparedStatement get_transaction_id = connect.prepareStatement(transaction_id_query);
                ResultSet result_id = get_transaction_id.executeQuery();
                result_id.next();
                String transaction_id = result_id.getString("transaction_id");
                get_transaction_id.close();

                Alert success_transaction = new Alert(Alert.AlertType.INFORMATION);
                success_transaction.setTitle("Transaction Successful");
                success_transaction.setHeaderText(null);
                success_transaction.setContentText("Transaction Successful (with Transaction ID = " + transaction_id
                        + ") for Student " + payer.getId_number() + ". "
                        + "Please wait for verification from the BUFICOM Officers. Thank you.");
                success_transaction.showAndWait();
            } catch (SQLException e) {
                Alert connection_error = new Alert(Alert.AlertType.ERROR);
                connection_error.setTitle("Database Connection Error");
                connection_error.setHeaderText("Check your connection.");
                connection_error.setContentText(e.toString());
                connection_error.showAndWait();
                System.exit(0);
            }

            if (semester.equals("1"))
                payer.setFirst_sem_status("Pending");
            else
                payer.setSecond_sem_status("Pending");

            // close the window
            ((Stage) transaction_scene.getScene().getWindow()).close();
        }
    }

    /**
     * Provide filechooser for selecting photo of the receipt.
     */
    @FXML
    private void addReceiptAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Insert Receipt Photo.");

        // open the FileChooser on the folder of the currently selected photo
        if (!receipt_link.getText().equals("No File Chosen")) {
            String path = receipt_link.getText().replace('\\', '/');
            String[] folder_paths = path.split("/");
            StringBuilder folder_path = new StringBuilder();

            for (String folder : folder_paths)
                folder_path.append(folder).append('\\');

            fileChooser.setInitialDirectory(new File(folder_path.toString()));
        }

        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("IMAGE FILES", "*.jpg", "*.png", "*.jpeg"));
        File receipt_file = fileChooser.showOpenDialog(((Stage) transaction_scene.getScene().getWindow()).getOwner());
        if (receipt_file != null)
            receipt_link.setText(receipt_file.getAbsolutePath());
    }

    /**
     * Facilitates if a photo is dragged over the hyperlink.
     * 
     * @param event
     */
    @FXML
    private void receiptDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        final boolean isAccepted = dragboard.getFiles().get(0).getName().toLowerCase().endsWith(".png")
                || dragboard.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                || dragboard.getFiles().get(0).getName().toLowerCase().endsWith(".jpg");
        if ((dragboard.hasImage() || dragboard.hasFiles()) && isAccepted) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    /**
     * Facilitates if a photo is dropped on the hyperlink.
     * 
     * @param event
     */
    @FXML
    private void receiptDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasImage() || dragboard.hasFiles()) {
            receipt_link.setText(dragboard.getFiles().get(0).getPath());
        }
        event.consume();
    }

    /**
     * Facilitates the frame for viewing the receipt when clicking the hyperlink.
     */
    @FXML
    private void receiptViewer() {
        if (!receipt_link.getText().equals("No File Chosen")) {
            Stage receipt_stage = new Stage();
            receipt_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
            receipt_stage.setResizable(false);
            receipt_stage.initModality(Modality.APPLICATION_MODAL);
            try {
                InputStream stream = new FileInputStream(receipt_link.getText());
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
                receipt_stage.setTitle("Payment Receipt");
                receipt_stage.setScene(scene);
                receipt_stage.show();
            } catch (FileNotFoundException e) {
                Alert file_not_found = new Alert(Alert.AlertType.INFORMATION);
                file_not_found.setTitle("File Not Found.");
                file_not_found.setHeaderText(null);
                file_not_found.setContentText("Please check the file if exist.");
                file_not_found.showAndWait();
            }
        }
    }

    /*
     * Setters and Getters.
     */

    public String getOrg_code() {
        return org_code;
    }

    public void setOrg_code(String org_code) {
        this.org_code = org_code;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public StudentPaymentInfo getPayer() {
        return payer;
    }

    /**
     * Automatically add the information of the payer from the table.
     *
     * @param payer
     */
    public void setPayer(StudentPaymentInfo payer) {
        this.payer = payer;

        transaction_payer_id.setText(this.payer.getId_number());
        transaction_payer_name.setText(this.payer.getLast_name() + ", "
                + this.payer.getFirst_name() + " "
                + this.payer.getMiddle_name() + " "
                + this.payer.getSuffix_name());

        ObservableList<String> year_levels = FXCollections.observableArrayList();
        year_levels.add(this.payer.getYear_level());
        transaction_payer_year.setItems(year_levels);
        transaction_payer_year.getSelectionModel().selectFirst();
    }

    public String getAcademic_year() {
        return academic_year;
    }

    public void setAcademic_year(String academic_year) {
        this.academic_year = academic_year;
    }
}
