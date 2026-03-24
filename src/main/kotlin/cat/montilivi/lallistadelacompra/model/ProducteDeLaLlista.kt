package cat.montilivi.lallistadelacompra.model

import cat.montilivi.lallistadelacompra.db.LlistesDeLaCompra
import cat.montilivi.lallistadelacompra.db.Usuaris
import io.ktor.network.sockets.BoundDatagramSocket
import kotlinx.serialization.Serializable

@Serializable
data class ProducteDeLaLlista(
    val id: Int,
    val idLLista:Int,
    val nomProducte: String,
    val quenatitat:Int,
    val unitat: String,
    val estatComprat: Boolean,
    val quiHaComprat:Int
)
