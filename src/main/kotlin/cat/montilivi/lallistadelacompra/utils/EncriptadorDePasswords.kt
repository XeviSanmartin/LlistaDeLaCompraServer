package cat.montilivi.lallistadelacompra.utils

import org.mindrot.jbcrypt.BCrypt

object  EncriptadorDePasswords {
    /**
     * Crea un hash a partir d'una contrasenya en text pla.
     */
    fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * Verifica si una contrasenya coincideix amb un hash guardat.
     */
    fun check(password: String, hashed: String): Boolean {
        return BCrypt.checkpw(password, hashed)
    }
}
