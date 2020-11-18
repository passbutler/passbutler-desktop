package de.passbutler.desktop.crypto

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
        TODO("Not yet implemented")
    }

    override suspend fun generateKey(keyName: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun initializeKeyForEncryption(keyName: String, encryptionCipher: Cipher): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun initializeKeyForDecryption(keyName: String, decryptionCipher: Cipher, initializationVector: ByteArray): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun encryptData(encryptionCipher: Cipher, data: ByteArray): Result<ByteArray> {
        TODO("Not yet implemented")
    }

    override suspend fun decryptData(decryptionCipher: Cipher, data: ByteArray): Result<ByteArray> {
        TODO("Not yet implemented")
    }

    override suspend fun removeKey(keyName: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}