package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.db.DatabaseFactory.dbQuery
import cat.montilivi.lallistadelacompra.db.UsuarisAmics
import cat.montilivi.lallistadelacompra.db.Usuaris
import cat.montilivi.lallistadelacompra.model.CampActualitzable
import cat.montilivi.lallistadelacompra.model.Usuari
import cat.montilivi.lallistadelacompra.utils.EncriptadorDePasswords
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update




object RepositoriUsuaris {
    suspend fun creaUsuari(nom_usuari: String, password_usuari: String, alias_usuari:String?): Usuari? = dbQuery {
        val insertStatement = Usuaris.insert {
            it[nomusuari] = nom_usuari
            it[password] = EncriptadorDePasswords.hash(password_usuari)
            it[alias] = alias_usuari
        }
        insertStatement.resultedValues?.singleOrNull()?.toUsuari()
    }


    suspend fun cercaUsuariPerId(id: Int): Usuari? = dbQuery {
        Usuaris.selectAll().where { Usuaris.id eq id }
            .map { it.toUsuari() }
            .singleOrNull()
    }

    suspend fun cercaUsuariPerNomUsuari(nomUsuari: String): Usuari? = dbQuery {
        Usuaris.selectAll().where { Usuaris.nomusuari eq nomUsuari }
            .map { it.toUsuari() }
            .singleOrNull()
    }

    suspend fun cercaUsuariPerCredencials(nom_usuari: String, password_usuari: String): Usuari? = dbQuery {
//        Usuaris.selectAll().where { (Usuaris.nomusuari eq nomUsuari) and (Usuaris.password eq passwordUsuari) }
//            .map { it.toUsuari() }
//            .singleOrNull()
        val fila = Usuaris.selectAll().where { Usuaris.nomusuari eq nom_usuari }.singleOrNull()

        if (fila != null) {
            val hashedPass = fila[Usuaris.password]
            // Comparem la contrasenya enviada amb el hash de la BD
            if (EncriptadorDePasswords.check(password_usuari, hashedPass)) {
                fila.toUsuari()
            } else {
                null // Contrasenya incorrecta
            }
        } else {
            null // L'usuari no existeix
        }
    }

    suspend fun obtenTots(): List<Usuari> = dbQuery {
        Usuaris.selectAll().map { it.toUsuari() }
    }

    suspend fun actualitzaAliasUsuari(id: Int, aliasUsuari: String?): Boolean = dbQuery {
        Usuaris.update({ Usuaris.id eq id }) {
            it[alias] = aliasUsuari
        } > 0
    }

    suspend fun actualitzaPasswordUsuari(id: Int, passwordUsuari: String): Boolean = dbQuery {
        Usuaris.update({ Usuaris.id eq id }) {
            it[password] = passwordUsuari
        } > 0
    }

    suspend fun actualitzaNomUsuari(id: Int, nomUsuari: String): Boolean = dbQuery {
        Usuaris.update({ Usuaris.id eq id }) {
            it[nomusuari] = nomUsuari
        } > 0
    }

    suspend fun actualitzaUsuari(
        id: Int,
        aliasUsuari: CampActualitzable<String?> = CampActualitzable.SenseCanvi,
        nomUsuari: CampActualitzable<String> = CampActualitzable.SenseCanvi,
        passwordUsuari: CampActualitzable<String> = CampActualitzable.SenseCanvi
    ): Boolean = dbQuery {
        val hiHaCanvis =
            aliasUsuari !is CampActualitzable.SenseCanvi ||
            nomUsuari !is CampActualitzable.SenseCanvi ||
            passwordUsuari !is CampActualitzable.SenseCanvi

        if (!hiHaCanvis) return@dbQuery false

        Usuaris.update({ Usuaris.id eq id }) {
            when (aliasUsuari) {
                is CampActualitzable.NouValor -> it[alias] = aliasUsuari.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (nomUsuari) {
                is CampActualitzable.NouValor -> it[nomusuari] = nomUsuari.valor
                CampActualitzable.SenseCanvi -> Unit
            }
            when (passwordUsuari) {
                is CampActualitzable.NouValor -> it[password] = passwordUsuari.valor
                CampActualitzable.SenseCanvi -> Unit
            }
        } > 0
    }

    suspend fun afegeixAmic(idUsuari: Int, idAmic: Int): Boolean = dbQuery {
        if (idUsuari == idAmic) return@dbQuery false

        //Mirem que existeixin tant l'usuari propi com l'amic a la taula d'usuaris
        val usuarisExistents = Usuaris.selectAll()
            .where { (Usuaris.id eq idUsuari) or (Usuaris.id eq idAmic) }
            .count()

        if (usuarisExistents < 2L) return@dbQuery false

        val relacioJaExisteix = UsuarisAmics.selectAll()
            .where { (UsuarisAmics.idUsuari eq idUsuari) and (UsuarisAmics.idAmic eq idAmic) }
            .empty()
            .not()

        if (relacioJaExisteix) return@dbQuery false

        UsuarisAmics.insert {
            it[UsuarisAmics.idUsuari] = idUsuari
            it[UsuarisAmics.idAmic] = idAmic

            //Farem les relacions recíproques:
            it[UsuarisAmics.idUsuari] = idUsuari
            it[UsuarisAmics.idAmic] = idAmic
        }
        true
    }

    suspend fun obtenAmics(idUsuari: Int): List<Usuari> = dbQuery {
        // Fem un JOIN entre la taula d'amics i la d'usuaris
        (UsuarisAmics innerJoin Usuaris)
            .selectAll().where{ UsuarisAmics.idUsuari eq idUsuari }
            .map{it.toUsuari()}
    }

    suspend fun eliminaUsuari(id: Int): Boolean = dbQuery {
        Usuaris.deleteWhere { Usuaris.id eq id } > 0
    }

    private fun ResultRow.toUsuari(): Usuari {
        return Usuari(
            id = this[Usuaris.id],
            alias = this[Usuaris.alias],
            nomUsuari = this[Usuaris.nomusuari],
            password = this[Usuaris.password]
        )
    }
}
