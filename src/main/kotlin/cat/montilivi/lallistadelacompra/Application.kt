package cat.montilivi.lallistadelacompra

import cat.montilivi.lallistadelacompra.plugins.configureHTTP
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
import io.ktor.server.routing.openapi.registerBearerAuthSecurityScheme

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    //DatabaseFactory.poblaLaBBDD()
    DatabaseFactory.poblaLaBBDDUtilitzantElsRepositoris()
    JwtConfig.inicialitza(environment.config)
    configureHTTP()          // CORS - ha d'anar abans del routing
    configureSecurity(RepositoriUsuaris)
    // Registrem el security scheme de JWT per al Swagger UI
    // "auth-jwt" és el nom del provider definit a Security.kt
    registerBearerAuthSecurityScheme(name = "auth-jwt", bearerFormat = "JWT")
    configureSerialization()
    configureSockets()
    configureStatusPages()
    configureRouting(RepositoriUsuaris)
}
