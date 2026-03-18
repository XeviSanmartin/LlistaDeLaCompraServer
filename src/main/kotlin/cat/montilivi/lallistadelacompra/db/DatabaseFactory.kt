package cat.montilivi.lallistadelacompra.db

import cat.montilivi.cat.montilivi.lallistadelacompra.db.Categories
import cat.montilivi.cat.montilivi.lallistadelacompra.db.LlistesDeLaCompra
import cat.montilivi.cat.montilivi.lallistadelacompra.db.Productes
import cat.montilivi.cat.montilivi.lallistadelacompra.db.ProductesDeLaLlista
import cat.montilivi.cat.montilivi.lallistadelacompra.db.Usuaris
import cat.montilivi.cat.montilivi.lallistadelacompra.db.UsuarisAmics
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger("DatabaseFactory")

    fun init() {
        // Configuració de SQLite. 'llistes_de_la_compra.db' es crearà a l'arrel del projecte.
        val jdbcURL = "jdbc:sqlite:./llistes_de_la_compra.db"
        //Aquesta línia donava error de classnotfound
        //val database = Database.connect(jdbcURL, "org.xerial.sqlite.JDBC")
        val database = Database.connect(jdbcURL, driver = "org.xerial.sqlite.JDBC")

        // Activa el suport de claus foranes (Foreign Keys) en SQLite (important!)
        transaction(database) {
            exec("PRAGMA foreign_keys = ON;")

            // Crea les taules si no existeixen o les actualitza (si pot)
            SchemaUtils.create(
                Usuaris,
                Categories,
                Productes,
                LlistesDeLaCompra,
                ProductesDeLaLlista,
                UsuarisAmics
            )
            logger.info("Base de dades SQLite inicialitzada correctament.")
        }
    }

    /**
     * Helper per a consultes asíncrones.
     * Totes les crides a la BD s'han d'embolicar en un dbQuery { ... }
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}