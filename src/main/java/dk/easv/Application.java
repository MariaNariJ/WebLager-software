package dk.easv;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(
                Application.class.getResource("/dk/easv/gui/log-in.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("WebLager");
        stage.setScene(scene);
        stage.show();
    }
}