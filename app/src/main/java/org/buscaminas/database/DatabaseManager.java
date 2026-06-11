package org.buscaminas.database;

import java.io.File;
import java.sql.*;

public class DatabaseManager {

    // ── Ruta del fichero ──────────────────────────────────────────────────────
    private static final String DB_DIR =
        System.getProperty("user.home") + File.separator + ".buscaminas";
    private static final String DB_FILE =
        DB_DIR + File.separator + "buscaminas.db";
    private static final String JDBC_URL =
        "jdbc:sqlite:" + DB_FILE;

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        ensureDir();
        connect();
        applyPragmas();
        createSchema();
    }

    /** Obtiene (o crea) la instancia única. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    private void ensureDir() {
        File dir = new File(DB_DIR);
        if (!dir.exists() && !dir.mkdirs())
            throw new RuntimeException("No se pudo crear el directorio: " + DB_DIR);
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");          // necesario en módulos Java 9+
            connection = DriverManager.getConnection(JDBC_URL);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo conectar a SQLite: " + JDBC_URL, e);
        }
    }

    private void applyPragmas() {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode = WAL");   // escrituras más rápidas
            st.execute("PRAGMA foreign_keys = ON");    // integridad referencial
            st.execute("PRAGMA synchronous = NORMAL"); // equilibrio seguridad/velocidad
        } catch (SQLException e) {
            throw new RuntimeException("Error aplicando PRAGMAs", e);
        }
    }

    /**
     * Devuelve la conexión activa.
     * Si por algún motivo fue cerrada externamente, reconecta.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) connect();
        } catch (SQLException ignored) {
            connect();
        }
        return connection;
    }

    /** Debe llamarse al cerrar la aplicación (ver {@code Main.stop()}). */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
        instance = null;
    }

    public String getDatabasePath() { return DB_FILE; }

    // ── Esquema ───────────────────────────────────────────────────────────────

    private void createSchema() {
        try (Statement st = connection.createStatement()) {

            // ----------------------------------------------------------------
            // Tabla: jugadores
            // ----------------------------------------------------------------
            st.execute("""
                CREATE TABLE IF NOT EXISTS jugadores (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    alias      TEXT    NOT NULL UNIQUE COLLATE NOCASE,
                    creado_en  TEXT    NOT NULL
                                DEFAULT (datetime('now','localtime'))
                )
            """);

            
            st.execute("""
                CREATE TABLE IF NOT EXISTS partidas (
                    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                    jugador_id          INTEGER
                                        REFERENCES jugadores(id)
                                        ON DELETE SET NULL,
                    dificultad          TEXT    NOT NULL
                                        CHECK(dificultad IN
                                              ('NOVATO','INTERMEDIO','DIFICIL')),
                    resultado           TEXT    NOT NULL
                                        CHECK(resultado IN
                                              ('VICTORIA','DERROTA')),
                    tiempo_segundos     INTEGER NOT NULL
                                        CHECK(tiempo_segundos >= 0),
                    casillas_reveladas  INTEGER NOT NULL
                                        CHECK(casillas_reveladas >= 0),
                    banderas_colocadas  INTEGER NOT NULL
                                        CHECK(banderas_colocadas >= 0),
                    puntuacion          INTEGER NOT NULL
                                        CHECK(puntuacion >= 0),
                    filas               INTEGER NOT NULL,
                    columnas            INTEGER NOT NULL,
                    total_minas         INTEGER NOT NULL,
                    jugada_en           TEXT    NOT NULL
                                        DEFAULT (datetime('now','localtime'))
                )
            """);

            st.execute("""
                CREATE INDEX IF NOT EXISTS idx_partidas_jugador
                    ON partidas(jugador_id)
            """);
            st.execute("""
                CREATE INDEX IF NOT EXISTS idx_partidas_dificultad
                    ON partidas(dificultad)
            """);
            st.execute("""
                CREATE INDEX IF NOT EXISTS idx_partidas_puntuacion
                    ON partidas(puntuacion DESC)
            """);
            st.execute("""
                CREATE INDEX IF NOT EXISTS idx_partidas_fecha
                    ON partidas(jugada_en DESC)
            """);

            st.execute("""
                CREATE VIEW IF NOT EXISTS ranking AS
                SELECT
                    j.alias,
                    p.dificultad,
                    MAX(p.puntuacion)                                    AS mejor_puntuacion,
                    MIN(CASE WHEN p.resultado = 'VICTORIA'
                             THEN p.tiempo_segundos END)                 AS mejor_tiempo,
                    COUNT(*)                                             AS partidas_totales,
                    SUM(CASE WHEN p.resultado = 'VICTORIA' THEN 1
                             ELSE 0 END)                                 AS victorias
                FROM  partidas  p
                JOIN  jugadores j ON j.id = p.jugador_id
                GROUP BY j.alias, p.dificultad
                ORDER BY mejor_puntuacion DESC
            """);

        } catch (SQLException e) {
            throw new RuntimeException("Error creando el esquema de la BD", e);
        }
    }
}