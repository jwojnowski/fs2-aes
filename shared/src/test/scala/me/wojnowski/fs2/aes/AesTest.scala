package me.wojnowski.fs2.aes

import fs2.Stream

import cats.effect.IO

import munit.CatsEffectSuite
import munit.ScalaCheckSuite

class AesTest extends CatsEffectSuite with ScalaCheckSuite with ResourceReader {
  val key1: SecretKey = SecretKey.fromHex("c0e5c54c2a40c95b40d6e837a9c147d4cd7cadeccc555e679efed48f726a5fe5").get
  val key2: SecretKey = SecretKey.fromHex("8460623cf2eb7059f5a2f653513cfe66d9c21196a330414d4b3bf1f3f838d884").get

  val plainText1  = readResource("plaintext1.txt")
  val plainText2  = readResource("plaintext2.txt")
  val cipherText1 = readResource("ciphertext1.bin")
  val cipherText2 = readResource("ciphertext2.bin")

  val randomBytes = readResource("random-bytes.bin")

  val ChunkSize = 256

  override def scalaCheckTestParameters =
    super
      .scalaCheckTestParameters
      .withMinSuccessfulTests(200)
      .withMaxDiscardRatio(10)

  test("Data larger than chunk size is encrypted properly") {
    SecureRandomMock.ringBuffer[IO](randomBytes).flatMap { implicit secureRandom =>
      for {
        result <- Stream
                    .emits(plainText1)
                    .covary[IO]
                    .through(Aes.encrypt(key1, chunkSize = ChunkSize))
                    .compile
                    .to(Array)
      } yield assert(result.sameElements(cipherText1))
    }
  }
  test("Data larger than chunk size is decrypted properly") {
    for {
      result <- Stream
                  .emits(cipherText1)
                  .covary[IO]
                  .through(Aes.decrypt(key1))
                  .compile
                  .to(Array)
    } yield assert(result.sameElements(plainText1))
  }

  test("Data smaller than chunk size is encrypted properly") {
    SecureRandomMock.ringBuffer[IO](randomBytes).flatMap { implicit secureRandom =>
      for {
        result <- Stream
                    .emits(plainText2)
                    .covary[IO]
                    .through(Aes.encrypt(key1, chunkSize = ChunkSize))
                    .compile
                    .to(Array)
      } yield assert(result.sameElements(cipherText2))
    }
  }

  test("Data smaller than chunk size is decrypted properly") {
    for {
      result <- Stream
                  .emits(cipherText2)
                  .covary[IO]
                  .through(Aes.decrypt(key1))
                  .compile
                  .to(Array)
    } yield assert(result.sameElements(plainText2))
  }

  test("Data can't be decrypted with another key") {
    for {
      result <- Stream
                  .emits(cipherText1)
                  .covary[IO]
                  .through(Aes.decrypt(key2))
                  .compile
                  .to(Array)
                  .attempt
    } yield assert(result.isLeft) // TODO checking the type of the error?
  }

  test("Decrypting a stream of insufficient data fails") {
    for {
      result <- Stream(42.toByte, -42.toByte)
                  .covary[IO]
                  .through(Aes.decrypt(key1))
                  .compile
                  .to(Array)
                  .attempt
    } yield assert(result.isLeft) // TODO checking the type of the error?
  }

  test("Encrypting and decrypting empty data") {
    SecureRandomMock.ringBuffer[IO](randomBytes).flatMap { implicit secureRandom =>
      val data = Array[Byte]()
      for {
        result <- Stream
                    .emits(data)
                    .covary[IO]
                    .through(Aes.encrypt(key1))
                    .through(Aes.decrypt(key1))
                    .compile
                    .to(Array)
      } yield assert(result.sameElements(data))
    }
  }

}
