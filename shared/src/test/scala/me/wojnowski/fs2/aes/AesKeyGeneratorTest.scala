package me.wojnowski.fs2.aes

import cats.effect.IO

import munit.CatsEffectSuite

class AesKeyGeneratorTest extends CatsEffectSuite with ResourceReader {
  test("AES key generation") {
    val randomBytes = readResource("random-bytes.bin")

    SecureRandomMock.ringBuffer[IO](randomBytes).flatMap { implicit secureRandom =>
      for {
        key1 <- Aes.generateKeyHexString(128)
        key2 <- Aes.generateKeyHexString(192)
        key3 <- Aes.generateKeyHexString(256)
      } yield {
        assertEquals(key1, expected = "8ab7e6b58fed8fbde68089e9a2a7e7a1")
        assertEquals(key2, expected = "85e99286e4a5ace6a9bddaaae7a090e1a596eb84bed1b7e9")
        assertEquals(key3, expected = "92aced878ae6b29deba4a6e1be9de78988e386bdeba0beea9f90e1aebcecb596")
      }
    }
  }
}
