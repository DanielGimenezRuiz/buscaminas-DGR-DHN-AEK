/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.buscaminas;

import java.util.Random;

/**
 *
 * @author dangimrui
 */
public class Board {

    private final Cell[][] cells;
    private final int rows;
    private final int cols;
    private final int totalMines;
    private int flagsPlaced;
    private int revealedCount;
    private boolean gameOver;
    private boolean gameWon;
    private boolean firstClick;

    public Board(Difficulty difficulty) {
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.totalMines = difficulty.getMines();
        this.flagsPlaced = 0;
        this.revealedCount = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.firstClick = true;
        this.cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell();
            }
        }
    }

    // Place mines after first click so it's never immediately deadly
    public void placeMines(int safeRow, int safeCol) {
        Random rand = new Random();
        int placed = 0;
        while (placed < totalMines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            // Avoid the first-click cell and its neighbours
            if (!cells[r][c].isMine() && !isNeighbour(r, c, safeRow, safeCol)) {
                cells[r][c].setMine(true);
                placed++;
            }
        }
        computeAdjacentCounts();
    }

    private boolean isNeighbour(int r, int c, int sr, int sc) {
        return Math.abs(r - sr) <= 1 && Math.abs(c - sc) <= 1;
    }

    private void computeAdjacentCounts() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!cells[r][c].isMine()) {
                    cells[r][c].setAdjacentMines(countAdjacentMines(r, c));
                }
            }
        }
    }

    private int countAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && cells[nr][nc].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Reveal a cell. Returns true if a mine was hit.
     */
    public boolean reveal(int row, int col) {
        if (firstClick) {
            placeMines(row, col);
            firstClick = false;
        }

        Cell cell = cells[row][col];
        if (cell.isRevealed() || cell.isFlagged()) {
            return false;
        }

        if (cell.isMine()) {
            cell.setRevealed(true);
            gameOver = true;
            return true;
        }

        floodReveal(row, col);
        checkWin();
        return false;
    }

    private void floodReveal(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return;
        }
        Cell cell = cells[r][c];
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        cell.setRevealed(true);
        revealedCount++;

        if (cell.getAdjacentMines() == 0 && !cell.isMine()) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr != 0 || dc != 0) {
                        floodReveal(r + dr, c + dc);
                    }
                }
            }
        }
    }

    public void toggleFlag(int row, int col) {
        Cell cell = cells[row][col];
        if (cell.isRevealed()) {
            return;
        }
        if (cell.isFlagged()) {
            cell.setFlagged(false);
            flagsPlaced--;
        } else {
            cell.setFlagged(true);
            flagsPlaced++;
        }
    }

    private void checkWin() {
        int safeCells = rows * cols - totalMines;
        if (revealedCount == safeCells) {
            gameWon = true;
            gameOver = true;
        }
    }

    /**
     * Reveal all mines (called on loss)
     */
    public void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine()) {
                    cells[r][c].setRevealed(true);
                }
            }
        }
    }

    // Getters
    public Cell getCell(int r, int c) {
        return cells[r][c];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public int getFlagsPlaced() {
        return flagsPlaced;
    }

    public int getRemainingMines() {
        return totalMines - flagsPlaced;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public boolean isFirstClick() {
        return firstClick;
    }

    public int getRevealedCount() {
        return revealedCount;
    }

}
