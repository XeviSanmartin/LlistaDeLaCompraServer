package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.db.LlistesDeLaCompra
import cat.montilivi.lallistadelacompra.db.DatabaseFactory.dbQuery
import cat.montilivi.lallistadelacompra.model.LlistaDeLaCompra
import cat.montilivi.lallistadelacompra.model.CampActualitzable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


object RepositoriLlistaDeLaCompra {
    
    suspend fun creaLlista(nomLlista: String, idPropietari: Int): LlistaDeLaCompra? = dbQuery {
        val insertStatement = LlistesDeLaCompra.insert {
            it[LlistesDeLaCompra.nomLlista] = nomLlista
            it[LlistesDeLaCompra.idPropietari] = idPropietari
        }
        insertStatement.resultedValues?.singleOrNull()?.toLlistaDeLaCompra()
    }
    
    suspend fun cercaLlistaPerId(id: Int): LlistaDeLaCompra? = dbQuery {
        LlistesDeLaCompra.selectAll().where { LlistesDeLaCompra.id eq id }
            .map { it.toLlistaDeLaCompra() }
            .singleOrNull()
    }
    
    suspend fun cercaLlistaPerNom(nomLlista: String): LlistaDeLaCompra? = dbQuery {
        LlistesDeLaCompra.selectAll().where { LlistesDeLaCompra.nomLlista eq nomLlista }
            .map { it.toLlistaDeLaCompra() }
            .singleOrNull()
    }
    
    suspend fun cercaLlistesPerPropietari(idPropietari: Int): List<LlistaDeLaCompra> = dbQuery {
        LlistesDeLaCompra.selectAll().where { LlistesDeLaCompra.idPropietari eq idPropietari }
            .map { it.toLlistaDeLaCompra() }
    }
    
    suspend fun obtenTotes(): List<LlistaDeLaCompra> = dbQuery {
        LlistesDeLaCompra.selectAll().map { it.toLlistaDeLaCompra() }
    }
    
    // Versió amb camps individuals
    suspend fun actualitzaNomLlista(id: Int, nomLlista: String): Boolean = dbQuery {
        LlistesDeLaCompra.update({ LlistesDeLaCompra.id eq id }) {
            it[LlistesDeLaCompra.nomLlista] = nomLlista
        } > 0
    }
    
    suspend fun actualitzaPropietariLlista(id: Int, idPropietari: Int): Boolean = dbQuery {
        LlistesDeLaCompra.update({ LlistesDeLaCompra.id eq id }) {
            it[LlistesDeLaCompra.idPropietari] = idPropietari
        } > 0
    }
    
    // Versió amb paràmetres opcionals
    suspend fun actualitzaLlista(
        id: Int,
        nomLlista: CampActualitzable<String> = CampActualitzable.SenseCanvi,
        idPropietari: CampActualitzable<Int> = CampActualitzable.SenseCanvi
    ): Boolean = dbQuery {
        val hiHaCanvis =
            nomLlista !is CampActualitzable.SenseCanvi ||
            idPropietari !is CampActualitzable.SenseCanvi

        if (!hiHaCanvis) return@dbQuery false

        LlistesDeLaCompra.update({ LlistesDeLaCompra.id eq id }) {
            when (nomLlista) {
                is CampActualitzable.NouValor -> it[LlistesDeLaCompra.nomLlista] = nomLlista.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (idPropietari) {
                is CampActualitzable.NouValor -> it[LlistesDeLaCompra.idPropietari] = idPropietari.valor
                CampActualitzable.SenseCanvi -> Unit
            }
        } > 0
    }
    
    suspend fun eliminaLlista(id: Int): Boolean = dbQuery {
        LlistesDeLaCompra.deleteWhere { LlistesDeLaCompra.id eq id } > 0
    }
    
    private fun ResultRow.toLlistaDeLaCompra(): LlistaDeLaCompra {
        return LlistaDeLaCompra(
            id = this[LlistesDeLaCompra.id],
            nomLlista = this[LlistesDeLaCompra.nomLlista],
            idPropietari = this[LlistesDeLaCompra.idPropietari]
        )
    }
}

