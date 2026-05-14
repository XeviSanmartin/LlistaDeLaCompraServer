package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.websockets.EsdevenimentLlista
import cat.montilivi.lallistadelacompra.model.requests.PeticioActualitzacioUsuari
import cat.montilivi.lallistadelacompra.model.requests.PeticioRegistre
import cat.montilivi.lallistadelacompra.model.autentificacio.SessioUsuari
import cat.montilivi.lallistadelacompra.model.websockets.TipusAccio
import cat.montilivi.lallistadelacompra.model.eines.toCampActualitzable
import cat.montilivi.lallistadelacompra.plugins.JwtConfig.generaToken
import cat.montilivi.lallistadelacompra.repositori.GestorDeConnexions
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import cat.montilivi.lallistadelacompra.model.requests.PeticioLogin
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
import io.ktor.server.routing.openapi.describe
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set

fun Route.rutesDelsUsuaris() {

    // Rutes públiques
    post("login") {
        // Accepta form data: username= i password=
        val parametres = call.receiveParameters()
        val username = parametres["username"] ?: ""
        val password = parametres["password"] ?: ""

        // 1. Validem amb el UserRepository + BCrypt
        val usuariDB = RepositoriUsuaris.cercaUsuariPerCredencials(username, password)

        if (usuariDB != null) {
            // 2. CREEM la sessió (Ktor enviarà la Cookie automàticament)
            call.sessions.set(SessioUsuari(idUsuari = usuariDB.id, nomUsuari = usuariDB.nomUsuari))

            // 3. Generem el token
            val token = generaToken(usuariDB.id)
            call.respond(mapOf("missatge" to "Login correcte", "token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Credencials invàlides")
        }
    }.describe {
        summary = "Inicia sessió"
        description = "Rep username i password com a form data (application/x-www-form-urlencoded) i retorna un token JWT. Copia el token i clica 'Authorize' a la part superior per usar els endpoints protegits."
        tag("Autenticació")
        requestBody {
            description = "Credencials de l'usuari en format form-data: camps 'username' i 'password'"
            ContentType.Application.FormUrlEncoded {
                schema = jsonSchema<PeticioLogin>()
            }
        }
        responses {
            HttpStatusCode.OK { description = "Login correcte. Retorna el token JWT a usar als endpoints protegits." }
            HttpStatusCode.Unauthorized { description = "Credencials invàlides" }
        }
    }
    post("registre") {
        //region Versió sense status page
//        val peticio = call.receive<PeticioRegistre>()
//        try {
//            val id = RepositoriUsuaris.creaUsuari(peticio.nomUsuari, peticio.motDePas, peticio.alias)
//            call.respond(HttpStatusCode.Created, mapOf("id" to id))
//        } catch (e: Exception) {
//            call.respond(HttpStatusCode.Conflict, "Ja existeix un usuari amb aquest nom d'usuari")
//        }
        //endregion
        val peticio = call.receive<PeticioRegistre>()
        val id = RepositoriUsuaris.creaUsuari(peticio.nomUsuari, peticio.motDePas, peticio.alias)
        // Si l'usuari existeix es llançarà una SQLException.
        call.respond(HttpStatusCode.Created, mapOf("id" to id))
    }.describe {
        summary = "Registra un usuari nou"
        description = "Crea un compte nou amb nomUsuari, motDePas i alias. El nomUsuari ha de ser únic."
        tag("Autenticació")
        requestBody {
            description = "Dades del nou usuari: nomUsuari (únic), motDePas i alias"
            schema = jsonSchema<PeticioRegistre>()
        }
        responses {
            HttpStatusCode.Created { description = "Usuari creat correctament. Retorna l'id del nou usuari." }
            HttpStatusCode.Conflict { description = "Ja existeix un usuari amb aquest nomUsuari" }
        }
    }

    // Aquesta ruta és només per a testing, no l'hauria de tenir en producció
    route("usuaris") {
        get {
            call.respond(RepositoriUsuaris.obtenTots())
        }
    }.describe {
        summary = "Llista tots els usuaris (només per a testing)"
        description = "No hauria d'estar disponible en producció"
        tag("Usuaris")
        responses {
            HttpStatusCode.OK { description = "Retorna la llista completa d'usuaris registrats" }
        }
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
            }.describe {
                summary = "Obté el meu perfil"
                description = "Retorna les dades de l'usuari autenticat amb el token JWT."
                tag("Usuaris")
                responses {
                    HttpStatusCode.OK { description = "Dades del perfil de l'usuari actual" }
                    HttpStatusCode.Unauthorized { description = "Token JWT absent o invàlid" }
                    HttpStatusCode.NotFound { description = "Usuari no trobat a la base de dades" }
                }
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
            }.describe {
                summary = "Actualitza el meu perfil"
                description = "Permet modificar nomUsuari, motDePas i/o alias. Tots els camps són opcionals"
                tag("Usuaris")
                requestBody {
                    description = "Camps a actualitzar: nomUsuari, motDePas i/o alias (tots opcionals)"
                    schema = jsonSchema<PeticioActualitzacioUsuari>()
                }
                responses {
                    HttpStatusCode.OK { description = "Perfil actualitzat correctament" }
                    HttpStatusCode.BadRequest { description = "No s'ha pogut actualitzar el perfil" }
                    HttpStatusCode.Unauthorized { description = "Token JWT absent o invàlid" }
                }
            }

            delete {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val exit = RepositoriUsuaris.eliminaUsuari(idUsuari)
                if (exit) call.respond(HttpStatusCode.OK, "Usuari eliminat")
                else call.respond(HttpStatusCode.BadRequest)
            }.describe {
                summary = "Elimina el meu compte"
                description = "Elimina permanentment el compte de l'usuari autenticat."
                tag("Usuaris")
                responses {
                    HttpStatusCode.OK { description = "Compte eliminat correctament" }
                    HttpStatusCode.BadRequest { description = "No s'ha pogut eliminar el compte" }
                    HttpStatusCode.Unauthorized { description = "Token JWT absent o invàlid" }
                }
            }
        }

        route("me/amics") {

            // GET: Llistar els meus amics
            get {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val amics = RepositoriUsuaris.obtenAmics(idUsuari)
                call.respond(amics)
            }.describe {
                summary = "Llista els meus amics"
                description = "Retorna la llista d'amics de l'usuari autenticat."
                tag("Usuaris")
                responses {
                    HttpStatusCode.OK { description = "Llista d'amics retornada correctament" }
                    HttpStatusCode.Unauthorized { description = "Token JWT absent o invàlid" }
                }
            }

            // POST: Afegir un amic (per ID)
            post("{idAmic}") {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val idAmic =
                    call.parameters["idAmic"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
                val exit = RepositoriUsuaris.afegeixAmic(idUsuari, idAmic)
                if (exit) {
                    val idsAVisualitzar = listOf<Int>(idAmic)
                    GestorDeConnexions.enviaAUsuarisConcrets(
                        idsAVisualitzar,
                        EsdevenimentLlista(TipusAccio.NOTIFICACIÓ_AMISTAT_NOVA, 0, idUsuari, null)
                    )
                    call.respond(HttpStatusCode.Created, "Amic afegit")
                } else call.respond(HttpStatusCode.BadRequest, "No s'ha pogut afegir l'amic")
            }.describe {
                summary = "Afegeix un amic per ID"
                description = "Afegeix l'usuari identificat per idAmic a la llista d'amics. Envia una notificació via WebSocket a l'amic."
                tag("Usuaris")
                parameters {
                    path("idAmic") {
                        description = "Identificador únic de l'usuari a afegir com a amic"
                        required = true
                    }
                }
                responses {
                    HttpStatusCode.Created { description = "Amic afegit correctament" }
                    HttpStatusCode.BadRequest { description = "No s'ha pogut afegir l'amic (id invàlid o ja és amic)" }
                    HttpStatusCode.Unauthorized { description = "Token JWT absent o invàlid" }
                }
            }

            // DELETE: Treure un amic
            delete("{idAmic}") {
                val idUsuari = call.principal<JWTPrincipal>()?.payload?.getClaim("idUsuari")?.asInt()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val idAmic = call.parameters["idAmic"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val exit = RepositoriUsuaris.eliminaAmic(idUsuari, idAmic)
                if (exit) {
                    val idsAVisualitzar = listOf<Int>(idAmic)
                    GestorDeConnexions.enviaAUsuarisConcrets(
                        idsAVisualitzar,
                        EsdevenimentLlista(TipusAccio.NOTIFICACIÓ_AMISTAT_ELIMINADA, 0, idUsuari, null)
                    )
                    call.respond(HttpStatusCode.OK, "Amic eliminat")
                } else call.respond(HttpStatusCode.NotFound)
            }.describe {
                summary = "Elimina un amic per ID"
                description = "Elimina l'usuari identificat per idAmic de la llista d'amics. Envia una notificació via WebSocket a l'amic."
                tag("Usuaris")
                parameters {
                    path("idAmic") {
                        description = "Identificador únic de l'usuari a eliminar de la llista d'amics"
                        required = true
                    }
                }
                responses {
                    HttpStatusCode.OK { description = "Amic eliminat correctament" }
                    HttpStatusCode.NotFound { description = "No s'ha trobat l'amic amb aquest id" }
                    HttpStatusCode.BadRequest { description = "El paràmetre idAmic no és un enter vàlid" }
                    HttpStatusCode.Unauthorized { description = "Token JWT absent o invàlid" }
                }
            }
        }
    }
}
