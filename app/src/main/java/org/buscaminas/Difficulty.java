/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package org.buscaminas;

/**
 *
 * @author dangimrui
 */
public enum Difficulty {
    NOVATO("Novato", 10, 10, 10, 1.0),
    INTERMEDIO("Intermedio", 16, 16, 40, 1.5),
    DIFICIL("Difícil", 30, 30, 99, 2.5);

    private final String displayName;
    private final int rows;
    private final int cols;
    private final int mines;
    private final double scoreMultiplier;

    Difficulty(String displayName, int rows, int cols, int mines, double scoreMultiplier) {
        this.displayName = displayName;
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.scoreMultiplier = scoreMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMines() {
        return mines;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }
}
