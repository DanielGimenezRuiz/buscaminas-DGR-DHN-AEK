/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.buscaminas;

import org.buscaminas.Board;
import org.buscaminas.Difficulty;
import org.buscaminas.ScoreCalculator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author dangimrui
 */
public class GameScreen {

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final int CELL_SIZE = 32;
    private static final int HEADER_HEIGHT = 70;
    private static final int PADDING = 12;

    // ── Face states ──────────────────────────────────────────────────────────
    private static final String FACE_NORMAL = "🙂";
    private static final String FACE_PRESSED = "😮";
    private static final String FACE_DEAD = "😵";
    private static final String FACE_WIN = "😎";

    // ── Number colours ───────────────────────────────────────────────────────
    private static final String[] NUMBER_COLORS = {
        "", "#1565C0", "#2E7D32", "#C62828", "#4527A0",
        "#6D4C41", "#00838F", "#000000", "#546E7A"
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private final Stage stage;
    private final Difficulty difficulty;
    private Board board;

    private Button[][] cellButtons;
    private Label mineCountLabel;
    private Label timerLabel;
    private Button faceButton;
    private Label scoreLabel;

    private Timeline timer;
    private long elapsedSeconds = 0;
    private boolean gameActive = false;

    public GameScreen(Stage stage, Difficulty difficulty) {
        this.stage = stage;
        this.difficulty = difficulty;
    }

    // ─────────────────────────────────────────────────────────────────────────
    public void show() {
        board = new Board(difficulty);
        elapsedSeconds = 0;
        gameActive = false;

        int cols = difficulty.getCols();
        int rows = difficulty.getRows();

        int windowWidth = cols * CELL_SIZE + PADDING * 2;
        int windowHeight = rows * CELL_SIZE + HEADER_HEIGHT + PADDING * 2 + 30; // +30 score bar

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #bdbdbd;");
        root.setPadding(new Insets(PADDING));

        root.setTop(buildHeader());
        root.setCenter(buildGrid());
        root.setBottom(buildScoreBar());

        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        stage.setTitle("Buscaminas – " + difficulty.getDisplayName());
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        setupTimer();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPrefHeight(HEADER_HEIGHT);
        header.setPadding(new Insets(6, 0, 6, 0));
        header.setSpacing(0);
        header.setStyle(
                "-fx-background-color: #9e9e9e;"
                + "-fx-border-color: #757575 #e0e0e0 #e0e0e0 #757575;"
                + "-fx-border-width: 3;"
        );

        // Left: mine counter
        mineCountLabel = buildLCDLabel(String.format("%03d", board.getRemainingMines()));
        HBox leftBox = new HBox(mineCountLabel);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setPadding(new Insets(0, 0, 0, 8));
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        // Centre: smiley button
        faceButton = new Button(FACE_NORMAL);
        faceButton.setFont(Font.font(22));
        faceButton.setPrefSize(40, 40);
        faceButton.setStyle(raisedStyle());
        faceButton.setOnAction(e -> restartGame());

        // Right: timer
        timerLabel = buildLCDLabel("000");
        HBox rightBox = new HBox(timerLabel);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setPadding(new Insets(0, 8, 0, 0));
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        header.getChildren().addAll(leftBox, faceButton, rightBox);
        return header;
    }

    // ── Score bar ─────────────────────────────────────────────────────────────
    private HBox buildScoreBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 8, 0, 8));
        bar.setSpacing(6);

        Label lbl = new Label("Puntuación:");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#333333"));

        scoreLabel = new Label("0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        scoreLabel.setTextFill(Color.web("#1565C0"));

        bar.getChildren().addAll(lbl, scoreLabel);
        return bar;
    }

    private Label buildLCDLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        lbl.setTextFill(Color.RED);
        lbl.setStyle(
                "-fx-background-color: #000000;"
                + "-fx-padding: 2 6 2 6;"
                + "-fx-border-color: #444 #aaa #aaa #444;"
                + "-fx-border-width: 1;"
        );
        lbl.setMinWidth(52);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        return lbl;
    }

    // ── Grid ──────────────────────────────────────────────────────────────────
    private GridPane buildGrid() {
        int rows = difficulty.getRows();
        int cols = difficulty.getCols();
        cellButtons = new Button[rows][cols];

        GridPane grid = new GridPane();
        grid.setStyle(
                "-fx-border-color: #757575 #e0e0e0 #e0e0e0 #757575;"
                + "-fx-border-width: 3;"
        );

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Button btn = new Button();
                btn.setPrefSize(CELL_SIZE, CELL_SIZE);
                btn.setMinSize(CELL_SIZE, CELL_SIZE);
                btn.setMaxSize(CELL_SIZE, CELL_SIZE);
                btn.setStyle(raisedStyle());
                btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                final int row = r, col = c;

                // Left click – reveal
                btn.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                    if (e.getButton() == MouseButton.PRIMARY && !board.isGameOver()) {
                        faceButton.setText(FACE_PRESSED);
                    }
                });
                btn.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
                    if (e.getButton() == MouseButton.PRIMARY && !board.isGameOver()) {
                        faceButton.setText(FACE_NORMAL);
                        handleLeftClick(row, col);
                    }
                });

                // Right click – flag
                btn.setOnContextMenuRequested(e -> handleRightClick(row, col));

                cellButtons[r][c] = btn;
                grid.add(btn, c, r);
            }
        }
        return grid;
    }
 
   // ── Click handlers ────────────────────────────────────────────────────────
    private void handleLeftClick(int row, int col) {
        if (board.isGameOver()) {
            return;
        }
        if (board.getCell(row, col).isFlagged()) {
            return;
        }
        if (board.getCell(row, col).isRevealed()) {
            return;
        }

        if (!gameActive) {
            gameActive = true;
            timer.play();
        }

        boolean hitMine = board.reveal(row, col);
        refreshBoard();

        if (hitMine) {
            board.revealAllMines();
            refreshBoard();
            faceButton.setText(FACE_DEAD);
            stopTimer();
            showResult(false);
        } else if (board.isGameWon()) {
            faceButton.setText(FACE_WIN);
            stopTimer();
            refreshBoard();
            showResult(true);
        }

        updateScore();
    }

    private void handleRightClick(int row, int col) {
        if (board.isGameOver()) {
            return;
        }
        if (board.getCell(row, col).isRevealed()) {
            return;
        }

        board.toggleFlag(row, col);
        updateCellButton(row, col);
        mineCountLabel.setText(String.format("%03d", Math.max(0, board.getRemainingMines())));
        updateScore();
    }

    // ── Board refresh ─────────────────────────────────────────────────────────
    private void refreshBoard() {
        for (int r = 0; r < difficulty.getRows(); r++) {
            for (int c = 0; c < difficulty.getCols(); c++) {
                updateCellButton(r, c);
            }
        }
        mineCountLabel.setText(String.format("%03d", Math.max(0, board.getRemainingMines())));
    }

    private void updateCellButton(int r, int c) {
        Button btn = cellButtons[r][c];
        var cell = board.getCell(r, c);

        if (!cell.isRevealed()) {
            if (cell.isFlagged()) {
                btn.setText("🚩");
                btn.setStyle(raisedStyle());
            } else {
                btn.setText("");
                btn.setStyle(raisedStyle());
            }
            return;
        }

        // Revealed
        btn.setStyle(revealedStyle());

        if (cell.isMine()) {
            btn.setText("💣");
            btn.setStyle("-fx-background-color: #f44336; -fx-min-width: " + CELL_SIZE + "; -fx-min-height: " + CELL_SIZE + "; -fx-pref-width: " + CELL_SIZE + "; -fx-pref-height: " + CELL_SIZE + ";");
            btn.setFont(Font.font(14));
            return;
        }

        int adj = cell.getAdjacentMines();
        if (adj == 0) {
            btn.setText("");
        } else {
            btn.setText(String.valueOf(adj));
            btn.setTextFill(Color.web(NUMBER_COLORS[adj]));
            btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────
    private void setupTimer() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedSeconds++;
            long display = Math.min(elapsedSeconds, 999);
            timerLabel.setText(String.format("%03d", display));
            updateScore();
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    // ── Score ─────────────────────────────────────────────────────────────────
    private void updateScore() {
        int score = ScoreCalculator.calculate(
                difficulty, elapsedSeconds,
                board.getRevealedCount(), board.isGameWon()
        );
        scoreLabel.setText(String.valueOf(score));
    }

    // ── Result dialog ─────────────────────────────────────────────────────────
    private void showResult(boolean won) {
        int finalScore = ScoreCalculator.calculate(
                difficulty, elapsedSeconds,
                board.getRevealedCount(), won
        );
        ResultScreen result = new ResultScreen(stage, difficulty, won, elapsedSeconds, finalScore, board.getRevealedCount());
        result.show(() -> restartGame(), () -> goToMenu());
    }

    // ── Restart / Menu ────────────────────────────────────────────────────────
    private void restartGame() {
        stopTimer();
        show();
    }

    private void goToMenu() {
        stopTimer();
        new MenuScreen(stage).show();
    }

    // ── CSS helpers ───────────────────────────────────────────────────────────
    private String raisedStyle() {
        return "-fx-background-color: #bdbdbd;"
                + "-fx-border-color: #e0e0e0 #757575 #757575 #e0e0e0;"
                + "-fx-border-width: 2;"
                + "-fx-pref-width: " + CELL_SIZE + ";"
                + "-fx-pref-height: " + CELL_SIZE + ";"
                + "-fx-min-width: " + CELL_SIZE + ";"
                + "-fx-min-height: " + CELL_SIZE + ";"
                + "-fx-padding: 0;"
                + "-fx-cursor: hand;";
    }

    private String revealedStyle() {
        return "-fx-background-color: #c8c8c8;"
                + "-fx-border-color: #a0a0a0;"
                + "-fx-border-width: 1;"
                + "-fx-pref-width: " + CELL_SIZE + ";"
                + "-fx-pref-height: " + CELL_SIZE + ";"
                + "-fx-min-width: " + CELL_SIZE + ";"
                + "-fx-min-height: " + CELL_SIZE + ";"
                + "-fx-padding: 0;";
    }
}
