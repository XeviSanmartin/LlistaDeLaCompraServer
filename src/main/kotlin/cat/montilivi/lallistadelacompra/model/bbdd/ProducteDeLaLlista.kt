package cat.montilivi.lallistadelacompra.model.bbdd

import kotlinx.serialization.Serializable

@Serializable
data class ProducteDeLaLlista(
    val idProducte: Int,
    val idLLista:Int,
    val quantitat:Int,
    val unitat: String,
    val estaComprat: Boolean,
    val quiHaComprat:Int
)
