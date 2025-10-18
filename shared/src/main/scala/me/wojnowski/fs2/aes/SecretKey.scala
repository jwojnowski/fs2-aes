package me.wojnowski.fs2.aes

import scala.util.Try

final case class SecretKey(bytes: Array[Byte]) extends AnyVal

object SecretKey {
  def fromHex(hexString: String): Option[SecretKey] =
    Try {
      SecretKey(hexString.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte))
    }.toOption
}
