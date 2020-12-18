package de.passbutler.desktop.crypto

import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.crypto.BiometricsProviding
import javax.crypto.Cipher

class BiometricsProvider : BiometricsProviding {
    override val isBiometricAvailable: Boolean
        get() {
            // Not yet implemented
            return false
        }

    override fun obtainKeyInstance(): Result<Cipher> {
        return Failure(NotYetImplementedException)
    }

    override suspend fun generateKey(keyName: String): Result<Unit> {
        return Failure(NotYetImplementedException)
    }

    override suspend fun initializeKeyForEncryption(keyName: String, encryptionCipher: Cipher): Result<Unit> {
        return Failure(NotYetImplementedException)
    }

    override suspend fun initializeKeyForDecryption(keyName: String, decryptionCipher: Cipher, initializationVector: ByteArray): Result<Unit> {
        return Failure(NotYetImplementedException)
    }

    override suspend fun encryptData(encryptionCipher: Cipher, data: ByteArray): Result<ByteArray> {
        return Failure(NotYetImplementedException)
    }

    override suspend fun decryptData(decryptionCipher: Cipher, data: ByteArray): Result<ByteArray> {
        return Failure(NotYetImplementedException)
    }

    override suspend fun removeKey(keyName: String): Result<Unit> {
        return Failure(NotYetImplementedException)
    }

    object NotYetImplementedException : UnsupportedOperationException("Not yet implemented!")
}