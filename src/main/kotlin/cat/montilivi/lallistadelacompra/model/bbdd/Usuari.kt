package cat.montilivi.lallistadelacompra.model.bbdd

import kotlinx.serialization.Serializable

@Serializable
data class Usuari(
    val id: Int,
    val alias: String?,
    val nomUsuari: String,
    val password:String,
    val idsLlistesVisibles: List<Int> = emptyList(),
    val idsAmics:List<Int> = emptyList()
)
