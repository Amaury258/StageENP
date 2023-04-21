package com.example.projet_heimdall;

import javafx.scene.paint.Color;
import com.example.projet_heimdall.v2.MachineBlanche;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {

    private double xOffset, yOffset;
    @Override
    public void start(Stage stage) throws IOException {

//        ImageView imageView = new ImageView(new Image(System.getProperty("user.dir")+"\\src\\main\\resources\\waiting.gif"));
        ImageView imageView = new ImageView(new Image(getClass().getResource("/waiting.gif").toExternalForm()));


        // Créer une étiquette
        Label title = new Label("Bienvenue sur Heimdall");
        Label label = new Label("");
        Label lbl2 = new Label("");

        VBox vbox = new VBox();

        vbox.getChildren().addAll(label,imageView);
        vbox.setSpacing(5);

        title.setFont(Font.font("Arial",30));
        label.setFont(Font.font("Arial",20));
        lbl2.setFont(Font.font("Arial",20));

        // Créer une disposition empilée pour afficher l'étiquette au centre de la fenêtre
        BorderPane root = new BorderPane();
        root.setTop(title);
        root.setCenter(vbox);
        root.setBottom(lbl2);

        vbox.setAlignment(Pos.CENTER);

        BorderPane.setAlignment(title,Pos.CENTER);
        BorderPane.setAlignment(vbox,Pos.CENTER);
        BorderPane.setAlignment(lbl2,Pos.CENTER);

        label.setPadding(new Insets(0,50,0,50));
        label.setWrapText(true);

        BorderPane.setMargin(title,new Insets(50,0,0,0));
        BorderPane.setMargin(lbl2,new Insets(0,0,50,0));

        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 700, 550);

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

        try {
            MachineBlanche mb = new MachineBlanche(vbox,lbl2);
            Thread thread = new Thread(mb);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {

        }

    }

    public static void main(String[] args) {
        launch();
    }
}