package projet_heimdall;

import v2.MachineCliente;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication2 extends Application {

    private double xOffset, yOffset;
    @Override
    public void start(Stage stage) throws IOException {

        // Créer une étiquette
        Label title = new Label("Bienvenu sur Heimdall");
        Label label = new Label("");
        Label lbl2 = new Label("");

        title.setFont(Font.font("Arial",30));
        label.setFont(Font.font("Arial",20));
        lbl2.setFont(Font.font("Arial",20));

        // Créer une disposition empilée pour afficher l'étiquette au centre de la fenêtre
        BorderPane root = new BorderPane();
        root.setTop(title);
        root.setCenter(label);
        root.setBottom(lbl2);

        BorderPane.setAlignment(title,Pos.CENTER);
        BorderPane.setAlignment(label,Pos.CENTER);
        BorderPane.setAlignment(lbl2,Pos.CENTER);
        label.setAlignment(Pos.CENTER);

        label.setWrapText(true);

        BorderPane.setMargin(title,new Insets(50,0,0,0));
        BorderPane.setMargin(lbl2,new Insets(0,0,50,0));
        BorderPane.setMargin(label,new Insets(0,85,0,85));

        Scene scene = new Scene(root, 600, 400);

        stage.initStyle(StageStyle.UNDECORATED);

        stage.setOnCloseRequest(windowEvent -> windowEvent.consume());

        // Gérer le déplacement de la fenêtre avec la souris
        root.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.setScene(scene);
        stage.show();
        stage.setIconified(true);

        try {
            MachineCliente mc = new MachineCliente(stage,label,lbl2);
            Thread thread = new Thread(mc);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}