package cat.montilivi.lallistadelacompra.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PeticioActualitzacioProducteDeLaLlista(
    val quantitat:Int? = null,
    val unitat: String? = null,
    val estaComprat: Boolean? = null,
    val quiHaComprat:Int? =  null
)
