package org.buscaminas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreCalculator – cálculo de puntuación")
class ScoreCalculatorTest {

    // ── Invariantes generales ─────────────────────────────────────────────────

    @Test
    @DisplayName("La puntuación nunca es negativa")
    void scoreNeverNegative() {
        int score = ScoreCalculator.calculate(Difficulty.NOVATO, 9999, 0, false);
        assertTrue(score >= 0);
    }

    @ParameterizedTest(name = "{0}: score >= 0 con 0 reveladas y 9999s")
    @EnumSource(Difficulty.class)
    @DisplayName("Puntuación no negativa en cualquier dificultad con peores condiciones")
    void scoreNonNegativeAllDifficulties(Difficulty d) {
        int score = ScoreCalculator.calculate(d, 9999, 0, false);
        assertTrue(score >= 0, "score debe ser >= 0 para " + d);
    }

    // ── Victoria vs derrota ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Bono de victoria")
    class WinBonus {

        @Test
        @DisplayName("Ganar da más puntos que perder con iguales condiciones")
        void winScoreHigherThanLoss() {
            int win  = ScoreCalculator.calculate(Difficulty.NOVATO, 5, 50, true);
            int loss = ScoreCalculator.calculate(Difficulty.NOVATO, 5, 50, false);
            assertTrue(win > loss, "victoria debe puntuar más que derrota");
        }

        @Test
        @DisplayName("Con 0 reveladas y victoria, el score refleja el bono")
        void winWithZeroRevealedStillScores() {
            int score = ScoreCalculator.calculate(Difficulty.NOVATO, 0, 0, true);
            assertTrue(score > 0, "ganar sin revelar nada sigue sumando puntos");
        }
    }

    // ── Penalización por tiempo ────────────────────────────────────────────────

    @Nested
    @DisplayName("Penalización por tiempo")
    class TimePenalty {

        @Test
        @DisplayName("Más tiempo → menos puntos (mismas condiciones)")
        void moreTimetMeansLessScore() {
            int fast = ScoreCalculator.calculate(Difficulty.NOVATO, 5,  50, false);
            int slow = ScoreCalculator.calculate(Difficulty.NOVATO, 200, 50, false);
            assertTrue(fast >= slow, "tardar más no debe dar más puntos");
        }

        @Test
        @DisplayName("Los primeros 10 segundos no penalizan")
        void first10SecondsNoPenalty() {
            int at0  = ScoreCalculator.calculate(Difficulty.NOVATO, 0,  40, false);
            int at10 = ScoreCalculator.calculate(Difficulty.NOVATO, 10, 40, false);
            assertEquals(at0, at10, "0 s y 10 s deben dar la misma puntuación");
        }

        @Test
        @DisplayName("El segundo 11 ya penaliza respecto al 10")
        void second11Penalizes() {
            int at10 = ScoreCalculator.calculate(Difficulty.NOVATO, 10, 40, false);
            int at11 = ScoreCalculator.calculate(Difficulty.NOVATO, 11, 40, false);
            assertTrue(at10 >= at11, "el segundo 11 debe penalizar respecto al 10");
        }
    }

    // ── Progreso (casillas reveladas) ─────────────────────────────────────────

    @Nested
    @DisplayName("Influencia del progreso")
    class Progress {

        @Test
        @DisplayName("Más casillas reveladas → mayor puntuación")
        void moreCellsRevealedMoreScore() {
            int few  = ScoreCalculator.calculate(Difficulty.NOVATO, 5, 10, false);
            int many = ScoreCalculator.calculate(Difficulty.NOVATO, 5, 80, false);
            assertTrue(many >= few, "revelar más casillas debe dar más puntos");
        }

        @Test
        @DisplayName("0 casillas reveladas sin victoria da 0 puntos")
        void zeroCellsZeroScore() {
            int score = ScoreCalculator.calculate(Difficulty.NOVATO, 5, 0, false);
            assertEquals(0, score, "sin revelar nada y sin ganar → 0 puntos");
        }
    }

    // ── Multiplicador de dificultad ───────────────────────────────────────────

    @Nested
    @DisplayName("Multiplicador de dificultad")
    class DifficultyMultiplier {

        @Test
        @DisplayName("INTERMEDIO puntúa más que NOVATO con iguales parámetros")
        void intermedioMoreThanNovato() {
            int novato     = ScoreCalculator.calculate(Difficulty.NOVATO,     5, 50, true);
            int intermedio = ScoreCalculator.calculate(Difficulty.INTERMEDIO, 5, 50, true);
            assertTrue(intermedio > novato,
                "INTERMEDIO debe superar a NOVATO: intermedio=" + intermedio + " novato=" + novato);
        }

        @Test
        @DisplayName("DIFÍCIL puntúa más que INTERMEDIO con iguales parámetros")
        void dificilMoreThanIntermedio() {
            int intermedio = ScoreCalculator.calculate(Difficulty.INTERMEDIO, 5, 50, true);
            int dificil    = ScoreCalculator.calculate(Difficulty.DIFICIL,    5, 50, true);
            assertTrue(dificil > intermedio,
                "DIFÍCIL debe superar a INTERMEDIO: dificil=" + dificil + " intermedio=" + intermedio);
        }
    }

    // ── Casos límite ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Casos límite")
    class EdgeCases {

        @Test
        @DisplayName("0 segundos no produce excepción ni negativo")
        void zeroSeconds() {
            assertDoesNotThrow(() -> {
                int s = ScoreCalculator.calculate(Difficulty.NOVATO, 0, 5, false);
                assertTrue(s >= 0);
            });
        }

        @Test
        @DisplayName("Tiempo muy elevado (Long.MAX_VALUE) no lanza excepción")
        void extremelyLargeTime() {
            assertDoesNotThrow(() -> {
                int s = ScoreCalculator.calculate(Difficulty.NOVATO, Long.MAX_VALUE, 5, false);
                assertTrue(s >= 0);
            });
        }

        @Test
        @DisplayName("Número máximo de casillas reveladas no lanza excepción")
        void maxRevealedCells() {
            int safe = Difficulty.NOVATO.getRows() * Difficulty.NOVATO.getCols()
                     - Difficulty.NOVATO.getMines(); // 90 casillas seguras
            assertDoesNotThrow(() -> {
                int s = ScoreCalculator.calculate(Difficulty.NOVATO, 30, safe, true);
                assertTrue(s > 0);
            });
        }
    }
}