package cat.montilivi.lallistadelacompra.db

import org.jetbrains.exposed.sql.Table

/**
 * TAULA D'USUARIS
 * Guardem la informació bàsica per al login.
 */
object Usuaris : Table("usuaris") {
    val idUsuari = integer("id_usuari").autoIncrement()
    val alias = varchar("alias", 30).nullable()
    val nomusuari = varchar("nomusuari", 50).uniqueIndex()
    val password = varchar("password", 128) // Guardarem el hash, no el text pla


    override val primaryKey = PrimaryKey(idUsuari)
}
/**
 * TAULA DE CATEGORIES DISPONIBLES PER CLASSIFICAR ELS PRODUCTES
 */
object Categories : Table("categories") {
    val idCategoria = integer("id_categoria").autoIncrement()
    val nomCategoria = varchar("nom_producte", 100).uniqueIndex()

    override val primaryKey = PrimaryKey(idCategoria)
}
/**
 * TAULA DE PRODUCTES DISPONIBLES PER A COMPRAR
 */
object Productes : Table("productes") {
    val idProducte = integer("id_producte").autoIncrement()
    val nomProducte = varchar("nom_producte", 100).uniqueIndex()
    val idCategoria = integer("id_categoria") references Categories.idCategoria

    override val primaryKey = PrimaryKey(idProducte)
}
/**
 * TAULA DE LLISTES DE LA COMPRA
 */
object LlistesDeLaCompra : Table("llistes_de_compra") {
    val idLlista = integer("id_llista").autoIncrement()
    val nomLlista = varchar("nom", 100)

    override val primaryKey = PrimaryKey(idLlista)
}


/**
 * RELACIO LLISTA -> PROPIETARIS
 */
object LlistesPropietaris : Table("llistes_propietaris") {
    val idLlista = integer("id_llista") references LlistesDeLaCompra.idLlista
    val idUsuari = integer("id_usuari") references Usuaris.idUsuari

    override val primaryKey = PrimaryKey(idLlista, idUsuari)
}

/**
 * TAULA D'ITEMS DINS LA LLISTA
 * Aquí és on es produeix el "tatxat".
 */
object ProductesDeLaLlista : Table("productes_de_llista") {
    val idProducte = integer("id_producte").autoIncrement()
    val idLlista = integer("id_llista") references LlistesDeLaCompra.idLlista
    val quantitat = integer("quantitat").default(1)
    val unitat = varchar("unitat", 30).default("unitats")
    val estaComprat = bool("esta_comprat").default(false) // FALSE = pendent, TRUE = tatxat
    val quiHaComprat = integer("qui_ha_comprat").references(Usuaris.idUsuari).nullable()

    override val primaryKey = PrimaryKey(idProducte)
}

/**
 * TAULA D'AMICS (Relació molts-a-molts)
 * Defineix qui és amic de qui per poder compartir llistes.
 */
object UsuarisAmics : Table("usuaris_amics") {
    val idUsuari = integer("id_usuari") references Usuaris.idUsuari
    val idAmic = integer("id_amic") references Usuaris.idUsuari

    // Evitem duplicats: la parella (A, B) és única
    override val primaryKey = PrimaryKey(idUsuari, idAmic)
}
