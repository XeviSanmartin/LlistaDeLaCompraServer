package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.db.DatabaseFactory.dbQuery
import cat.montilivi.lallistadelacompra.db.LlistesDeLaCompra
import cat.montilivi.lallistadelacompra.db.LlistesPropietaris
import cat.montilivi.lallistadelacompra.db.ProductesDeLaLlista
import cat.montilivi.lallistadelacompra.model.CampActualitzable
import cat.montilivi.lallistadelacompra.model.LlistaDeLaCompra
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object RepositoriLlistesDeLaCompra {

    suspend fun creaLlista(nomLlista: String, idPropietari: Int): LlistaDeLaCompra? =
        creaLlista(nomLlista, listOf(idPropietari))

    suspend fun creaLlista(nomLlista: String, idsPropietaris: List<Int>): LlistaDeLaCompra? = dbQuery{
        if (idsPropietaris.isEmpty()) return@dbQuery null

        val insertStatement = LlistesDeLaCompra.insert {
            it[LlistesDeLaCompra.nomLlista] = nomLlista
        }
        val fila = insertStatement.resultedValues?.singleOrNull() ?: return@dbQuery null
        val idLlista = fila[LlistesDeLaCompra.id]
        val propietaris = idsPropietaris.distinct()

        replacePropietarisIntern(idLlista, propietaris)

        fila.toLlistaDeLaCompra()
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
        val idsLlistes = LlistesPropietaris.selectAll()
            .where { LlistesPropietaris.idUsuari eq idPropietari }
            .map { it[LlistesPropietaris.idLlista] }

        if (idsLlistes.isEmpty()) return@dbQuery emptyList()

        LlistesDeLaCompra.selectAll()
            .where { LlistesDeLaCompra.id inList idsLlistes }
            .map { it.toLlistaDeLaCompra() }
    }

    suspend fun obtenTotes(): List<LlistaDeLaCompra> = dbQuery {
        LlistesDeLaCompra.selectAll().map { it.toLlistaDeLaCompra() }
    }

    // Versio amb camps individuals
    suspend fun actualitzaNomLlista(idLlista: Int, nomLlista: String, idUsuari: Int): Boolean = dbQuery {
        val teAcces = LlistesPropietaris.select (
            (LlistesPropietaris.idLlista eq idLlista) and (LlistesPropietaris.idUsuari eq idUsuari)
        ).any()
        if (teAcces)
        {
            LlistesDeLaCompra.update({ (LlistesDeLaCompra.id eq idLlista) }) {
                it[LlistesDeLaCompra.nomLlista] = nomLlista
            } > 0
        }
        else
            false
    }

    suspend fun actualitzaPropietariLlista(id: Int, idPropietari: Int): Boolean = dbQuery {
        actualitzaPropietarisLlista(id, listOf(idPropietari))
    }

    suspend fun actualitzaPropietarisLlista(id: Int, idsPropietaris: List<Int>): Boolean = dbQuery {
        if (idsPropietaris.isEmpty()) return@dbQuery false

        val propietaris = idsPropietaris.distinct()
        replacePropietarisIntern(id, propietaris)
        true
    }

    // Versio amb parametres opcionals
    suspend fun actualitzaLlista(
        id: Int,
        nomLlista: CampActualitzable<String> = CampActualitzable.SenseCanvi,
        idsPropietaris: CampActualitzable<List<Int>> = CampActualitzable.SenseCanvi
    ): Boolean = dbQuery {
        val hiHaCanvis =
            nomLlista !is CampActualitzable.SenseCanvi ||
            idsPropietaris !is CampActualitzable.SenseCanvi

        if (!hiHaCanvis) return@dbQuery false

        val nomActualitzat = LlistesDeLaCompra.update({ LlistesDeLaCompra.id eq id }) {
            when (nomLlista) {
                is CampActualitzable.NouValor -> it[LlistesDeLaCompra.nomLlista] = nomLlista.valor
                CampActualitzable.SenseCanvi -> Unit
            }
        } > 0

        val propietarisActualitzats = when (idsPropietaris) {
            is CampActualitzable.NouValor -> {
                if (idsPropietaris.valor.isEmpty()) return@dbQuery false
                val propietaris = idsPropietaris.valor.distinct()
                replacePropietarisIntern(id, propietaris)
                true
            }
            CampActualitzable.SenseCanvi -> false
        }

        nomActualitzat || propietarisActualitzats
    }

    suspend fun eliminaLlista(idLlista: Int, idUsuari:Int): Boolean = dbQuery {
        val teAcces = LlistesPropietaris.select (
            (LlistesPropietaris.idLlista eq idLlista) and (LlistesPropietaris.idUsuari eq idUsuari)
        ).any()
        if (teAcces) {
            LlistesPropietaris.deleteWhere { LlistesPropietaris.idLlista eq idLlista }
            ProductesDeLaLlista.deleteWhere { ProductesDeLaLlista.idLlista eq idLlista }
            LlistesDeLaCompra.deleteWhere { LlistesDeLaCompra.id eq idLlista } > 0
        }
        else
            false
    }

    private fun replacePropietarisIntern(idLlista: Int, idsPropietaris: List<Int>) {
        LlistesPropietaris.deleteWhere { LlistesPropietaris.idLlista eq idLlista }
        idsPropietaris.forEach { idUsuari ->
            LlistesPropietaris.insert {
                it[LlistesPropietaris.idLlista] = idLlista
                it[LlistesPropietaris.idUsuari] = idUsuari
            }
        }
    }

    private fun ResultRow.toLlistaDeLaCompra(): LlistaDeLaCompra {
        val idLlista = this[LlistesDeLaCompra.id]
        val idsPropietaris = LlistesPropietaris.selectAll()
            .where { LlistesPropietaris.idLlista eq idLlista }
            .map { it[LlistesPropietaris.idUsuari] }

        return LlistaDeLaCompra(
            id = idLlista,
            nomLlista = this[LlistesDeLaCompra.nomLlista],
            idsPropietaris = idsPropietaris
        )
    }
}

