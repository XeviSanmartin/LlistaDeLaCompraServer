package cat.montilivi.cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.cat.montilivi.lallistadelacompra.model.Categoria
import java.util.UUID

object RepositoriDeCategories {
    fun obtenTotes():List<Categoria>{
        return listOf(
            Categoria(0, "Altres"),
            Categoria(1, "Fruites"),
            Categoria(2, "Verdures"),
            Categoria(3, "Carns"),
            Categoria(4, "Peixos"),
            Categoria(5, "Lactis")
        )
    }
}