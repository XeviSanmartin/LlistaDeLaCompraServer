package cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
data class Producte(
    val idProducte:Int,
    val nomProducte:String,
    val idCategoria:Int,

    )

