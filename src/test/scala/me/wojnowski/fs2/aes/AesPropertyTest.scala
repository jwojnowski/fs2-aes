package me.wojnowski.fs2.aes

import fs2.Stream

import cats.effect.IO

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

import javax.crypto.SecretKey

import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite

class AesPropertyTest extends CatsEffectSuite with ScalaCheckEffectSuite {

  override def munitTimeout: Duration = 120.seconds

  val chunkSizeGenerator = Gen.frequency(
    (80, Gen.choose(256, 16 * 1024)),
    (10, Gen.choose(1, 256)),
    (10, Gen.choose(16 * 1024, 4 * 1024 * 1024))
  )

  val secretKeyGen: Gen[SecretKey] = Gen.stringOfN(64, Gen.hexChar).map(Aes.keyFromHex(_).get)

  def arrayOfByteGenerator(size: Int): Gen[Array[Byte]] = Gen.containerOfN[Array, Byte](size, arbitrary[Byte])

  val plaintextGenerator =
    Gen.frequency(
      (80, Gen.choose(1024, 1024 * 1024).flatMap(arrayOfByteGenerator)),
      (10, Gen.choose(1024 * 1024, 16 * 1024 * 1024).flatMap(arrayOfByteGenerator)),
      (10, Gen.choose(0, 1024).flatMap(arrayOfByteGenerator))
    )

  case class KeyPlainTextAndChunkSize(key: SecretKey, plainText: Array[Byte], chunkSize: Int, randomBytes: Array[Byte])

  val dataSetGenerator =
    for {
      key         <- secretKeyGen
      plainText   <- plaintextGenerator
      chunkSize   <- chunkSizeGenerator
      randomBytes <- arrayOfByteGenerator(128)
    } yield KeyPlainTextAndChunkSize(key, plainText, chunkSize, randomBytes)

  test("Encrypted data can be decrypted using the same key and is of predicted length".ignore) {
    forAllF(dataSetGenerator) { case KeyPlainTextAndChunkSize(key, plainText, chunkSize, randomBytes) =>
      SecureRandomMock.ringBuffer[IO](randomBytes).flatMap { implicit secureRandom =>
        for {
          result <- Stream
                      .emits(plainText)
                      .covary[IO]
                      .through(Aes.encrypt[IO](key, chunkSize))
                      .through(Aes.decrypt[IO](key))
                      .compile
                      .to(Array)
        } yield assert(result.sameElements(plainText))
      }
    }
  }

  test("Encrypted data is of predicted length".ignore) {
    forAllF(dataSetGenerator) { case KeyPlainTextAndChunkSize(key, plainText, chunkSize, randomBytes) =>
      SecureRandomMock.ringBuffer[IO](randomBytes).flatMap { implicit secureRandom =>
        for {
          result <- Stream
                      .emits(plainText)
                      .covary[IO]
                      .through(Aes.encrypt[IO](key, chunkSize))
                      .compile
                      .to(Array)
        } yield assertEquals(result.length, calculateLength(plainText.length, chunkSize))
      }
    }
  }

  test("Encrypted data can't be decrypted using another key") {
    val wrongKey: SecretKey = Aes.keyFromHex("c0e5c54c2a40c95b40d6e837a9c147d4cd7cadeccc555e679efed48f726a5fe5").get

    forAllF(dataSetGenerator.suchThat(_.plainText.length > 0)) { case KeyPlainTextAndChunkSize(key, plainText, chunkSize, randomBytes) =>
      SecureRandomMock.ringBuffer[IO](randomBytes).flatMap { implicit secureRandom =>
        for {
          result <- Stream
                      .emits(plainText)
                      .covary[IO]
                      .through(Aes.encrypt[IO](key, chunkSize))
                      .through(Aes.decrypt[IO](wrongKey))
                      .compile
                      .to(Array)
                      .attempt
        } yield assert(result.isLeft)
      }
    }
  }

  private def calculateLength(originalLength: Int, chunkSize: Int) = {
    val numberOfChunks = (originalLength + chunkSize - 1) / chunkSize

    Aes.IntSizeInBytes + originalLength + numberOfChunks * (Aes.IvLengthBytes + Aes.AuthTagLengthBytes)
  }

}
