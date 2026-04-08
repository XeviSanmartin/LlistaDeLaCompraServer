package cat.montilivi.lallistadelacompra.plugins

import cat.montilivi.lallistadelacompra.model.SessioUsuari
import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import cat.montilivi.lallistadelacompra.utils.EncriptadorDePasswords
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic
import io.ktor.server.auth.session
import io.ktor.server.response.respond
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.maxAge
import io.ktor.util.hex
import kotlin.time.DurationUnit
import kotlin.time.toDuration


fun Application.configureSecurity(userRepository: RepositoriUsuaris) {

    // 1. Configurem el magatzem de sessions (necessari per a la de sessió)
    install(Sessions) {
        cookie<SessioUsuari>("USER_SESSION") {
            cookie.path = "/"
            cookie.maxAge = 1.toDuration(unit = DurationUnit.DAYS)// La sessió dura 1 dia
            // En producció, afegiríem .extensions["SameSite"] = "Strict"

            // Xifratge de la cookie (Molt important perquè l'usuari no la pugui editar)
            val secretSignKey = hex("68656c6c6f6f72646572313233") // Una clau secreta de 16+ caràcters
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }

    install(Authentication) {

        // Autenticació bàsica
        basic("auth-basic") {
            realm = "Accés a la Llista de la Compra"
            validate { credentials ->
                // 1. Busquem l'usuari a la BBDD pel nom que ve de la capçalera
                val user = userRepository.cercaUsuariPerNomUsuari(credentials.name)

                // 2. Si l'usuari existeix i la password (BCrypt) coincideix
                if (user != null && EncriptadorDePasswords.check(credentials.password, user.password)) {
                    // Retornem un Principal (l'objecte que identifica qui és)
                    UserIdPrincipal(user.nomUsuari)
                } else {
                    // Si falla, retornem null i Ktor denegarà l'accés
                    null
                }
            }
        }

        //Autenticació per sessió
        session<SessioUsuari>("auth-session") {
            validate { sessio ->
                // --- AQUÍ FEM LA VALIDACIÓ REAL ---
                // Busquem l'usuari a la base de dades per la seva ID guardada a la sessió
                //Si s'esborra un usuari però encara té la cookie activa, això impediria el seu accès
                val existeixUsuari = RepositoriUsuaris.cercaUsuariPerId(sessio.idUsuari)

                if (existeixUsuari != null) {
                    // Si l'usuari encara existeix, la sessió és vàlida
                    sessio
                } else {
                    // Si l'usuari ha estat esborrat, retornem null i Ktor rebutjarà la petició
                    null
                }
            }
            challenge {
                // Què passa si no té sessió? Redirigim o donem error
                call.respond(io.ktor.http.HttpStatusCode.Unauthorized, "Sessió no iniciada")
            }
        }

    }




}