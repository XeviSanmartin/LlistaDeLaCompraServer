package cat.montilivi.lallistadelacompra.plugins

import cat.montilivi.lallistadelacompra.model.PeticioLlistaNova
import cat.montilivi.lallistadelacompra.model.SessioUsuari
import cat.montilivi.lallistadelacompra.plugins.JwtConfig.generaToken
import cat.montilivi.lallistadelacompra.plugins.routingV1.rutesDeLesLlistesDeLaCompra
import cat.montilivi.lallistadelacompra.repositori.FAKERepositoriDeProductes
import cat.montilivi.lallistadelacompra.repositori.RepositoriCategories
import cat.montilivi.lallistadelacompra.repositori.RepositoriLlistesDeLaCompra
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.html.respondHtml
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.head
import kotlinx.html.title

fun Application.configureRouting(userRepository: RepositoriUsuaris) {
    routing {

        //region Proves prèvies
        get("/") {
            call.respondText("Hello World!")
        }

        //creador web de projectes: https://start.ktor.io/settings
        //region Versió sense enrutaments niuats
//        get ("/html"){
//            call.respondHtml (status = HttpStatusCode.OK) {
//                head {
//                    title { +"Servei de llista de la compra" }
//                }
//                body {
//                    h1 { +"Benvingut al servei de llista de la compra" }
//                    p { +"Aquesta és un microservei implementat amb Ktor." }
//                    div{
//                        a(href = "html/clickat") { +"Fes-me click" }
//                    }
//                }
//            }
//        }
//        get ("/html/clickat"){
//            call.respondHtml {
//                head {
//                    title { +"Has fet click!" }
//                }
//                body {
//                    h1 { +"Has fet click al link!" }
//                    p { +"Gràcies per visitar la pàgina." }
//                    div{
//                        a(href = "/html") { +"Torna a la pàgina principal" }
//                    }
//                }
//            }
//        }
        //endregion
        //region Versió amb enrutaments niuats
//        route ("html"){
//            get{
//                call.respondHtml (status = HttpStatusCode.OK) {
//                    head {
//                        title { +"Servei de llista de la compra" }
//                    }
//                    body {
//                        h1 { +"Benvingut al servei de llista de la compra" }
//                        p { +"Aquesta és un microservei implementat amb Ktor." }
//                        div{
//                            a(href = "html/clickat") { +"Fes-me click" }
//                        }
//                    }
//                }
//            }
//            get("clickat"){
//                call.respondHtml {
//                    head {
//                        title { +"Has fet click!" }
//                    }
//                    body {
//                        h1 { +"Has fet click al link!" }
//                        p { +"Gràcies per visitar la pàgina." }
//                        div{
//                            a(href = "/html") { +"Torna a la pàgina principal" }
//                        }
//                    }
//                }
//            }
//        }
        //endregion
        //Versió amb enrutaments niuats extraient la lògica a una funció
        rutesHtml()

        route("/usuaris"){
            get{
                call.respond(RepositoriUsuaris.obtenTots())
            }
        }

        authenticate("auth-basic") {
            route("/categories"){
                get{
                    // Recuperem la identitat de qui està trucant
                    val principal = call.principal<UserIdPrincipal>()
                    val nom = principal?.name
                    //call.respondText("Benvingut, ${principal?.name}! Aquesta és la teva zona privada.")
                    call.respond(RepositoriCategories.obtenTotes())
                }
            }
        }

        authenticate("auth-jwt") {
            route("/productes") {
                get {
                    call.respond(FAKERepositoriDeProductes.obtenTots())
                }
            }
        }

        post("/login") {
            val parametres = call.receiveParameters()
            val usuari = parametres["username"] // En aquest moment estic decidint com cal que es diguin els paràmetres
            val motDePas = parametres["password"]  // que m'han de passar per iniciar sessió

            // 1. Validem amb el UserRepository + BCrypt
            val usuariDB = RepositoriUsuaris.cercaUsuariPerCredencials(usuari ?: "", motDePas ?: "")

            if (usuariDB != null) {
                // 2. CREEM la sessió (Ktor enviarà la Cookie automàticament)
                call.sessions.set(SessioUsuari(idUsuari = usuariDB.id, nomUsuari = usuariDB.nomUsuari))
                //Deactivem aquesta resposta, perquè tan sols es pot contestar una vegada i non ens
                //deixaria enviar el token. Afego, el missatge amb el token
                //call.respondText("Login correcte!")

                // 3. Generem el token
                val token = generaToken(usuariDB.id)
                call.respond(mapOf("missatge" to "Login correcte", "token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Credencials invàlides")
            }
        }

        authenticate("auth-session") {
            get("/perfil") {
                val sessioUsuari = call.principal<SessioUsuari>()
                call.respondText("Hola ${sessioUsuari?.nomUsuari}, la teva ID és ${sessioUsuari?.idUsuari}")
            }

            post("/logout") {
                // 3. DESTRUÏM la sessió
                call.sessions.clear<SessioUsuari>()
                call.respondText("Sessió tancada")
            }
        }

        //endregion

        rutesV1()
    }
}
private fun Routing.rutesV1() {
    route("/V1") {
        post("login") {
            val parametres = call.receiveParameters()
            val usuari = parametres["username"] // En aquest moment estic decidint com cal que es diguin els paràmetres
            val motDePas = parametres["password"]  // que m'han de passar per iniciar sessió

            // 1. Validem amb el UserRepository + BCrypt
            val usuariDB = RepositoriUsuaris.cercaUsuariPerCredencials(usuari ?: "", motDePas ?: "")

            if (usuariDB != null) {
                // 2. CREEM la sessió (Ktor enviarà la Cookie automàticament)
                call.sessions.set(SessioUsuari(idUsuari = usuariDB.id, nomUsuari = usuariDB.nomUsuari))
                //Deactivem aquesta resposta, perquè tan sols es pot contestar una vegada i non ens
                //deixaria enviar el token. Afego, el missatge amb el token
                //call.respondText("Login correcte!")

                // 3. Generem el token
                val token = generaToken(usuariDB.id)
                call.respond(mapOf("missatge" to "Login correcte", "token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Credencials invàlides")
            }
        }

        authenticate("auth-jwt") {
            rutesDeLesLlistesDeLaCompra()
        }
    }
}

private fun Routing.rutesHtml() {
            route ("html"){
            get{
                call.respondHtml (status = HttpStatusCode.OK) {
                    head {
                        title { +"Servei de llista de la compra" }
                    }
                    body {
                        h1 { +"Benvingut al servei de llista de la compra" }
                        p { +"Aquesta és un microservei implementat amb Ktor." }
                        div{
                            a(href = "html/clickat") { +"Fes-me click" }
                        }
                    }
                }
            }
            get("clickat"){
                call.respondHtml {
                    head {
                        title { +"Has fet click!" }
                    }
                    body {
                        h1 { +"Has fet click al link!" }
                        p { +"Gràcies per visitar la pàgina." }
                        div{
                            a(href = "/html") { +"Torna a la pàgina principal" }
                        }
                    }
                }
            }
        }
}
