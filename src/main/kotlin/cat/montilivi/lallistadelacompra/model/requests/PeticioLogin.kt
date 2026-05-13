package cat.montilivi.lallistadelacompra.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PeticioLogin(
    val username: String,
    val password: String,
)

