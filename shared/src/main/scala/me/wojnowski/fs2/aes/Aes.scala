package me.wojnowski.fs2.aes

import fs2.Chunk
import fs2.Pipe
import fs2.Pull
import fs2.RaiseThrowable
import fs2.Stream

import cats.effect.Sync
import cats.effect.std.SecureRandom
import cats.syntax.all._

import scala.util.control.NonFatal

import java.nio.ByteBuffer

object Aes extends AesKeyGenerator {

  private[aes] val IntSizeInBytes     = 4
  private[aes] val IvLengthBytes      = 12
  private[aes] val AuthTagLengthBytes = 16
  private val DefaultChunkSize        = 4 * 1024 * 1024

  def decrypt[F[_]: Sync](key: SecretKey): Pipe[F, Byte, Byte] =
    (stream: Stream[F, Byte]) =>
      readFirstN(IntSizeInBytes, stream) { (chunkSizeBytes, remainingStream) =>
        val chunkSize = bytesToChunkSize(chunkSizeBytes)

        remainingStream
          .chunkN(IvLengthBytes + chunkSize + AuthTagLengthBytes)
          .flatMap { chunk =>
            readFirstN(IvLengthBytes, Stream.chunk(chunk).covary[F]) { (ivChunk, stream) =>
              Stream.eval(Cipher.makeDecrypt(key, ivChunk.toArray)).flatMap { cipher =>
                stream.mapChunks(cipher.cipherChunk)
              }
            }
          }
          .adaptError { case NonFatal(throwable) =>
            Error.DecryptionError(throwable)
          }

      }

  def encrypt[F[_]: Sync: SecureRandom](key: SecretKey, chunkSize: Int = DefaultChunkSize): Pipe[F, Byte, Byte] =
    (stream: Stream[F, Byte]) =>
      Stream.chunk(chunkSizeToBytes(chunkSize)) ++
        stream
          .chunkN(chunkSize)
          .flatMap { chunk =>
            Stream.eval(SecureRandom[F].nextBytes(IvLengthBytes)).flatMap { ivBytes =>
              Stream.eval(Cipher.makeEncrypt[F](key, ivBytes)).flatMap { cipher =>
                Stream.chunk(Chunk.array(ivBytes)) ++ Stream.chunk(cipher.cipherChunk(chunk))
              }
            }
          }
          .adaptError { case NonFatal(throwable) =>
            Error.EncryptionError(throwable)
          }

  private def readFirstN[F[_]: RaiseThrowable, A, B](
    n: Int,
    stream: Stream[F, A]
  )(
    f: (Chunk[A], Stream[F, A]) => Stream[F, B]
  ): Stream[F, B] =
    stream
      .pull
      .unconsN(n)
      .flatMap {
        case Some((chunk, stream)) =>
          f(chunk, stream).pull.echo
        case None                  =>
          Pull.raiseError(Error.DataTooShort)
      }
      .stream

  private def chunkSizeToBytes(chunkSize: Int): Chunk[Byte] = {
    val buffer = ByteBuffer.allocate(IntSizeInBytes)
    buffer.putInt(chunkSize)
    Chunk.byteBuffer(buffer.rewind())
  }

  private def bytesToChunkSize(bytes: Chunk[Byte]): Int =
    bytes.toByteBuffer.getInt

  sealed abstract class Error(message: String, cause: Option[Throwable] = None)
    extends Exception(message, cause.orNull)
    with Product
    with Serializable

  object Error {
    case object DataTooShort extends Error("Data too short")

    case class EncryptionError(cause: Throwable) extends Error(s"Error during encryption: $cause", cause.some)

    case class DecryptionError(cause: Throwable) extends Error(s"Error during decryption: $cause", cause.some)
  }

}
