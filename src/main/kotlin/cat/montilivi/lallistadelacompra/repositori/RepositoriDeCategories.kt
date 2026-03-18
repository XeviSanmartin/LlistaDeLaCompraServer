package cat.montilivi.cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.cat.montilivi.lallistadelacompra.model.Categoria
import java.util.UUID

object RepositoriDeCategories {
    fun obtenTotes():List<Categoria>{
        return listOf(
            Categoria(UUID.randomUUID().toString(), "Altres"),
            Categoria(UUID.randomUUID().toString(), "Fruites"),
            Categoria(UUID.randomUUID().toString(), "Verdures"),
            Categoria(UUID.randomUUID().toString(), "Carns"),
            Categoria(UUID.randomUUID().toString(), "Peixos"),
            Categoria(UUID.randomUUID().toString(), "Lactis")
        )
    }
}