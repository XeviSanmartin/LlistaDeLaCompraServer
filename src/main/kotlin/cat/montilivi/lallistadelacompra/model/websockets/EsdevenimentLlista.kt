package cat.montilivi.lallistadelacompra.model.websockets

import cat.montilivi.lallistadelacompra.model.bbdd.ProducteDeLaLlista
import kotlinx.serialization.Serializable

@Serializable
data class EsdevenimentLlista(
    val accio: TipusAccio,  //AFEGIT, ELIMINAT, ACTUALITZAT
    val idLlista: Int,
    val idRecursAfectat: Int? = null, // Pot ser idProducte, idCategoria, etc. depenent de l'acció, si no cal el producte sencer
    val producte: ProducteDeLaLlista? = null, // Només si l'acció és sobre un producte
    val timestamp: Long = System.currentTimeMillis() // Útil per evitar missatges repetits o antics
)