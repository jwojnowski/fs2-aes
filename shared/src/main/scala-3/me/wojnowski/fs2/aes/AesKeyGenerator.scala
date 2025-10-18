package me.wojnowski.fs2.aes

import cats.Functor
import cats.effect.std.SecureRandom
import cats.syntax.all.*

trait AesKeyGenerator {

  def generateKeyHexString[F[_]: SecureRandom: Functor](lengthInBits: 128 | 192 | 256): F[String] =
    SecureRandom[F]
      .nextBytes(lengthInBits / 8)
      .map(_.map("%02x".format(_)).mkString)

}
