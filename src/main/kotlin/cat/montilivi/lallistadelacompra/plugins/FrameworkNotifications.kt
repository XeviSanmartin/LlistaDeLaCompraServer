import cat.montilivi.lallistadelacompra.model.errors.BaseException
import cat.montilivi.lallistadelacompra.model.errors.RespostaError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<BaseException> { call, cause ->
            call.respond(
                status = cause.status,
                message = RespostaError(
                    estat = cause.status.value,
                    missatge = cause.message
                )
            )
        }

        // Gestionar errors de paràmetres (p. ex. enviar text on cal un número)
        exception<ParameterConversionException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                RespostaError(
                    estat = HttpStatusCode.BadRequest.value,
                    missatge = "El paràmetre proporcionat no té un format correcte. (${cause.localizedMessage})"
                )
            )
        }

        // Gestionar errors de la Base de Dades (p. ex. claus duplicades)
        exception<java.sql.SQLException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                RespostaError(
                    estat = HttpStatusCode.Conflict.value,
                    missatge = "Hi ha hagut un conflicte amb les dades (possible duplicat o falta de referència)  (${cause.localizedMessage})."
                )
            )
        }

        // Error genèric (La xarxa de seguretat final)
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                RespostaError(
                    estat = HttpStatusCode.InternalServerError.value,
                    missatge = "Error inesperat al servidor: ${cause.localizedMessage}"
                )
            )
        }
    }
}