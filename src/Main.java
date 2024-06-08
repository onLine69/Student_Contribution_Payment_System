import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

//TODO: Retype the filepaths of the images, kapoy man. But usable na sya.
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage login_stage) throws IOException {
        // starts with the login scene
        FXMLLoader login_view = new FXMLLoader(getClass().getResource("/view/login-frame.fxml"));
        Scene login_scene = new Scene(login_view.load());

        login_stage.setTitle("Login to SCPS");
        login_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
        login_stage.setScene(login_scene);
        login_stage.setResizable(false);
        login_stage.show();
    }
}
