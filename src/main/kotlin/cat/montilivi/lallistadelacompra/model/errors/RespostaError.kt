package cat.montilivi.lallistadelacompra.model.errors

import kotlinx.serialization.Serializable

@Serializable
data class RespostaError(
    val estat: Int,
    val missatge: String,
    val codiIntern: String? = null
)
