package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.PeticioCategoria
import cat.montilivi.lallistadelacompra.repositori.RepositoriCategories
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.rutesDeLesCategories() {
    route("categories") {

        // GET: Llistar totes
        get {
            val categories = RepositoriCategories.obtenTotes()
            call.respond(categories)
        }

        // POST: Crear-ne una
        post {
            val peticio = call.receive<PeticioCategoria>() // Un model amb nom i icona
            val idCategoria = RepositoriCategories.creaCategoria(peticio.nom)
            call.respond(HttpStatusCode.Created, mapOf("idCategoria" to idCategoria))
        }

        // PATCH: Modificar-ne una
        patch("/{idCategoria}") {
            val id = call.parameters["idCategoria"]?.toIntOrNull() ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val peticio = call.receive<PeticioCategoria>()
            val exit = RepositoriCategories.actualitzaNomCategoria(id, peticio.nom)
            if (exit) call.respond(HttpStatusCode.OK, mapOf("status" to "Categoria actualitzada")) else call.respond(HttpStatusCode.NotFound)
        }

        // DELETE: Esborrar
        delete("/{idCategoria}") {
            val id = call.parameters["idCategoria"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val exit = RepositoriCategories.eliminaCategoria(id)
            if (exit) call.respond(HttpStatusCode.OK, mapOf("status" to "Categoria esborrada")) else call.respond(HttpStatusCode.NotFound)
        }
    }
}