module com.example.netstorage {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires java.sql;

    opens com.example.netstorage to javafx.fxml;
    exports com.example.netstorage;
}