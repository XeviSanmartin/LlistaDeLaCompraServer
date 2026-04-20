package cat.montilivi.lallistadelacompra.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private lateinit var algorithm: Algorithm

    // Aquesta funció s'ha de cridar un cop a l'inici (Application.module)
    fun inicialitza(config: ApplicationConfig) {
        try {
        secret = config.property("jwt.secret").getString()
        issuer = config.property("jwt.issuer").getString()
        audience = config.property("jwt.audience").getString()
        algorithm = Algorithm.HMAC256(secret)
        }
        catch (e: Exception) {
            // Si falta alguna, llancem un error personalitzat i clar
            throw IllegalStateException(
                "ERROR DE CONFIGURACIÓ: No s'ha trobat la secció 'jwt' al fitxer application.yaml. " +
                        "Assegura't que tens definits 'secret', 'issuer' i 'audience' sota el bloc 'jwt'."
            )
        }
    }

    fun generaToken(idUsuari: Int): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("idUsuari", idUsuari)
        .withExpiresAt(Date(System.currentTimeMillis() + 24*3600000)) // 1 hora
        .sign(algorithm)
    }
