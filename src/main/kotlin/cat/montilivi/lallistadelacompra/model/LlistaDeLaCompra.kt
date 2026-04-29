package cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
data class LlistaDeLaCompra(
    val id: Int,
    val nomLlista: String,
    val idsPropietaris: List<Int> = emptyList(),
    val idsProductes: List<Int> = emptyList(),
)
