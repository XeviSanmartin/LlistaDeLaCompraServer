package cat.montilivi.lallistadelacompra.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PeticioActualitzacioUsuari(
    val nomUsuari: String? = null,
    val motDePas: String? =  null,
    val alias: String? = null
)
