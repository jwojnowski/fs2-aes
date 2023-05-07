package me.wojnowski.fs2.aes

import cats.Applicative
import cats.effect.Ref
import cats.effect.Sync
import cats.effect.std.SecureRandom
import cats.syntax.all._

object SecureRandomMock {

  def ringBuffer[F[_]: Sync](sourceArray: Array[Byte]): F[SecureRandom[F]] =
    Ref[F].of(0).map { ref =>
      bytesFromF(ref.updateAndGet(x => (x + 1) % sourceArray.length).map(sourceArray.apply))
    }

  def bytesFromF[F[_]: Applicative](nextByteF: F[Byte]): SecureRandom[F] = new SecureRandom[F] {
    override def nextBytes(n: Int): F[Array[Byte]] = nextByteF.replicateA(n).map(_.toArray)

    override def betweenDouble(minInclusive: Double, maxExclusive: Double): F[Double] = ???

    override def betweenFloat(minInclusive: Float, maxExclusive: Float): F[Float] = ???

    override def betweenInt(minInclusive: Int, maxExclusive: Int): F[Int] = ???

    override def betweenLong(minInclusive: Long, maxExclusive: Long): F[Long] = ???

    override def nextAlphaNumeric: F[Char] = ???

    override def nextBoolean: F[Boolean] = ???

    override def nextDouble: F[Double] = ???

    override def nextFloat: F[Float] = ???

    override def nextGaussian: F[Double] = ???

    override def nextInt: F[Int] = ???

    override def nextIntBounded(n: Int): F[Int] = ???

    override def nextLong: F[Long] = ???

    override def nextLongBounded(n: Long): F[Long] = ???

    override def nextPrintableChar: F[Char] = ???

    override def nextString(length: Int): F[String] = ???

    override def shuffleList[A](l: List[A]): F[List[A]] = ???

    override def shuffleVector[A](v: Vector[A]): F[Vector[A]] = ???
  }

}
