package cat.montilivi.lallistadelacompra.model.errors

import io.ktor.http.*

// La mare de totes les nostres excepcions
open class BaseException(
    val status: HttpStatusCode,
    override val message: String
) : RuntimeException(message)

// Errors concrets de la teva app
class RecursNoTrobatException(val id: Int) :
    BaseException(HttpStatusCode.NotFound, "No s'ha trobat cap element amb l'ID: $id")

class PropietariIncorrecteException(val usuari: String) :
    BaseException(HttpStatusCode.Forbidden, "L'usuari $usuari no té permisos per modificar aquesta llista")

class ParametresInvalidException(missatge: String) :
BaseException(HttpStatusCode.BadRequest, missatge)