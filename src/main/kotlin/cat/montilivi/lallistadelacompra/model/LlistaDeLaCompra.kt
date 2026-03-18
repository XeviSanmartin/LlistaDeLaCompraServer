package cat.montilivi.cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
data class LlistaDeLaCompra(
    val id: Int,
    val nomLlista: String,
    val idPropietari: Int,
)
