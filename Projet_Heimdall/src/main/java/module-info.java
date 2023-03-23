module com.example.projet_heimdall {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.ibm.icu;
    requires org.jdom2;
    requires commons.io;
    requires com.sun.jna.platform;

    opens projet_heimdall to javafx.fxml;
    exports projet_heimdall;
}