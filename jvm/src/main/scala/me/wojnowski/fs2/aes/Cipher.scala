package me.wojnowski.fs2.aes

import fs2.Chunk
import cats.effect.kernel.Sync
import javax.crypto.Cipher as JavaCipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import Aes.AuthTagLengthBytes
import java.nio.ByteBuffer

class Cipher private (cipher: JavaCipher) {

  def cipherChunk(chunk: Chunk[Byte]): Chunk[Byte] = {
    val inputBuffer  = chunk.toByteBuffer
    val outputBuffer = ByteBuffer.allocate(cipher.getOutputSize(inputBuffer.remaining()))
    
    cipher.doFinal(inputBuffer, outputBuffer)

    Chunk.byteBuffer(outputBuffer.rewind())
  }

}

object Cipher {
  private val transformation = "AES/GCM/NoPadding"
  private val keyAlgorithm   = "AES"

  private type Mode = Int

  def makeEncrypt[F[_]: Sync](key: SecretKey, ivBytes: Array[Byte]): F[Cipher] =
    makeImpl[F](JavaCipher.ENCRYPT_MODE, key, ivBytes)

  def makeDecrypt[F[_]: Sync](key: SecretKey, ivBytes: Array[Byte]): F[Cipher] =
    makeImpl[F](JavaCipher.DECRYPT_MODE, key, ivBytes)

  private def makeImpl[F[_]: Sync](mode: Mode, key: SecretKey, ivBytes: Array[Byte]): F[Cipher] =
    Sync[F].delay {
      val cipher = JavaCipher.getInstance(transformation)
      cipher.init(mode, new SecretKeySpec(key.bytes, keyAlgorithm), new GCMParameterSpec(AuthTagLengthBytes * 8, ivBytes))

      new Cipher(cipher)
    }

}
