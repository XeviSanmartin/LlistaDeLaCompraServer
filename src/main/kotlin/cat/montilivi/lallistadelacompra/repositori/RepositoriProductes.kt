package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.db.Productes
import cat.montilivi.lallistadelacompra.db.DatabaseFactory.dbQuery
import cat.montilivi.lallistadelacompra.model.bbdd.Producte
import cat.montilivi.lallistadelacompra.model.eines.CampActualitzable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


object RepositoriProductes {
    
    suspend fun creaProducte(nomProducte: String, idCategoria: Int): Producte? = dbQuery {
        val insertStatement = Productes.insert {
            it[Productes.nomProducte] = nomProducte
            it[Productes.idCategoria] = idCategoria
        }
        insertStatement.resultedValues?.singleOrNull()?.toProducte()
    }
    suspend fun existeixProductePerID(id:Int): Boolean = dbQuery {
        Productes.selectAll().where { Productes.idProducte eq id }
            .count() > 0
    }
    suspend fun cercaProductePerId(id: Int): Producte? = dbQuery {
        Productes.selectAll().where { Productes.idProducte eq id }
            .map { it.toProducte() }
            .singleOrNull()
    }
    
    suspend fun cercaProductePerNom(nomProducte: String): Producte? = dbQuery {
        Productes.selectAll().where { Productes.nomProducte eq nomProducte }
            .map { it.toProducte() }
            .singleOrNull()
    }
    
    suspend fun cercaProductesPerCategoria(idCategoria: Int): List<Producte> = dbQuery {
        Productes.selectAll().where { Productes.idCategoria eq idCategoria }
            .map { it.toProducte() }
    }
    
    suspend fun obtenTots(): List<Producte> = dbQuery {
        Productes.selectAll().map { it.toProducte() }
    }
    
    // Versió amb camps individuals
    suspend fun actualitzaNomProducte(id: Int, nomProducte: String): Boolean = dbQuery {
        Productes.update({ Productes.idProducte eq id }) {
            it[Productes.nomProducte] = nomProducte
        } > 0
    }
    
    suspend fun actualitzaCategoriaProducte(id: Int, idCategoria: Int): Boolean = dbQuery {
        Productes.update({ Productes.idProducte eq id }) {
            it[Productes.idCategoria] = idCategoria
        } > 0
    }
    
    // Versió amb paràmetres opcionals
    suspend fun actualitzaProducte(
        id: Int,
        nomProducte: CampActualitzable<String> = CampActualitzable.SenseCanvi,
        idCategoria: CampActualitzable<Int> = CampActualitzable.SenseCanvi
    ): Boolean = dbQuery {
        val hiHaCanvis =
            nomProducte !is CampActualitzable.SenseCanvi ||
            idCategoria !is CampActualitzable.SenseCanvi

        if (!hiHaCanvis) return@dbQuery false

        Productes.update({ Productes.idProducte eq id }) {
            when (nomProducte) {
                is CampActualitzable.NouValor -> it[Productes.nomProducte] = nomProducte.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (idCategoria) {
                is CampActualitzable.NouValor -> it[Productes.idCategoria] = idCategoria.valor
                CampActualitzable.SenseCanvi -> Unit
            }
        } > 0
    }
    
    suspend fun eliminaProducte(id: Int): Boolean = dbQuery {
        Productes.deleteWhere { Productes.idProducte eq id } > 0
    }
    
    private fun ResultRow.toProducte(): Producte {
        return Producte(
            idProducte = this[Productes.idProducte],
            nomProducte = this[Productes.nomProducte],
            idCategoria = this[Productes.idCategoria]
        )
    }
}

