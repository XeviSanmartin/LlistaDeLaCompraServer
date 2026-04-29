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
import org.jetbrains.exposed.sql.insertIgnore
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
        val idLlista = fila[LlistesDeLaCompra.idLlista]
        val propietaris = idsPropietaris.distinct()
        propietaris.forEach { idUsuari ->
            LlistesPropietaris.insert {
                it[LlistesPropietaris.idLlista] = idLlista
                it[LlistesPropietaris.idUsuari] = idUsuari
            }
        }
        fila.toLlistaDeLaCompra()
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

    suspend fun cercaLlistaPerId(id: Int): LlistaDeLaCompra? = dbQuery {
        LlistesDeLaCompra.selectAll().where { LlistesDeLaCompra.idLlista eq id }
            .map { it.toLlistaDeLaCompra() }
            .singleOrNull()
    }

    suspend fun cercaLlistaPerNom(nomLlista: String): LlistaDeLaCompra? = dbQuery {
        LlistesDeLaCompra.selectAll().where { LlistesDeLaCompra.nomLlista eq nomLlista }
            .map { it.toLlistaDeLaCompra() }
            .singleOrNull()
    }

    suspend fun cercaLlistesPerPropietari(idPropietari: Int): List<LlistaDeLaCompra> = dbQuery {
        (LlistesDeLaCompra innerJoin LlistesPropietaris)
            .selectAll()
            .where { LlistesPropietaris.idUsuari eq idPropietari }
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
            LlistesDeLaCompra.update({ (LlistesDeLaCompra.idLlista eq idLlista) }) {
                it[LlistesDeLaCompra.nomLlista] = nomLlista
            } > 0
        }
        else
            false
    }

    // Versio amb parametres opcionals
    suspend fun actualitzaLlista(
        id: Int,
        nomLlista: CampActualitzable<String> = CampActualitzable.SenseCanvi
    ): Boolean = dbQuery {
        val hiHaCanvis =
            nomLlista !is CampActualitzable.SenseCanvi

        if (!hiHaCanvis) return@dbQuery false

        val nomActualitzat = LlistesDeLaCompra.update({ LlistesDeLaCompra.idLlista eq id }) {
            when (nomLlista) {
                is CampActualitzable.NouValor -> it[LlistesDeLaCompra.nomLlista] = nomLlista.valor
                CampActualitzable.SenseCanvi -> Unit
            }
        } > 0

        nomActualitzat
    }

    suspend fun eliminaLlista(idLlista: Int, idUsuari:Int): Boolean = dbQuery {
        val teAcces = LlistesPropietaris.select (
            (LlistesPropietaris.idLlista eq idLlista) and (LlistesPropietaris.idUsuari eq idUsuari)
        ).any()
        if (teAcces) {
            LlistesPropietaris.deleteWhere { LlistesPropietaris.idLlista eq idLlista }
            ProductesDeLaLlista.deleteWhere { ProductesDeLaLlista.idLlista eq idLlista }
            LlistesDeLaCompra.deleteWhere { LlistesDeLaCompra.idLlista eq idLlista } > 0
        }
        else
            false
    }

    suspend fun existeixLlista(idLlista: Int): Boolean = dbQuery {
        LlistesDeLaCompra.selectAll().where { LlistesDeLaCompra.idLlista eq idLlista }
            .any()
    }



    suspend fun afegeixPropietariDeLlista(idUsuari: Int, idLlista: Int): Boolean = dbQuery {
        // Comprovem si la llista existeix
        val llistaExisteix = LlistesDeLaCompra
            .selectAll().where { LlistesDeLaCompra.idLlista eq idLlista }
            .any()

        if (!llistaExisteix) return@dbQuery false

        // Intentem inserir el nou propietari
        // Fem servir insertIgnore per si l'usuari ja era propietari,
        // així el servidor no petarà i simplement retornarà false.
        val inserit = LlistesPropietaris.insertIgnore {
            it[LlistesPropietaris.idUsuari] = idUsuari
            it[LlistesPropietaris.idLlista] = idLlista
        }.insertedCount > 0

        inserit
    }

    suspend fun eliminaPropietariDeLlista(idUsuari: Int, idLlista: Int): Boolean = dbQuery {

        val propietarisRestants = LlistesPropietaris
            .selectAll().where { LlistesPropietaris.idLlista eq idLlista }
            .count()

        //Si és l'únic propietari de la llista, eliminem la llista completament
        if (propietarisRestants == 1L) {
            // Primer eliminem els productes associats (si no està configurat el CASCADE a la BD)
            ProductesDeLaLlista.deleteWhere { ProductesDeLaLlista.idLlista eq idLlista }
            // Eliminem el vincle entre l'usuari i la llista
            LlistesPropietaris.deleteWhere {
                (LlistesPropietaris.idUsuari eq idUsuari) and (LlistesPropietaris.idLlista eq idLlista)
            }
            // Finalment eliminem la llista
            LlistesDeLaCompra.deleteWhere { LlistesDeLaCompra.idLlista eq idLlista }>0
        } else {
            // Eliminem el vincle entre l'usuari i la llista
            LlistesPropietaris.deleteWhere {
                (LlistesPropietaris.idUsuari eq idUsuari) and (LlistesPropietaris.idLlista eq idLlista)
            } > 0
        }
    }

    private suspend fun ResultRow.toLlistaDeLaCompra(): LlistaDeLaCompra {
        val idLlista = this[LlistesDeLaCompra.idLlista]
        val idsPropietaris = dbQuery {
            LlistesPropietaris.selectAll()
                .where { LlistesPropietaris.idLlista eq idLlista }
                .map { it[LlistesPropietaris.idUsuari] }
        }
        val idsProductes = dbQuery {
            ProductesDeLaLlista.selectAll()
                .where { ProductesDeLaLlista.idLlista eq idLlista }
                .map { it[ProductesDeLaLlista.idProducte] }
        }
        return LlistaDeLaCompra(
            id = idLlista,
            nomLlista = this[LlistesDeLaCompra.nomLlista],
            idsPropietaris = idsPropietaris,
            idsProductes = idsProductes,
        )
    }
}

