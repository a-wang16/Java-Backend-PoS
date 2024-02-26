module com.example.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.frontend to javafx.fxml;
    exports com.example.frontend;
}