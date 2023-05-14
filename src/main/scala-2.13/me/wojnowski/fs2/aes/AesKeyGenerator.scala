package me.wojnowski.fs2.aes

import cats.ApplicativeError
import cats.effect.std.SecureRandom
import cats.syntax.all._

trait AesKeyGenerator {

  private val allowedAesKeyLengthsInBits = Set(128, 192, 256)

  private type ThrowableAE[F[_]] = ApplicativeError[F, Throwable]

  def generateKeyHexString[F[_]: SecureRandom: ThrowableAE](lengthInBits: Int): F[String] =
    if (allowedAesKeyLengthsInBits.contains(lengthInBits)) {
      SecureRandom[F]
        .nextBytes(lengthInBits / 8)
        .map(_.map("%02x".format(_)).mkString)
    } else {
      new IllegalArgumentException(
        s"Invalid key length: $lengthInBits. Valid lengths: ${allowedAesKeyLengthsInBits.mkString(", ")}"
      ).raiseError[F, String]
    }

}
