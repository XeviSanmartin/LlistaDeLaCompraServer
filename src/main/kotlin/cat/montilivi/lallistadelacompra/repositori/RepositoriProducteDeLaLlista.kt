package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.db.ProductesDeLaLlista
import cat.montilivi.lallistadelacompra.db.DatabaseFactory.dbQuery
import cat.montilivi.lallistadelacompra.model.ProducteDeLaLlista
import cat.montilivi.lallistadelacompra.model.CampActualitzable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and


object RepositoriProducteDeLaLlista {
    
    suspend fun creaProducte(
        idLlista: Int,
        nomProducte: String,
        quantitat: Int = 1,
        unitat: String = "unitats",
        estatComprat: Boolean = false,
        quiHaComprat: Int = 0
    ): ProducteDeLaLlista? = dbQuery {
        val insertStatement = ProductesDeLaLlista.insert {
            it[ProductesDeLaLlista.idLlista] = idLlista
            it[ProductesDeLaLlista.nomProducte] = nomProducte
            it[ProductesDeLaLlista.quantitat] = quantitat
            it[ProductesDeLaLlista.unitat] = unitat
            it[ProductesDeLaLlista.estaComprat] = estatComprat
            it[ProductesDeLaLlista.quiHaComprat] = quiHaComprat
        }
        insertStatement.resultedValues?.singleOrNull()?.toProducteDeLaLlista()
    }
    
    suspend fun cercaProductePerId(id: Int): ProducteDeLaLlista? = dbQuery {
        ProductesDeLaLlista.selectAll().where { ProductesDeLaLlista.id eq id }
            .map { it.toProducteDeLaLlista() }
            .singleOrNull()
    }
    
    suspend fun cercaProductesPerLlista(idLlista: Int): List<ProducteDeLaLlista> = dbQuery {
        ProductesDeLaLlista.selectAll().where { ProductesDeLaLlista.idLlista eq idLlista }
            .map { it.toProducteDeLaLlista() }
    }
    
    suspend fun cercaProductesPerLlistaNoComprats(idLlista: Int): List<ProducteDeLaLlista> = dbQuery {
        ProductesDeLaLlista.selectAll().where { 
            (ProductesDeLaLlista.idLlista eq idLlista) and (ProductesDeLaLlista.estaComprat eq false)
        }
            .map { it.toProducteDeLaLlista() }
    }
    
    suspend fun cercaProductesPerLlistaComprats(idLlista: Int): List<ProducteDeLaLlista> = dbQuery {
        ProductesDeLaLlista.selectAll().where { 
            (ProductesDeLaLlista.idLlista eq idLlista) and (ProductesDeLaLlista.estaComprat eq true)
        }
            .map { it.toProducteDeLaLlista() }
    }
    
    suspend fun obtenTots(): List<ProducteDeLaLlista> = dbQuery {
        ProductesDeLaLlista.selectAll().map { it.toProducteDeLaLlista() }
    }
    
    // Versió amb camps individuals
    suspend fun actualitzaNomProducte(id: Int, nomProducte: String): Boolean = dbQuery {
        ProductesDeLaLlista.update({ ProductesDeLaLlista.id eq id }) {
            it[ProductesDeLaLlista.nomProducte] = nomProducte
        } > 0
    }
    
    suspend fun actualitzaQuantitat(id: Int, quantitat: Int): Boolean = dbQuery {
        ProductesDeLaLlista.update({ ProductesDeLaLlista.id eq id }) {
            it[ProductesDeLaLlista.quantitat] = quantitat
        } > 0
    }
    
    suspend fun actualitzaUnitat(id: Int, unitat: String): Boolean = dbQuery {
        ProductesDeLaLlista.update({ ProductesDeLaLlista.id eq id }) {
            it[ProductesDeLaLlista.unitat] = unitat
        } > 0
    }
    
    suspend fun actualitzaEstatComprat(id: Int, estatComprat: Boolean, quiHaComprat: Int = 0): Boolean = dbQuery {
        ProductesDeLaLlista.update({ ProductesDeLaLlista.id eq id }) {
            it[ProductesDeLaLlista.estaComprat] = estatComprat
            it[ProductesDeLaLlista.quiHaComprat] = quiHaComprat
        } > 0
    }
    
    // Versió amb paràmetres opcionals
    suspend fun actualitzaProducte(
        id: Int,
        nomProducte: CampActualitzable<String> = CampActualitzable.SenseCanvi,
        quantitat: CampActualitzable<Int> = CampActualitzable.SenseCanvi,
        unitat: CampActualitzable<String> = CampActualitzable.SenseCanvi,
        estatComprat: CampActualitzable<Boolean> = CampActualitzable.SenseCanvi,
        quiHaComprat: CampActualitzable<Int> = CampActualitzable.SenseCanvi
    ): Boolean = dbQuery {
        val hiHaCanvis =
            nomProducte !is CampActualitzable.SenseCanvi ||
            quantitat !is CampActualitzable.SenseCanvi ||
            unitat !is CampActualitzable.SenseCanvi ||
            estatComprat !is CampActualitzable.SenseCanvi ||
            quiHaComprat !is CampActualitzable.SenseCanvi

        if (!hiHaCanvis) return@dbQuery false

        ProductesDeLaLlista.update({ ProductesDeLaLlista.id eq id }) {
            when (nomProducte) {
                is CampActualitzable.NouValor -> it[ProductesDeLaLlista.nomProducte] = nomProducte.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (quantitat) {
                is CampActualitzable.NouValor -> it[ProductesDeLaLlista.quantitat] = quantitat.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (unitat) {
                is CampActualitzable.NouValor -> it[ProductesDeLaLlista.unitat] = unitat.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (estatComprat) {
                is CampActualitzable.NouValor -> it[ProductesDeLaLlista.estaComprat] = estatComprat.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (quiHaComprat) {
                is CampActualitzable.NouValor -> it[ProductesDeLaLlista.quiHaComprat] = quiHaComprat.valor
                CampActualitzable.SenseCanvi -> Unit
            }
        } > 0
    }
    
    suspend fun eliminaProducte(id: Int): Boolean = dbQuery {
        ProductesDeLaLlista.deleteWhere { ProductesDeLaLlista.id eq id } > 0
    }
    
    suspend fun eliminaProductesPerLlista(idLlista: Int): Boolean = dbQuery {
        ProductesDeLaLlista.deleteWhere { ProductesDeLaLlista.idLlista eq idLlista } > 0
    }
    
    private fun ResultRow.toProducteDeLaLlista(): ProducteDeLaLlista {
        return ProducteDeLaLlista(
            id = this[ProductesDeLaLlista.id],
            idLLista = this[ProductesDeLaLlista.idLlista],
            nomProducte = this[ProductesDeLaLlista.nomProducte],
            quenatitat = this[ProductesDeLaLlista.quantitat],
            unitat = this[ProductesDeLaLlista.unitat],
            estatComprat = this[ProductesDeLaLlista.estaComprat],
            quiHaComprat = this[ProductesDeLaLlista.quiHaComprat] ?: 0
        )
    }
}

