package cat.montilivi.lallistadelacompra

import cat.montilivi.lallistadelacompra.plugins.configureRouting
import cat.montilivi.lallistadelacompra.plugins.configureSerialization
import cat.montilivi.lallistadelacompra.db.DatabaseFactory
import cat.montilivi.lallistadelacompra.plugins.configureSecurity
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import io.ktor.server.application.*
import io.ktor.server.cio.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    //DatabaseFactory.poblaLaBBDD()
    DatabaseFactory.poblaLaBBDDUtilitzantElsRepositoris()
    configureSecurity(RepositoriUsuaris)
    configureSerialization()
    configureRouting(RepositoriUsuaris)
}
