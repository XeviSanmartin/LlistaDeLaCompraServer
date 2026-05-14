package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.requests.PeticioCategoria
import cat.montilivi.lallistadelacompra.repositori.RepositoriCategories
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.openapi.describe

fun Route.rutesDeLesCategories() {
    route("categories") {

        // GET: Llistar totes
        get {
            val categories = RepositoriCategories.obtenTotes()
            call.respond(categories)
        }.describe {
            summary = "Llista totes les categories"
            description = "Retorna la llista completa de categories disponibles."
            tag("Categories")
            responses {
                HttpStatusCode.OK { description = "Llista de categories retornada correctament" }
            }
        }

        // POST: Crear-ne una
        post {
            val peticio = call.receive<PeticioCategoria>()
            val idCategoria = RepositoriCategories.creaCategoria(peticio.nom)
            call.respond(HttpStatusCode.Created, mapOf("idCategoria" to idCategoria))
        }.describe {
            summary = "Crea una categoria nova"
            description = "Crea una nova categoria amb el nom indicat al cos de la petició."
            tag("Categories")
            requestBody {
                description = "Dades de la nova categoria"
                schema = jsonSchema<PeticioCategoria>()
            }
            responses {
                HttpStatusCode.Created { description = "Categoria creada correctament. Retorna l'id de la nova categoria." }
            }
        }

        // PATCH: Modificar-ne una
        patch("/{idCategoria}") {
            val id = call.parameters["idCategoria"]?.toIntOrNull() ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val peticio = call.receive<PeticioCategoria>()
            val exit = RepositoriCategories.actualitzaNomCategoria(id, peticio.nom)
            if (exit) call.respond(HttpStatusCode.OK, mapOf("status" to "Categoria actualitzada")) else call.respond(HttpStatusCode.NotFound)
        }.describe {
            summary = "Modifica el nom d'una categoria"
            description = "Actualitza el nom de la categoria identificada per idCategoria."
            tag("Categories")
            parameters {
                path("idCategoria") {
                    description = "Identificador únic de la categoria a modificar"
                    required = true
                }
            }
            requestBody {
                description = "Dades actualitzades de la categoria (nou nom)"
                schema = jsonSchema<PeticioCategoria>()
            }
            responses {
                HttpStatusCode.OK { description = "Categoria actualitzada correctament" }
                HttpStatusCode.NotFound { description = "No s'ha trobat cap categoria amb aquest id" }
                HttpStatusCode.BadRequest { description = "El paràmetre idCategoria no és un enter vàlid" }
            }
        }

        // DELETE: Esborrar
        delete("/{idCategoria}") {
            val id = call.parameters["idCategoria"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val exit = RepositoriCategories.eliminaCategoria(id)
            if (exit) call.respond(HttpStatusCode.OK, mapOf("status" to "Categoria esborrada")) else call.respond(HttpStatusCode.NotFound)
        }.describe {
            summary = "Esborra una categoria"
            description = "Elimina permanentment la categoria identificada per idCategoria."
            tag("Categories")
            parameters {
                path("idCategoria") {
                    description = "Identificador únic de la categoria a eliminar"
                    required = true
                }
            }
            responses {
                HttpStatusCode.OK { description = "Categoria esborrada correctament" }
                HttpStatusCode.NotFound { description = "No s'ha trobat cap categoria amb aquest id" }
                HttpStatusCode.BadRequest { description = "El paràmetre idCategoria no és un enter vàlid" }
            }
        }
    }
}
