package cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
data class PeticioActualitzacioUsuari(
    val nomUsuari: String? = null,
    val motDePas: String? =  null,
    val alias: String? = null
)
