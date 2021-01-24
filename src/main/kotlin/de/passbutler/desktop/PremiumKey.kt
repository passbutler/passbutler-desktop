package de.passbutler.desktop

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
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
    companion object {
        private const val JWT_CLAIM_NAME = "name"
        private const val JWT_CLAIM_EMAIL = "email"
        private const val JWT_CLAIM_COMPANY = "company"

        private val SIGNATURE_PUBLIC_KEY = byteArrayOf(48, -126, 2, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 2, 15, 0, 48, -126, 2, 10, 2, -126, 2, 1, 0, -38, 60, 40, -122, 50, 47, -107, -77, 98, -96, -31, 71, 122, 61, -14, -5, 2, -43, 11, 109, 72, -63, -56, 97, -55, 64, 59, 36, -113, -41, -5, 38, -83, -2, 23, -94, -64, 83, 84, -66, 96, 61, -93, 72, -7, 104, 68, -41, -99, -88, 14, 94, -97, 34, 26, -91, 31, -81, -21, 47, -61, 65, -96, 55, 79, 46, -71, -100, -68, 3, -2, 89, 35, -55, -2, -5, -13, -48, -58, -56, -43, -82, -32, 55, -118, -125, 115, -79, -81, -50, 41, 28, -110, -9, -111, -106, -9, 80, -29, 70, -15, 75, 38, 26, 27, -80, 116, 122, -82, 31, 15, -71, -23, -33, 123, -27, 36, 2, -123, 48, 10, -99, -90, 71, -112, 46, 22, 53, 91, -10, -109, 50, 24, 17, 63, 68, 65, 117, -69, -126, -34, 76, 83, 105, 45, 124, 114, 109, 35, 53, 110, 27, 26, -53, -24, -65, -54, 59, 1, -28, -83, 48, 34, 61, -119, -115, 102, 51, -72, 64, -128, -59, 12, 14, -3, -76, 14, -98, 91, -7, 31, 112, 103, -126, -41, -93, 93, 76, 24, -80, 44, 125, -100, 127, -18, -114, -112, -13, -90, 19, -93, 53, -114, -69, 30, -64, 87, 59, 120, 5, 82, 110, 26, -111, 124, -23, 114, 119, 92, -105, 104, 31, 101, 3, 70, -77, 126, -77, -90, -64, 17, 93, 12, 126, -83, -49, -26, -59, -75, 10, -18, -37, 23, 85, 8, 109, -61, 35, 8, 40, -112, -45, -103, -10, 67, 51, 5, 15, -77, 117, 97, 113, -73, -69, -8, 60, 54, 18, -67, -61, -66, -49, 126, -35, 99, 96, 44, 15, -57, -91, 114, 33, -81, -63, 46, 86, -84, 93, -59, -100, -50, 34, -75, -85, -92, 21, -112, -37, 16, -33, 105, 9, 124, -38, 82, 23, -100, -24, 111, -79, -5, -109, -13, 76, 29, 51, -44, 101, 6, 110, 59, -124, -104, -23, -12, 41, 56, -125, 102, -96, 51, 45, 55, -2, -37, -100, -107, -54, 1, 36, 50, -113, -22, 110, 56, -89, 92, 63, -66, -26, 68, 102, -128, -41, 124, 68, -21, 107, -36, -101, -80, -38, 46, 73, -21, -23, -19, -123, -83, 4, 95, -77, -107, -78, -125, -87, 67, -97, 96, -25, -126, -118, 5, 126, 16, -70, -95, -42, -9, -2, 42, 65, 12, 120, 5, -62, 124, -75, 28, 3, 82, 66, -61, 67, 79, 69, 119, -23, -27, 40, 71, -47, -126, 46, -72, -17, 86, 39, -72, 123, -97, 20, -23, 39, 13, -113, 106, 113, 38, -28, -78, -92, -22, 0, -104, 7, -78, 40, 111, -14, 41, 19, 111, -93, 24, -64, -90, -57, 120, -45, -87, 107, -4, -43, -80, -56, 61, -83, -1, -21, 12, 52, -32, 98, 33, 68, 124, 19, -43, 7, -18, -101, 125, 71, -119, 127, -63, -91, 32, -112, 42, 95, -34, 71, 26, -3, -80, 58, 71, -93, 54, 19, -28, 26, 29, -49, 85, -89, -111, 125, 49, -9, 84, 38, 102, 45, 34, -80, 24, -117, 59, 27, 2, 3, 1, 0, 1)

        fun parse(jsonWebToken: String): Result<PremiumKey> {
            return try {
                val rsaKeyFactory = KeyFactory.getInstance("RSA")
                val rsaPublicKey = rsaKeyFactory.generatePublic(X509EncodedKeySpec(SIGNATURE_PUBLIC_KEY)) as RSAPublicKey

                val jwtAlgorithm = Algorithm.RSA256(rsaPublicKey, null)
                val jwtVerifier = JWT.require(jwtAlgorithm).build()
                val decodedJsonWebToken = jwtVerifier.verify(jsonWebToken)

                val premiumKey = PremiumKey(
                    id = decodedJsonWebToken.id,
                    name = decodedJsonWebToken.getClaim(JWT_CLAIM_NAME).asString(),
                    email = decodedJsonWebToken.getClaim(JWT_CLAIM_EMAIL).asString(),
                    company = decodedJsonWebToken.getClaim(JWT_CLAIM_COMPANY).asString(),
                    expirationDate = decodedJsonWebToken.expiresAt?.toInstant()
                )

                Success(premiumKey)
            } catch (exception: Exception) {
                Failure(exception)
            }
        }
    }
}

object PremiumKeyRequiredException : Exception("A premium key is required for this operation!")
