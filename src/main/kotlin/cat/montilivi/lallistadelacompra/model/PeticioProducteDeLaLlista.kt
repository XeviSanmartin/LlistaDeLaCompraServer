package cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
data class PeticioProducteDeLaLlista(
    val idProducte:Int,
    val quantitat:Int,
    val unitat: String,
    val estaComprat: Boolean,
    val quiHaComprat:Int
)
