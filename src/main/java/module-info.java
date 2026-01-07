module com.mercedes.partsmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.mercedes.partsmanager.controller to javafx.fxml;
    opens com.mercedes.partsmanager.model to javafx.base;

    exports com.mercedes.partsmanager;
}
