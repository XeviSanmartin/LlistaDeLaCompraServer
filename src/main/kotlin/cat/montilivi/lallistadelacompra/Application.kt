package cat.montilivi.lallistadelacompra

import cat.montilivi.lallistadelacompra.plugins.configureRouting
import cat.montilivi.lallistadelacompra.plugins.configureSerialization
import cat.montilivi.lallistadelacompra.db.DatabaseFactory
import cat.montilivi.lallistadelacompra.plugins.JwtConfig
import cat.montilivi.lallistadelacompra.plugins.configureSecurity
import cat.montilivi.lallistadelacompra.plugins.configureSockets
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.cio.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    //DatabaseFactory.poblaLaBBDD()
    DatabaseFactory.poblaLaBBDDUtilitzantElsRepositoris()
    JwtConfig.inicialitza(environment.config)
    configureSecurity(RepositoriUsuaris)
    configureSerialization()
    configureSockets()
    configureStatusPages()
    configureRouting(RepositoriUsuaris)
}
