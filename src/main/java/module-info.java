module com.example.cafe.cafe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.cafe.cafe to javafx.fxml;
    exports com.example.cafe.cafe;
}