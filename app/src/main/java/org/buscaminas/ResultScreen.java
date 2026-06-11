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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author dangimrui
 */
public class ResultScreen {

    private final Stage owner;
    private final Difficulty difficulty;
    private final boolean won;
    private final long elapsedSeconds;
    private final int finalScore;
    private final int revealedCells;

    public ResultScreen(Stage owner, Difficulty difficulty, boolean won,
            long elapsedSeconds, int finalScore, int revealedCells) {
        this.owner = owner;
        this.difficulty = difficulty;
        this.won = won;
        this.elapsedSeconds = elapsedSeconds;
        this.finalScore = finalScore;
        this.revealedCells = revealedCells;
    }

    public void show(Runnable onRestart, Runnable onMenu) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(won ? "¡Victoria!" : "Game Over");
        dialog.setResizable(false);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Emoji + title
        Label emoji = new Label(won ? "🎉" : "💥");
        emoji.setFont(Font.font(52));

        Label titleLabel = new Label(won ? "¡Has ganado!" : "¡Boom! Has perdido");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(won ? "#27ae60" : "#e74c3c"));
        titleLabel.setEffect(new DropShadow(8, Color.web(won ? "#27ae60" : "#e74c3c")));

        // Stats table
        VBox stats = new VBox(8);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setStyle(
                "-fx-background-color: #16213e;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 14 20 14 20;"
        );

        stats.getChildren().addAll(
                statRow("🎯  Dificultad", difficulty.getDisplayName()),
                statRow("⏱️  Tiempo", formatTime(elapsedSeconds)),
                statRow("🔲  Casillas reveladas", String.valueOf(revealedCells)),
                statRow("💎  Puntuación final", String.valueOf(finalScore))
        );

        // Buttons
        HBox btnRow = new HBox(14);
        btnRow.setAlignment(Pos.CENTER);

        Button restartBtn = new Button("▶  Jugar de nuevo");
        styleBtn(restartBtn, "#27ae60");
        restartBtn.setOnAction(e -> {
            dialog.close();
            onRestart.run();
        });

        Button menuBtn = new Button("🏠  Menú principal");
        styleBtn(menuBtn, "#2980b9");
        menuBtn.setOnAction(e -> {
            dialog.close();
            onMenu.run();
        });

        btnRow.getChildren().addAll(restartBtn, menuBtn);
        root.getChildren().addAll(emoji, titleLabel, stats, btnRow);

        Scene scene = new Scene(root, 360, 380);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private HBox statRow(String key, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(10);

        Label keyLabel = new Label(key);
        keyLabel.setFont(Font.font("Arial", 14));
        keyLabel.setTextFill(Color.web("#aaaaaa"));
        keyLabel.setMinWidth(180);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        valueLabel.setTextFill(Color.WHITE);

        row.getChildren().addAll(keyLabel, valueLabel);
        return row;
    }

    private void styleBtn(Button btn, String color) {
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPrefHeight(38);
        btn.setPrefWidth(150);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand;", color));
        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
                "-fx-background-color: derive(%s,20%%); -fx-background-radius: 6; -fx-cursor: hand;", color)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand;", color)));
    }

    private String formatTime(long seconds) {
        long m = seconds / 60, s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
