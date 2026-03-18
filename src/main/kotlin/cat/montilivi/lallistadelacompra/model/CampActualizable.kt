package cat.montilivi.cat.montilivi.lallistadelacompra.model

sealed class CampActualitzable<out T> {
    data object SenseCanvi : CampActualitzable<Nothing>()
    data class NouValor<T>(val valor: T) : CampActualitzable<T>()
}
