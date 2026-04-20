package cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
data class PeticioRegistre(
    val nomUsuari: String,
    val motDePas: String,
    val alias: String,
)
