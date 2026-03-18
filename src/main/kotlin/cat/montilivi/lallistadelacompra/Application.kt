package cat.montilivi.cat.montilivi.lallistadelacompra

import cat.montilivi.cat.montilivi.lallistadelacompra.plugins.configureRouting
import cat.montilivi.cat.montilivi.lallistadelacompra.plugins.configureSerialization
import cat.montilivi.lallistadelacompra.db.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.cio.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureRouting()
}
