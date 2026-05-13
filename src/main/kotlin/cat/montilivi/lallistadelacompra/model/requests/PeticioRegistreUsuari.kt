package cat.montilivi.lallistadelacompra.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PeticioRegistre(
    val nomUsuari: String,
    val motDePas: String,
    val alias: String,
)
