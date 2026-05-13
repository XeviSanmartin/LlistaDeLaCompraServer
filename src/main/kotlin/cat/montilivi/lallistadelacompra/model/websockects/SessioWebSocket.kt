package cat.montilivi.lallistadelacompra.model.websockects

import io.ktor.server.websocket.DefaultWebSocketServerSession

data class SessioWebSocket(
    val idUsuari: Int,
    val nomUsuari: String,
    val session: DefaultWebSocketServerSession
)