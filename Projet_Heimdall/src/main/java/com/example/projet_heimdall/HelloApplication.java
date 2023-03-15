package com.example.projet_heimdall;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class HelloApplication extends Application {

    private double xOffset, yOffset;
    @Override
    public void start(Stage stage) throws IOException {

        // Créer une étiquette
        Label title = new Label("Bienvenu sur Heimdall");
        Label label = new Label("");

        // Créer une disposition empilée pour afficher l'étiquette au centre de la fenêtre
        BorderPane root = new BorderPane();
        root.getChildren().addAll(title, label);

        Scene scene = new Scene(root, 320, 240);

        stage.initStyle(StageStyle.UNDECORATED);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                windowEvent.consume();
            }
        });

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
    }

    /*private HBox createTitleBar(Stage stage) {
        Text titleText = new Text("Custom Title Bar Example");
        titleText.getStyleClass().add("title-text");

        Button closeButton = new Button("X");
        closeButton.setOnAction(event -> stage.close());

        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("title-bar");
        titleBar.getChildren().addAll(titleText, new HBox(), closeButton);
        HBox.setHgrow(titleBar.getChildren().get(1), Priority.ALWAYS);
        titleBar.setPadding(new Insets(5));
        titleBar.setAlignment(Pos.CENTER);

        // Remove standard window buttons
        WindowHelper windowHelper = WindowHelper.getWindowHelper(stage);
        if (windowHelper != null) {
            windowHelper.setHasMinimizeButton(false);
            windowHelper.setHasMaximizeButton(false);
            windowHelper.setHasCloseButton(false);
        }

        return titleBar;
    }*/

    public static void main(String[] args) {
        launch();
    }
}