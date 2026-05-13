package cat.montilivi.lallistadelacompra.model.bbdd

import kotlinx.serialization.Serializable

@Serializable
data class Categoria(
    val id: Int,
    val nomCategoria: String
)
