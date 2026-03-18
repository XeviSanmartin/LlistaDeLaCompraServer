package cat.montilivi.cat.montilivi.lallistadelacompra.repositori

import DatabaseFactory.dbQuery
import cat.montilivi.cat.montilivi.lallistadelacompra.db.Usuaris
import cat.montilivi.cat.montilivi.lallistadelacompra.model.Usuari
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll



class RepositoriUsuaris {
    suspend fun creaUsuari(nom_usuari: String, password_usuari: String, alias_usuari:String?): Usuari? = dbQuery {
        val insertStatement = Usuaris.insert {
            it[nomusuari] = nom_usuari
            it[password] = password_usuari
            it[alias] = alias_usuari
        }
        insertStatement.resultedValues?.singleOrNull()?.let {
            Usuari(id = it[Usuaris.id], nomUsuari = it[Usuaris.nomusuari], password = it[Usuaris.password], alias = it[Usuaris.alias])
        }
    }

    suspend fun findUserById(id: Int): Usuari? = dbQuery {
        //select dóna problemes perquè es confont amb Table.select(). És un lio d'imports

        Usuaris.selectAll().where { Usuaris.id eq id }
            .map { Usuari(it[Usuaris.id], it[Usuaris.alias],it[Usuaris.nomusuari],it[Usuaris.password]) }
            .singleOrNull()
    }
}
