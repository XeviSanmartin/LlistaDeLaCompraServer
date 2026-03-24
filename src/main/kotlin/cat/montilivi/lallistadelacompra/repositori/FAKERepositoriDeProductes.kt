package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.model.Producte

object FAKERepositoriDeProductes {
    fun obtenTots():List<Producte> {
        return listOf(
            Producte(0, "Llet", 0,),
            Producte(1, "Pa", 1, ),
            Producte(2, "Poma", 2),
            Producte(3, "Enciam", 3),
            Producte(4, "Pollastre", 4),
            Producte(5, "Salmó", 5)
        )
    }
}
