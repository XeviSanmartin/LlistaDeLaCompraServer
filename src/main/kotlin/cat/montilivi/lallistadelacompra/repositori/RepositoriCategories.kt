package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.db.Categories
import cat.montilivi.lallistadelacompra.db.DatabaseFactory.dbQuery
import cat.montilivi.lallistadelacompra.model.bbdd.Categoria
import cat.montilivi.lallistadelacompra.model.eines.CampActualitzable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


object RepositoriCategories {
    
    suspend fun creaCategoria(nomCategoria: String): Categoria? = dbQuery {
        val insertStatement = Categories.insert {
            it[Categories.nomCategoria] = nomCategoria
        }
        insertStatement.resultedValues?.singleOrNull()?.toCategoria()
    }
    
    suspend fun cercaCategoriaPerId(id: Int): Categoria? = dbQuery {
        Categories.selectAll().where { Categories.idCategoria eq id }
            .map { it.toCategoria() }
            .singleOrNull()
    }
    
    suspend fun cercaCategoriaPerNom(nomCategoria: String): Categoria? = dbQuery {
        Categories.selectAll().where { Categories.nomCategoria eq nomCategoria }
            .map { it.toCategoria() }
            .singleOrNull()
    }
    
    suspend fun obtenTotes(): List<Categoria> = dbQuery {
        Categories.selectAll().map { it.toCategoria() }
    }
    
    // Versió amb camp individual
    suspend fun actualitzaNomCategoria(id: Int, nomCategoria: String): Boolean = dbQuery {
        Categories.update({ Categories.idCategoria eq id }) {
            it[Categories.nomCategoria] = nomCategoria
        } > 0
    }
    
    // Versió amb paràmetres opcionals
    suspend fun actualitzaCategoria(
        id: Int,
        nomCategoria: CampActualitzable<String> = CampActualitzable.SenseCanvi
    ): Boolean = dbQuery {
        if (nomCategoria !is CampActualitzable.NouValor) return@dbQuery false
        
        Categories.update({ Categories.idCategoria eq id }) {
            when (nomCategoria) {
                is CampActualitzable.NouValor -> it[Categories.nomCategoria] = nomCategoria.valor
                CampActualitzable.SenseCanvi -> Unit
            }
        } > 0
    }
    
    suspend fun eliminaCategoria(id: Int): Boolean = dbQuery {
        Categories.deleteWhere { Categories.idCategoria eq id } > 0
    }
    
    private fun ResultRow.toCategoria(): Categoria {
        return Categoria(
            id = this[Categories.idCategoria],
            nomCategoria = this[Categories.nomCategoria]
        )
    }
}

