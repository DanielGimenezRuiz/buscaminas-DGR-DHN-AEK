/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.buscaminas;

/**
 *
 * @author dangimrui
 */
public class ScoreCalculator {

    /**
     * Calculates the final score.
     *
     * @param difficulty the chosen difficulty
     * @param elapsedSeconds total seconds spent
     * @param revealedCells how many safe cells were revealed
     * @param won whether the player won
     * @return integer score
     */
    public static int calculate(Difficulty difficulty, long elapsedSeconds, int revealedCells, boolean won) {
        int safeCells = difficulty.getRows() * difficulty.getCols() - difficulty.getMines();

        // Base points per revealed cell
        double baseScore = revealedCells * 10.0;

        // Win bonus: 500 points
        if (won) {
            baseScore += 500;
        }

        // Time penalty: -1 point per second after first 10 seconds
        long timePenalty = Math.max(0, elapsedSeconds - 10);
        baseScore -= timePenalty;

        // Progress ratio (0.0 – 1.0)
        double progress = safeCells > 0 ? (double) revealedCells / safeCells : 0;
        baseScore *= (0.5 + progress * 0.5); // scale by progress

        // Difficulty multiplier
        baseScore *= difficulty.getScoreMultiplier();

        return Math.max(0, (int) Math.round(baseScore));
    }
}
