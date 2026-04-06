package cat.montilivi.lallistadelacompra.model

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class SessioUsuari (val idUsuari: Int, val nomUsuari: String) : Principal