package cat.montilivi.lallistadelacompra.model

import kotlinx.serialization.Serializable

@Serializable
enum class TipusAccio {
    CONNEXIO_ESTABLERTA,
    PRODUCTE_AFEGIT,
    PRODUCTE_ACTUALITZAT,
    PRODUCTE_ELIMINAT,
    LLISTA_ACTUALITZADA,
    NOTIFICACIÓ_AMISTAT_NOVA,
    NOTIFICACIÓ_AMISTAT_ELIMINADA,
}