package cat.montilivi.lallistadelacompra.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import cat.montilivi.lallistadelacompra.utils.EncriptadorDePasswords
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import cat.montilivi.lallistadelacompra.repositori.RepositoriCategories
import cat.montilivi.lallistadelacompra.repositori.RepositoriProductes
import cat.montilivi.lallistadelacompra.repositori.RepositoriLlistaDeLaCompra
import cat.montilivi.lallistadelacompra.repositori.RepositoriProducteDeLaLlista
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger("DatabaseFactory")

    fun init() {
        // Configuració de SQLite. 'llistes_de_la_compra.db' es crearà a l'arrel del projecte.
        val jdbcURL = "jdbc:sqlite:./llistes_de_la_compra.db"
        val database = Database.connect(jdbcURL, driver = "org.sqlite.JDBC")

        // Activa el suport de claus foranes (Foreign Keys) en SQLite (important!)
        transaction(database) {
            exec("PRAGMA foreign_keys = ON;", emptyList(), StatementType.OTHER)

            // Crea les taules si no existeixen o les actualitza (si pot)
            SchemaUtils.create(
                Usuaris,
                Categories,
                Productes,
                LlistesDeLaCompra,
                LlistesPropietaris,
                ProductesDeLaLlista,
                UsuarisAmics
            )
            logger.info("Base de dades SQLite inicialitzada correctament.")
        }
    }

    fun poblaLaBBDD() {
        transaction {
            // USUARIS
            if (Usuaris.selectAll().empty()) {
                val usuarisData = listOf(
                    Triple("admin",    "admin",    null),
                    Triple("joan",     "joan",     "Joaniki"),
                    Triple("maria",    "maria",    "Mary"),
                    Triple("pere",     "pere",     null),
                    Triple("anna",     "anna",     "Anuska"),
                )
                val ids = usuarisData.map { (nomUsuari, _, alias) ->
                    Usuaris.insert {
                        it[nomusuari] = nomUsuari
                        it[password]  = EncriptadorDePasswords.hash(nomUsuari)
                        it[Usuaris.alias] = alias
                    } get Usuaris.id
                }
                logger.info("Seed: ${ids.size} usuaris inserits.")

                // AMICS (relacions recíproques)
                listOf(
                    ids[1] to ids[2],  // joan <-> maria
                    ids[2] to ids[1],
                    ids[1] to ids[3],  // joan <-> pere
                    ids[3] to ids[1],
                    ids[2] to ids[4],  // maria <-> anna
                    ids[4] to ids[2],
                ).forEach { (idUsuari, idAmic) ->
                    UsuarisAmics.insert {
                        it[UsuarisAmics.idUsuari] = idUsuari
                        it[UsuarisAmics.idAmic]   = idAmic
                    }
                }
                logger.info("Seed: amistats inserides.")
            }

            // CATEGORIES
            if (Categories.selectAll().empty()) {
                val categories = listOf(
                    "Altres", "Fruites", "Verdures", "Carns", "Peixos", "Lactis", "Begudes", "Cereals"
                )
                categories.forEach { nom ->
                    Categories.insert { it[nomCategoria] = nom }
                }
                logger.info("Seed: ${categories.size} categories inserides.")
            }

            // PRODUCTES
            if (Productes.selectAll().empty()) {
                // id de categoria: 1=Fruites, 2=Verdures, 3=Carns, 4=Peixos, 5=Lactis, 6=Begudes, 7=Cereals, 0=Altres
                val productes = listOf(
                    "Poma"        to 2,
                    "Plàtan"      to 2,
                    "Taronja"     to 2,
                    "Tomàquet"    to 3,
                    "Enciam"      to 3,
                    "Pastanaga"   to 3,
                    "Pit de pollastre" to 4,
                    "Llom de porc" to 4,
                    "Salmó"       to 5,
                    "Llet"        to 6,
                    "Iogurt"      to 6,
                    "Formatge"    to 6,
                    "Aigua"       to 7,
                    "Suc de taronja" to 7,
                    "Pa"          to 8,
                    "Arròs"       to 8,
                )
                productes.forEach { (nom, idCat) ->
                    Productes.insert {
                        it[nomProducte] = nom
                        it[idCategoria] = idCat
                    }
                }
                logger.info("Seed: ${productes.size} productes inserits.")
            }

            // LLISTES DE LA COMPRA
            if (LlistesDeLaCompra.selectAll().empty()) {
                val idJoan  = Usuaris.selectAll().where { Usuaris.nomusuari eq "joan"  }.first()[Usuaris.id]
                val idMaria = Usuaris.selectAll().where { Usuaris.nomusuari eq "maria" }.first()[Usuaris.id]

                val idLlista1 = LlistesDeLaCompra.insert {
                    it[nomLlista]    = "Compra setmanal Joan"
                } get LlistesDeLaCompra.id

                val idLlista2 = LlistesDeLaCompra.insert {
                    it[nomLlista]    = "Festa d'aniversari"
                } get LlistesDeLaCompra.id

                LlistesPropietaris.insert {
                    it[LlistesPropietaris.idLlista] = idLlista1
                    it[LlistesPropietaris.idUsuari] = idJoan
                }
                LlistesPropietaris.insert {
                    it[LlistesPropietaris.idLlista] = idLlista1
                    it[LlistesPropietaris.idUsuari] = idMaria
                }
                LlistesPropietaris.insert {
                    it[LlistesPropietaris.idLlista] = idLlista2
                    it[LlistesPropietaris.idUsuari] = idMaria
                }

                logger.info("Seed: 2 llistes inserides.")

                // PRODUCTES DE LA LLISTA
                if (ProductesDeLaLlista.selectAll().empty()) {
                    listOf(
                        Triple("Poma",       2, "kg"),
                        Triple("Llet",       3, "brics"),
                        Triple("Pa",         1, "barres"),
                        Triple("Iogurt",     4, "unitats"),
                        Triple("Tomàquet",   1, "kg"),
                    ).forEach { (nom, qty, unit) ->
                        ProductesDeLaLlista.insert {
                            it[idLlista]    = idLlista1
                            it[nomProducte] = nom
                            it[quantitat]   = qty
                            it[unitat]      = unit
                            it[estaComprat] = false
                            it[quiHaComprat] = null
                        }
                    }
                    listOf(
                        Triple("Salmó",       2, "unitats"),
                        Triple("Arròs",       1, "kg"),
                        Triple("Suc de taronja", 2, "ampolles"),
                    ).forEach { (nom, qty, unit) ->
                        ProductesDeLaLlista.insert {
                            it[idLlista]    = idLlista2
                            it[nomProducte] = nom
                            it[quantitat]   = qty
                            it[unitat]      = unit
                            it[estaComprat] = false
                            it[quiHaComprat] = null
                        }
                    }
                    logger.info("Seed: productes de les llistes inserits.")
                }
            }
        }
    }
    
    fun poblaLaBBDDUtilitzantElsRepositoris() {
        runBlocking {
            // USUARIS
            if (RepositoriUsuaris.obtenTots().isEmpty()) {
                RepositoriUsuaris.creaUsuari("admin", "admin", null)
                val joan  = RepositoriUsuaris.creaUsuari("joan",  "joan",  "Joaniki")
                val maria = RepositoriUsuaris.creaUsuari("maria", "maria", "Mary")
                val pere  = RepositoriUsuaris.creaUsuari("pere",  "pere",  null)
                val anna  = RepositoriUsuaris.creaUsuari("anna",  "anna",  "Anuska")
                logger.info("Seed: 5 usuaris inserits.")

                // AMICS (relacions recíproques)
                if (joan != null && maria != null && pere != null && anna != null) {
                    RepositoriUsuaris.afegeixAmic(joan.id,  maria.id)
                    RepositoriUsuaris.afegeixAmic(maria.id, joan.id)
                    RepositoriUsuaris.afegeixAmic(joan.id,  pere.id)
                    RepositoriUsuaris.afegeixAmic(pere.id,  joan.id)
                    RepositoriUsuaris.afegeixAmic(maria.id, anna.id)
                    RepositoriUsuaris.afegeixAmic(anna.id,  maria.id)
                    logger.info("Seed: amistats inserides.")
                }
            }

            // CATEGORIES
            if (RepositoriCategories.obtenTotes().isEmpty()) {
                listOf("Altres", "Fruites", "Verdures", "Carns", "Peixos", "Lactis", "Begudes", "Cereals")
                    .forEach { RepositoriCategories.creaCategoria(it) }
                logger.info("Seed: categories inserides.")
            }

            // PRODUCTES
            if (RepositoriProductes.obtenTots().isEmpty()) {
                listOf(
                    "Poma"             to 2,
                    "Plàtan"           to 2,
                    "Taronja"          to 2,
                    "Tomàquet"         to 3,
                    "Enciam"           to 3,
                    "Pastanaga"        to 3,
                    "Pit de pollastre" to 4,
                    "Llom de porc"     to 4,
                    "Salmó"            to 5,
                    "Llet"             to 6,
                    "Iogurt"           to 6,
                    "Formatge"         to 6,
                    "Aigua"            to 7,
                    "Suc de taronja"   to 7,
                    "Pa"               to 8,
                    "Arròs"            to 8,
                ).forEach { (nom, idCat) -> RepositoriProductes.creaProducte(nom, idCat) }
                logger.info("Seed: productes inserits.")
            }

            // LLISTES DE LA COMPRA
            if (RepositoriLlistaDeLaCompra.obtenTotes().isEmpty()) {
                val joan  = RepositoriUsuaris.cercaUsuariPerNomUsuari("joan")
                val maria = RepositoriUsuaris.cercaUsuariPerNomUsuari("maria")

                if (joan != null && maria != null) {
                    val llista1 = RepositoriLlistaDeLaCompra.creaLlista("Compra setmanal Joan", listOf(joan.id, maria.id))
                    val llista2 = RepositoriLlistaDeLaCompra.creaLlista("Festa d'aniversari",   maria.id)
                    logger.info("Seed: 2 llistes inserides.")

                    // PRODUCTES DE LA LLISTA
                    if (RepositoriProducteDeLaLlista.obtenTots().isEmpty()) {
                        if (llista1 != null) {
                            listOf(
                                Triple("Poma",     2, "kg"),
                                Triple("Llet",     3, "brics"),
                                Triple("Pa",       1, "barres"),
                                Triple("Iogurt",   4, "unitats"),
                                Triple("Tomàquet", 1, "kg"),
                            ).forEach { (nom, qty, unit) ->
                                RepositoriProducteDeLaLlista.creaProducte(llista1.id, nom, qty, unit)
                            }
                        }
                        if (llista2 != null) {
                            listOf(
                                Triple("Salmó",          2, "unitats"),
                                Triple("Arròs",          1, "kg"),
                                Triple("Suc de taronja", 2, "ampolles"),
                            ).forEach { (nom, qty, unit) ->
                                RepositoriProducteDeLaLlista.creaProducte(llista2.id, nom, qty, unit)
                            }
                        }
                        logger.info("Seed: productes de les llistes inserits.")
                    }
                }
            }
        }
    }
    /**
     * Helper per a consultes asíncrones.
     * Totes les crides a la BD s'han d'embolicar en un dbQuery { ... }
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
