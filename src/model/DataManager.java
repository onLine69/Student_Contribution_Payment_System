package model;

import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connects to the Database.
 */
public class DataManager {
    private static Connection connect;
    // TODO: Change the Academic Year often as I don't know how to change it
    // automatically :<<
    private static String academic_year = "2023-2024";
    private static final boolean isConnectOnline = false;

    public DataManager() {
    }

    /**
     * Create a connection to the database.
     */
    public static void createConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String sql_name;
            String host_name;
            String port;
            String schema_name;
            String username;
            String password;

            if (isConnectOnline) {
                // use this for the online database
                sql_name = "";
                host_name = "";
                port = "";
                schema_name = "";
                username = "";
                password = "";
            } else {
                // change for new connection (this is for my personal connection)
                sql_name = "";
                host_name = "";
                port = "";
                schema_name = "";
                username = "";
                password = "";
            }

            connect = DriverManager.getConnection(
                    "jdbc:" + sql_name + "://" + host_name + ":" + port + "/" + schema_name, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText("Check your connection.");
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
            System.exit(0);
        }
    }

    /**
     * Get the connection from the database.
     *
     * @return connection to the database
     */
    public static Connection getConnect() {
        return connect;
    }

    public static String getAcademic_year() {
        return academic_year;
    }

    public static void setAcademic_year(String academic_year) {
        DataManager.academic_year = academic_year;
    }

}
