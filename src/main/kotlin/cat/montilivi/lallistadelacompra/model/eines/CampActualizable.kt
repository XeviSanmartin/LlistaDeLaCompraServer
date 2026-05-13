package cat.montilivi.lallistadelacompra.model.eines

sealed class CampActualitzable<out T> {
    data object SenseCanvi : CampActualitzable<Nothing>()
    data class NouValor<T>(val valor: T) : CampActualitzable<T>()
}

fun <T> T?.toCampActualitzable(): CampActualitzable<T> =
    if (this == null) CampActualitzable.SenseCanvi else CampActualitzable.NouValor(this)