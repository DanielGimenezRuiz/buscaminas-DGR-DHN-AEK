/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.buscaminas;

import org.buscaminas.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 *
 * @author dangimrui
 */
public class MenuScreen {

    private final Stage stage;

    public MenuScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Title
        Label title = new Label("BUSCAMINAS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        title.setTextFill(Color.web("#e0e0e0"));
        title.setEffect(new DropShadow(10, Color.web("#00d4ff")));

        Label subtitle = new Label("Elige tu dificultad");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.web("#888888"));

        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button novato = createDifficultyButton("NOVATO", "10×10  •  10 minas", "#27ae60", Difficulty.NOVATO);
        Button intermedio = createDifficultyButton("INTERMEDIO", "16×16  •  40 minas", "#f39c12", Difficulty.INTERMEDIO);
        Button dificil = createDifficultyButton("DIFÍCIL", "30×30  •  99 minas", "#e74c3c", Difficulty.DIFICIL);

        buttonBox.getChildren().addAll(novato, intermedio, dificil);
        root.getChildren().addAll(title, subtitle, buttonBox);

        Scene scene = new Scene(root, 420, 420);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private Button createDifficultyButton(String title, String subtitle, String color, Difficulty difficulty) {
        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 12));
        subtitleLabel.setTextFill(Color.web("#cccccc"));

        content.getChildren().addAll(titleLabel, subtitleLabel);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setPrefWidth(300);
        btn.setPrefHeight(70);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: transparent;",
                color
        ));

        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
                "-fx-background-color: derive(%s, 20%%); -fx-background-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, %s, 15, 0.4, 0, 0);",
                color, color
        )));
        btn.setOnMouseExited(e -> btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 10; -fx-cursor: hand;",
                color
        )));

        btn.setOnAction(e -> {
            GameScreen gameScreen = new GameScreen(stage, difficulty);
            gameScreen.show();
        });

        return btn;
    }
}
