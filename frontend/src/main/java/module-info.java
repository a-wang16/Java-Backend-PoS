module com.example.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.frontend to javafx.fxml;
    exports com.example.frontend;
}