package cat.montilivi.lallistadelacompra.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PeticioProducte(
    val nom: String? = null,
    val idCategoria: Int? = null
)
