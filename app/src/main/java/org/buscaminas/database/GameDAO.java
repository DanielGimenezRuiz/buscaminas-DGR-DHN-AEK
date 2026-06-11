package org.buscaminas.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class GameDAO {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public long findOrCreateJugador(String alias) {
        if (alias == null || alias.isBlank())
            throw new IllegalArgumentException("El alias no puede estar vacío");
        alias = alias.strip();

        final String SEL = "SELECT id FROM jugadores WHERE alias = ? COLLATE NOCASE";
        try (PreparedStatement ps = db.getConnection().prepareStatement(SEL)) {
            ps.setString(1, alias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando jugador '" + alias + "'", e);
        }

        final String INS = "INSERT INTO jugadores(alias) VALUES(?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                INS, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, alias);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creando jugador '" + alias + "'", e);
        }
        throw new RuntimeException("No se obtuvo id para el jugador '" + alias + "'");
    }

    public List<String> getAllAliases() {
        List<String> result = new ArrayList<>();
        final String SQL = "SELECT alias FROM jugadores ORDER BY alias COLLATE NOCASE";
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(SQL)) {
            while (rs.next()) result.add(rs.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException("Error listando jugadores", e);
        }
        return result;
    }

    public long savePartida(GameRecord record) {
        final String SQL = """
            INSERT INTO partidas
                (jugador_id, dificultad, resultado,
                 tiempo_segundos, casillas_reveladas, banderas_colocadas,
                 puntuacion, filas, columnas, total_minas)
            VALUES (?,?,?, ?,?,?, ?,?,?,?)
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                SQL, Statement.RETURN_GENERATED_KEYS)) {

            if (record.getJugadorId() != null)
                ps.setLong(1, record.getJugadorId());
            else
                ps.setNull(1, Types.INTEGER);

            ps.setString(2, record.getDificultad());
            ps.setString(3, record.getResultado());
            ps.setInt   (4, record.getTiempoSegundos());
            ps.setInt   (5, record.getCasillasReveladas());
            ps.setInt   (6, record.getBanderasColocadas());
            ps.setInt   (7, record.getPuntuacion());
            ps.setInt   (8, record.getFilas());
            ps.setInt   (9, record.getColumnas());
            ps.setInt   (10, record.getTotalMinas());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    record.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando partida", e);
        }
        throw new RuntimeException("No se obtuvo id al insertar la partida");
    }

    public List<GameRecord> getHistorial(int limit) {
        String sql = """
            SELECT p.id, p.jugador_id, j.alias,
                   p.dificultad, p.resultado,
                   p.tiempo_segundos, p.casillas_reveladas, p.banderas_colocadas,
                   p.puntuacion, p.filas, p.columnas, p.total_minas, p.jugada_en
            FROM  partidas  p
            LEFT  JOIN jugadores j ON j.id = p.jugador_id
            ORDER BY p.jugada_en DESC
        """ + limitClause(limit);
        return fetchRecords(sql);
    }

    public List<GameRecord> getHistorialPorDificultad(String dificultad, int limit) {
        String sql = """
            SELECT p.id, p.jugador_id, j.alias,
                   p.dificultad, p.resultado,
                   p.tiempo_segundos, p.casillas_reveladas, p.banderas_colocadas,
                   p.puntuacion, p.filas, p.columnas, p.total_minas, p.jugada_en
            FROM  partidas  p
            LEFT  JOIN jugadores j ON j.id = p.jugador_id
            WHERE p.dificultad = ?
            ORDER BY p.jugada_en DESC
        """ + limitClause(limit);
        return fetchRecordsParam(sql, dificultad);
    }

    public List<GameRecord> getHistorialJugador(String alias, int limit) {
        String sql = """
            SELECT p.id, p.jugador_id, j.alias,
                   p.dificultad, p.resultado,
                   p.tiempo_segundos, p.casillas_reveladas, p.banderas_colocadas,
                   p.puntuacion, p.filas, p.columnas, p.total_minas, p.jugada_en
            FROM  partidas  p
            JOIN  jugadores j ON j.id = p.jugador_id
            WHERE j.alias = ? COLLATE NOCASE
            ORDER BY p.jugada_en DESC
        """ + limitClause(limit);
        return fetchRecordsParam(sql, alias);
    }

    public Optional<Integer> getMejorPuntuacion(String alias, String dificultad) {
        final String SQL = """
            SELECT MAX(p.puntuacion)
            FROM  partidas  p
            JOIN  jugadores j ON j.id = p.jugador_id
            WHERE j.alias = ? COLLATE NOCASE
              AND p.dificultad = ?
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(SQL)) {
            ps.setString(1, alias);
            ps.setString(2, dificultad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getObject(1) != null)
                    return Optional.of(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando mejor puntuación", e);
        }
        return Optional.empty();
    }

    /** Número total de partidas en la base de datos. */
    public int getTotalPartidas() {
        return countSql("SELECT COUNT(*) FROM partidas");
    }

    /** Número de victorias totales en la base de datos. */
    public int getTotalVictorias() {
        return countSql("SELECT COUNT(*) FROM partidas WHERE resultado = 'VICTORIA'");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RANKING  (vista agregada)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Top-N del ranking global o filtrado por dificultad.
     *
     * @param dificultad  null o "" → todas las dificultades
     * @param limit       máximo de filas; -1 → sin límite
     */
    public List<RankingEntry> getRanking(String dificultad, int limit) {
        boolean filter = dificultad != null && !dificultad.isBlank();
        String sql = """
            SELECT alias, dificultad, mejor_puntuacion, mejor_tiempo,
                   partidas_totales, victorias
            FROM  ranking
        """ + (filter ? "WHERE dificultad = ? " : "")
          + "ORDER BY mejor_puntuacion DESC"
          + limitClause(limit);

        List<RankingEntry> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            if (filter) ps.setString(1, dificultad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer mejorTiempo = rs.getObject("mejor_tiempo") != null
                        ? rs.getInt("mejor_tiempo") : null;
                    list.add(new RankingEntry(
                        rs.getString("alias"),
                        rs.getString("dificultad"),
                        rs.getInt   ("mejor_puntuacion"),
                        mejorTiempo,
                        rs.getInt   ("partidas_totales"),
                        rs.getInt   ("victorias")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando el ranking", e);
        }
        return list;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ═════════════════════════════════════════════════════════════════════════

    private String limitClause(int limit) {
        return limit > 0 ? " LIMIT " + limit : "";
    }

    private List<GameRecord> fetchRecords(String sql) {
        List<GameRecord> list = new ArrayList<>();
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando consulta de partidas", e);
        }
        return list;
    }

    private List<GameRecord> fetchRecordsParam(String sql, String param) {
        List<GameRecord> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando consulta de partidas (param)", e);
        }
        return list;
    }

    private GameRecord mapRow(ResultSet rs) throws SQLException {
        long   id      = rs.getLong  ("id");
        Long   jid     = rs.getObject("jugador_id") != null ? rs.getLong("jugador_id") : null;
        String alias   = rs.getString("alias");          // puede ser null (LEFT JOIN)
        String diff    = rs.getString("dificultad");
        String res     = rs.getString("resultado");
        int    tiempo  = rs.getInt   ("tiempo_segundos");
        int    rev     = rs.getInt   ("casillas_reveladas");
        int    band    = rs.getInt   ("banderas_colocadas");
        int    score   = rs.getInt   ("puntuacion");
        int    filas   = rs.getInt   ("filas");
        int    cols    = rs.getInt   ("columnas");
        int    minas   = rs.getInt   ("total_minas");

        String dtStr   = rs.getString("jugada_en");
        LocalDateTime dt = dtStr != null
            ? LocalDateTime.parse(dtStr.replace(' ', 'T'))
            : LocalDateTime.now();

        return new GameRecord(id, jid, alias, diff, res,
            tiempo, rev, band, score, filas, cols, minas, dt);
    }

    private int countSql(String sql) {
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando COUNT", e);
        }
    }
}