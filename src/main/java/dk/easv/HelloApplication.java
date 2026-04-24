package dk.easv;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("/dk/easv/gui/log-in.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("WebLager");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }
}