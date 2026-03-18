package cat.montilivi.cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.cat.montilivi.lallistadelacompra.model.Producte
import java.util.UUID

object RepositoriDeProductes {
    fun obtenTots():List<Producte> {
        return listOf(
            Producte(UUID.randomUUID().toString(), "Llet", "6", 2, "litres", null),
            Producte(UUID.randomUUID().toString(), "Pa", "1", 1, "barra", null),
            Producte(UUID.randomUUID().toString(), "Poma", "2", 6, "unitats", null),
            Producte(UUID.randomUUID().toString(), "Enciam", "3", 1, "unitat", null),
            Producte(UUID.randomUUID().toString(), "Pollastre", "4", 1, "kg", null),
            Producte(UUID.randomUUID().toString(), "Salmó", "5", 500, "gr", null)
        )
    }
}