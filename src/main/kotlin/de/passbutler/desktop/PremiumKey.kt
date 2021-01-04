package de.passbutler.desktop

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.tinylog.kotlin.Logger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.time.Instant

data class PremiumKey(
    val id: String,
    val name: String,
    val email: String,
    val company: String?,
    val expirationDate: Instant?
) {
    object Deserializer {
        // TODO: result
        fun deserializeOrNull(jsonWebToken: String): PremiumKey? {
            return try {
                val rsaKeyFactory = KeyFactory.getInstance("RSA")
                val rsaPublicKey = rsaKeyFactory.generatePublic(X509EncodedKeySpec(SIGNATURE_PUBLIC_KEY)) as RSAPublicKey

                val jwtAlgorithm = Algorithm.RSA256(rsaPublicKey, null)
                val jwtVerifier = JWT.require(jwtAlgorithm).build()
                val decodedJsonWebToken = jwtVerifier.verify(jsonWebToken)

                PremiumKey(
                    id = decodedJsonWebToken.id,
                    name = decodedJsonWebToken.getClaim(SERIALIZATION_KEY_NAME).asString(),
                    email = decodedJsonWebToken.getClaim(SERIALIZATION_KEY_EMAIL).asString(),
                    company = decodedJsonWebToken.getClaim(SERIALIZATION_KEY_COMPANY).asString(),
                    expirationDate = decodedJsonWebToken.expiresAt?.toInstant()
                )
            } catch (exception: JWTVerificationException) {
                Logger.warn(exception, "The given premium key is invalid!")
                null
            } catch (exception: Exception) {
                Logger.warn(exception, "The given premium key has an invalid format!")
                null
            }
        }
    }

    companion object {
        private const val SERIALIZATION_KEY_NAME = "name"
        private const val SERIALIZATION_KEY_EMAIL = "email"
        private const val SERIALIZATION_KEY_COMPANY = "company"

        private val SIGNATURE_PUBLIC_KEY = byteArrayOf(48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -49, 91, 117, 5, -64, -28, 5, -80, 94, -100, -126, 124, -69, 47, 94, -47, 101, 43, -102, -105, 119, 15, -101, -81, -39, 125, 123, 120, -100, -91, -128, 44, 10, 127, -82, 20, -45, -103, -111, 41, -104, -75, 33, -42, 59, 93, 76, -37, -5, -17, -78, 48, 40, 113, -80, -53, -128, -107, 70, 127, 112, 66, -11, 27, -73, 27, -13, -4, -99, 12, 77, 110, 26, 35, 55, -115, -64, 77, 17, 77, 118, 63, 99, -96, -93, 52, -55, -25, 34, 11, 3, 99, 61, -10, 84, -46, -115, 51, -85, 119, 76, -25, -82, -26, -58, -102, -122, -31, 33, -99, -18, 84, -115, 80, 74, 73, -107, -47, 32, -77, 90, 125, -104, 106, 75, 39, 62, -127, 1, -30, 91, -80, -23, 42, -10, -44, -127, -12, 49, -39, -127, 100, 6, -15, 69, -31, -41, -6, 25, -19, 68, 121, 24, -52, -86, 77, 10, 88, -96, 113, -104, 91, -120, 30, -15, -47, 25, 80, -31, -74, -5, -9, -15, 77, -95, 17, 117, -36, -23, 35, -111, -36, -69, -98, 41, 101, -16, 70, 109, 120, 44, -103, 83, -123, 78, -25, -101, -32, -59, -21, 32, 110, -47, 38, 121, -40, -52, -72, 112, -79, 14, -6, -90, 16, -119, 119, -102, 108, -114, 44, 1, 41, 19, -1, 36, 60, -31, -107, 125, -34, -35, -102, 99, 4, -114, -7, 112, -74, -126, 47, 96, -85, 93, 3, 17, 32, 9, -125, 1, 117, -106, 41, -20, -86, 20, -89, 2, 3, 1, 0, 1)
    }
}

object PremiumKeyRequiredException : Exception("A premium key is required for this operation!")
