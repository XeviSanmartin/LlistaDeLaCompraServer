package cat.montilivi.cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
data class Producte(
    val id:Int,
    val nomProducte:String,
    val idCategoria:Int,

)

