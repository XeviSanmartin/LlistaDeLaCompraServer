package cat.montilivi.cat.montilivi.lallistadelacompra.model

data class Produte(
    val idProducte:String,
    val nomProducte:String,
    val idCategoria:String,
    val quantitat:Int,
    val unitats: String,
    val idUsuari: String?
)

