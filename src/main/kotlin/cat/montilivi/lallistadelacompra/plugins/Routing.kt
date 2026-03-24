package cat.montilivi.lallistadelacompra.plugins

import cat.montilivi.lallistadelacompra.repositori.FAKERepositoriDeProductes
import cat.montilivi.lallistadelacompra.repositori.RepositoriCategories
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.html.respondHtml
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.head
import kotlinx.html.title

fun Application.configureRouting() {
    routing {
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
        route("/categories"){
            get{
                call.respond(RepositoriCategories.obtenTotes())
            }
        }
        route("/productes"){
            get{
                call.respond(FAKERepositoriDeProductes.obtenTots())
            }
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
