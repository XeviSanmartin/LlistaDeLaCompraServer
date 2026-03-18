package cat.montilivi.cat.montilivi.lallistadelacompra.model

import io.ktor.network.sockets.BoundDatagramSocket
import kotlinx.serialization.Serializable

@Serializable
data class ProducteDeLaLlista(
    val id: Int,
    val nomProducte: String,
    val quenatitat:Int,
    val unitat: String,
    val estatComprat: Boolean,
    val quiHaComprat:Int
)
