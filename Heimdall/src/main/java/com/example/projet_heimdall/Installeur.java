package com.example.projet_heimdall;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Installeur extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String destination = "";
        String machine = "";

        Button install = new Button("Installer");

        // Création de la grille pour disposer les éléments
//        GridPane gridPane = new GridPane();
//        gridPane.setHgap(10);
//        gridPane.setVgap(10);
//        gridPane.add(textField, 0, 0);
//        gridPane.add(browseButton, 1, 0);

        BorderPane borderPane = new BorderPane();
        VBox chemin = new VBox();
        VBox toggle = new VBox();
        VBox all = new VBox();
        HBox txtbutton = new HBox();
        Label lbl = new Label("Dossier de déstination : ");
        Label lblError = new Label("");
        Label titre = new Label("Heimdall : Installeur");
        TextField txtChemin = new TextField("C:\\!dep-user\\Heimdall");
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton rdBlanche = new RadioButton("Machine blanche");
        rdBlanche.setToggleGroup(toggleGroup);
        RadioButton rdClient = new RadioButton("Machine cliente");
        rdClient.setToggleGroup(toggleGroup);

        lblError.setMaxWidth(250);
        lblError.setWrapText(true);

        // Création du bouton pour ouvrir la fenêtre de sélection de dossier
        Button browseButton = new Button("Parcourir");
        browseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Choisir le dossier de destination");
                File selectedDirectory = directoryChooser.showDialog(primaryStage);
                if (selectedDirectory != null) {
                    txtChemin.setText(selectedDirectory.getAbsolutePath()+"\\Heimdall");
                }
            }
        });

        titre.setFont(new Font("Arial",30));

        txtbutton.getChildren().addAll(txtChemin,browseButton);

        txtbutton.setSpacing(10);

        all.getChildren().addAll(lbl,txtbutton,rdBlanche,rdClient,lblError);

        all.setPadding(new Insets(0,20,0,20));

        Pane scrollPane = new Pane(all);

        borderPane.setCenter(scrollPane);
        borderPane.setBottom(install);
        borderPane.setTop(titre);
        HBox.setHgrow(lbl, Priority.ALWAYS);
        HBox.setHgrow(txtChemin, Priority.ALWAYS);
        HBox.setHgrow(rdBlanche, Priority.ALWAYS);
        HBox.setHgrow(rdClient, Priority.ALWAYS);
        HBox.setHgrow(lblError, Priority.ALWAYS);

        BorderPane.setMargin(scrollPane,new Insets(20));
        BorderPane.setAlignment(titre,Pos.CENTER);
        BorderPane.setAlignment(install,Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(install,new Insets(0,10,10,0));

        install.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    if(toggleGroup.getSelectedToggle() == rdClient){
                        Runtime.getRuntime().exec(System.getProperty("user.dir")+"\\installerClient.cmd "+txtChemin.getText());
                        System.exit(0);
                    } else if (toggleGroup.getSelectedToggle() == rdBlanche) {
                        Runtime.getRuntime().exec(System.getProperty("user.dir")+"\\installerBlanche.cmd "+txtChemin.getText());
                        System.exit(0);
                    } else {
                        lblError.setText("Veuillez sélectionner le type de la machine (Cliente ou Blanche)!");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });



        // Création de la scène
        Scene scene = new Scene(borderPane, 350, 200);

        // Configuration de la fenêtre principale
        primaryStage.setTitle("Installeur");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
