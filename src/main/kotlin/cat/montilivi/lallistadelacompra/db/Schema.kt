package cat.montilivi.cat.montilivi.lallistadelacompra.db

import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

/**
 * TAULA D'USUARIS
 * Guardem la informació bàsica per al login.
 */
object Usuaris : Table("usuaris") {
    val id = integer("id").autoIncrement()
    val alias = varchar("alias", 30).nullable()
    val nomusuari = varchar("nomusuari", 50).uniqueIndex()
    val password = varchar("password", 128) // Guardarem el hash, no el text pla


    override val primaryKey = PrimaryKey(id)
}
/**
 * TAULA DE CATEGORIES DISPONIBLES PER CLASSIFICAR ELS PRODUCTES
 */
object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val nomCategoria = varchar("nom_producte", 100)

    override val primaryKey = PrimaryKey(id)
}
/**
 * TAULA DE PRODUCTES DISPONIBLES PER A COMPRAR
 */
object Productes : Table("productes") {
    val id = integer("id").autoIncrement()
    val nomProducte = varchar("nom_producte", 100)
    val idCategoria = integer("id_categoria") references Categories.id

    override val primaryKey = PrimaryKey(id)
}
/**
 * TAULA DE LLISTES DE LA COMPRA
 * Una llista pertany a un propietari, però pot ser vista per altres.
 */
object LlistesDeLaCompra : Table("llistes_de_compra") {
    val id = integer("id").autoIncrement()
    val nomLlista = varchar("nom", 100)
    val idPropietari = integer("id_propietari") references Usuaris.id

    override val primaryKey = PrimaryKey(id)
}

/**
 * TAULA D'ITEMS DINS LA LLISTA
 * Aquí és on es produeix el "tatxat".
 */
object ProductesDeLaLlista : Table("productes_de_llista") {
    val id = integer("id").autoIncrement()
    val idLlista = integer("id_llista") references LlistesDeLaCompra.id
    val nomProducte = varchar("nom_producte", 100)
    val quantitat = integer("quantitat").default(1)
    val unitat = varchar("unitat", 30).default("unitats")
    val estaComprat = bool("esta_comprat").default(false) // FALSE = pendent, TRUE = tatxat
    val quiHaComprat = integer("qui_ha_comprat").references(Usuaris.id).nullable()

    override val primaryKey = PrimaryKey(id)
}

/**
 * TAULA D'AMICS (Relació molts-a-molts)
 * Defineix qui és amic de qui per poder compartir llistes.
 */
object UsuarisAmics : Table("usuaris_amics") {
    val idUsuari = integer("id_usuari") references Usuaris.id
    val idAmic = integer("id_amic") references Usuaris.id

    // Evitem duplicats: la parella (A, B) és única
    override val primaryKey = PrimaryKey(idUsuari, idAmic)
}