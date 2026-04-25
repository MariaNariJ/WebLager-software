package dk.easv.gui;

import dk.easv.bll.TIFFService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserviewController {

    @FXML
    private void onLogOutClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/gui/log-in.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("WebLager");
            stage.sizeToScene();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final TIFFService apiService = new TIFFService();

    @FXML
    private VBox sidebar;

    @FXML
    private Pane sidebarTrigger;

    private boolean sidebarVisible = false;


    @FXML
    private Button sidebarLockButton;

    private boolean sidebarLocked = false;

    @FXML
    private void initialize() {
        sidebarTrigger.setOnMouseEntered(event -> showSidebar());
        sidebar.setOnMouseExited(event -> hideSidebar());
    }

    private void showSidebar() {
        if (sidebarVisible) return;

        sidebarVisible = true;

        TranslateTransition transition = new TranslateTransition(Duration.millis(110), sidebar);
        transition.setToX(180);
        transition.play();
    }

    private void hideSidebar() {
        if (!sidebarVisible || sidebarLocked) return;

        sidebarVisible = false;

        TranslateTransition transition = new TranslateTransition(Duration.millis(220), sidebar);
        transition.setToX(0);
        transition.play();
    }

    @FXML
    private void onSidebarLockClicked() {
        sidebarLocked = !sidebarLocked;

        if (sidebarLocked) {
            if (!sidebarLockButton.getStyleClass().contains("sidebar-icon-button-active")) {
                sidebarLockButton.getStyleClass().add("sidebar-icon-button-active");
            }
            showSidebar();
        } else {
            sidebarLockButton.getStyleClass().remove("sidebar-icon-button-active");
        }
    }

    public void onFetchFilesClicked() throws Exception {
//        System.out.println(apiService.getCount());
        apiService.processTiffs();
        //Path is: ..\Users\[Username]\Documents\TIFFApp_tiffs\
    }
}