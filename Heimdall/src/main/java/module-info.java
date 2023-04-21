module com.example.projet_heimdall {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.sun.jna.platform;
    requires org.jdom2;
    requires com.ibm.icu;
    requires com.fazecast.jSerialComm;

    opens com.example.projet_heimdall to javafx.fxml;
    exports com.example.projet_heimdall;
    exports com.example.projet_heimdall.v2;
    opens com.example.projet_heimdall.v2 to javafx.fxml;
}