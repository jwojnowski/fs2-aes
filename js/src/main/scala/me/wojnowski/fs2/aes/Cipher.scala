package me.wojnowski.fs2.aes

import cats.effect.kernel.Sync
import fs2.Chunk
import scala.scalajs.js
import scala.scalajs.js.typedarray.Int8Array
import scala.scalajs.js.typedarray.Uint8Array
import Aes.AuthTagLengthBytes
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.typedarray.ArrayBufferView
import scala.scalajs.js.typedarray._

final class Cipher private (
  encrypting: Boolean,
  algorithm: String,
  keyBytes: Array[Byte],
  ivBytes: Array[Byte]
) {

  def cipherChunk(chunk: Chunk[Byte]): Chunk[Byte] =
    if (encrypting) {
      val cipher  = NodeCrypto.createCipheriv(algorithm, keyBytes.toTypedArray, ivBytes.toTypedArray)
      
      val p1      = cipher.update(chunk.toArray.toTypedArray)
      val p2      = cipher.`final`()
      
      val authTag = cipher.getAuthTag()

      Chunk.array(toScalaArray(p1) ++ toScalaArray(p2) ++ toScalaArray(authTag))
    } else {
      val cipher          = NodeCrypto.createDecipheriv(algorithm, keyBytes.toTypedArray, ivBytes.toTypedArray)
      
      val (data, authTag) = chunk.toArray.splitAt(chunk.toArray.length - AuthTagLengthBytes)

      cipher.setAuthTag(authTag.toTypedArray)

      val p1 = cipher.update(data.toTypedArray)
      val p2 = cipher.`final`()

      Chunk.array(toScalaArray(p1) ++ toScalaArray(p2))
    }

  private def toScalaArray(input: Uint8Array): Array[Byte] =
    new Int8Array(input.buffer, input.byteOffset, input.length).toArray

}

object Cipher {
  def makeEncrypt[F[_]: Sync](key: SecretKey, ivBytes: Array[Byte]): F[Cipher] =
    Sync[F].delay(new Cipher(encrypting = true, "aes-256-gcm", key.bytes, ivBytes))

  def makeDecrypt[F[_]: Sync](key: SecretKey, ivBytes: Array[Byte]): F[Cipher] =
    Sync[F].delay(new Cipher(encrypting = false, "aes-256-gcm", key.bytes, ivBytes))
}

@js.native
trait NodeCipher extends js.Object {
  def update(data: ArrayBufferView): Uint8Array                                         = js.native
  def `final`(): Uint8Array                                                             = js.native
  def setAAD(aad: ArrayBufferView, options: js.UndefOr[js.Object] = js.undefined): Unit = js.native
  def getAuthTag(): Uint8Array                                                          = js.native
}

@js.native
trait NodeDecipher extends js.Object {
  def update(data: ArrayBufferView): Uint8Array                                         = js.native
  def `final`(): Uint8Array                                                             = js.native
  def setAAD(aad: ArrayBufferView, options: js.UndefOr[js.Object] = js.undefined): Unit = js.native
  def setAuthTag(tag: ArrayBufferView): Unit                                            = js.native
}

@js.native
@JSImport("node:crypto", JSImport.Namespace)
object NodeCrypto extends js.Object {

  def createCipheriv(
    algorithm: String,
    key: ArrayBufferView,
    iv: ArrayBufferView,
    options: js.UndefOr[js.Object] = js.undefined
  ): NodeCipher = js.native

  def createDecipheriv(
    algorithm: String,
    key: ArrayBufferView,
    iv: ArrayBufferView,
    options: js.UndefOr[js.Object] = js.undefined
  ): NodeDecipher = js.native

}
