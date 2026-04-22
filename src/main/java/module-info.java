module dk.easv.maria.weblager_software {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.maria.weblager_software to javafx.fxml;
    exports dk.easv.maria.weblager_software;
}