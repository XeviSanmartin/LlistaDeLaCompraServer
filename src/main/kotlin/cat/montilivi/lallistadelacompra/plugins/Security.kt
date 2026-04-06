package cat.montilivi.lallistadelacompra.plugins

import cat.montilivi.lallistadelacompra.repositori.RepositoriUsuaris
import cat.montilivi.lallistadelacompra.utils.EncriptadorDePasswords
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic

fun Application.configureSecurity(userRepository: RepositoriUsuaris) {
    install(Authentication) {
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
    }
}