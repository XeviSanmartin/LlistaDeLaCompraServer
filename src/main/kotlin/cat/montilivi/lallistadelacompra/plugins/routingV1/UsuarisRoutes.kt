package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.PeticioActualitzacioUsuari
import cat.montilivi.lallistadelacompra.model.PeticioRegistre
import cat.montilivi.lallistadelacompra.model.SessioUsuari
import cat.montilivi.lallistadelacompra.model.toCampActualitzable
import cat.montilivi.lallistadelacompra.plugins.JwtConfig.generaToken
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set

fun Route.rutesDelsUsuaris() {

    // Rutes públiques
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
    post("registre") {
        val peticio = call.receive<PeticioRegistre>()
        try {
            val id = RepositoriUsuaris.creaUsuari(peticio.nomUsuari, peticio.motDePas, peticio.alias)
            call.respond(HttpStatusCode.Created, mapOf("id" to id))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, "Ja existeix un usuari amb aquest nom d'usuari")
        }
    }
    // Aquesta ruta és només per a testing, no l'hauria de tenir en producció
    get("usuaris"){
        call.respond(RepositoriUsuaris.obtenTots())
    }

    // Rutes protegides (requereixen Token)
    authenticate("auth-jwt") {
        route("me") {

            // Obtenir les dades de l'usuari actual
            get {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val usuari = RepositoriUsuaris.cercaUsuariPerId(idUsuari)
                if (usuari != null) call.respond(usuari)
                else call.respond(HttpStatusCode.NotFound)
            }

            // Actualitza el meu perfil
            patch {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)

                val peticio = call.receive<PeticioActualitzacioUsuari>()
                val exit = RepositoriUsuaris.actualitzaUsuari(
                    idUsuari,
                    peticio.nomUsuari.toCampActualitzable(),
                    peticio.motDePas.toCampActualitzable(),
                    peticio.alias.toCampActualitzable()
                )

                if (exit) call.respond(HttpStatusCode.OK, "Perfil actualitzat")
                else call.respond(HttpStatusCode.BadRequest)
            }
            delete {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val exit = RepositoriUsuaris.eliminaUsuari(idUsuari)
                if (exit) call.respond(HttpStatusCode.OK, "Usuari eliminat")
                else call.respond(HttpStatusCode.BadRequest)
            }
        }
        route("me/amics") {

            // GET: Llistar els meus amics
            get {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val amics = RepositoriUsuaris.obtenAmics(idUsuari)
                call.respond(amics)
            }

            // POST: Afegir un amic (per ID o podríem fer-ho per username)
            post("{idAmic}") {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val idAmic =
                    call.parameters["idAmic"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)

                val exit = RepositoriUsuaris.afegeixAmic(idUsuari, idAmic)
                if (exit) call.respond(HttpStatusCode.Created, "Amic afegit")
                else call.respond(HttpStatusCode.BadRequest, "No s'ha pogut afegir l'amic")
            }

            // DELETE: Treure un amic
            delete("{idAmic}") {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val idAmic = call.parameters["idAmic"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val exit = RepositoriUsuaris.eliminaAmic(idUsuari, idAmic)
                if (exit) call.respond(HttpStatusCode.OK, "Amic eliminat")
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}