module com.optimatch {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.optimatch to javafx.fxml;
    opens com.optimatch.view to javafx.fxml;
    opens com.optimatch.model to javafx.fxml;
    opens com.optimatch.viewmodel to javafx.fxml;

    exports com.optimatch;
    exports com.optimatch.model;
    exports com.optimatch.view;
    exports com.optimatch.viewmodel;
    exports com.optimatch.service;
    exports com.optimatch.dao;
    exports com.optimatch.algorithm;
}
