package com.example.projet_heimdall;

import com.example.projet_heimdall.v2.HeimdallEjector;
import com.example.projet_heimdall.v2.MachineCliente;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

public class HelloApplication3 extends Application {

    private double xOffset, yOffset;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        // Créer une étiquette
        Label title = new Label("Bienvenue sur Heimdall");
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

        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setAlignment(label,Pos.CENTER);
        BorderPane.setAlignment(lbl2,Pos.CENTER);
        label.setAlignment(Pos.CENTER);

        label.setWrapText(true);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setPrefHeight(Region.USE_COMPUTED_SIZE);

        BorderPane.setMargin(title,new Insets(50,0,0,0));
        BorderPane.setMargin(lbl2,new Insets(0,0,50,0));
        BorderPane.setMargin(label,new Insets(0,85,0,85));

        Scene scene = new Scene(root, 600, 500);

        stage.initStyle(StageStyle.UNDECORATED);

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

        HeimdallEjector mc = new HeimdallEjector(stage,label,lbl2);

        stage.setOnCloseRequest((WindowEvent event) -> event.consume());
        Platform.setImplicitExit(false);

        try {
            Thread thread = new Thread(mc);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
