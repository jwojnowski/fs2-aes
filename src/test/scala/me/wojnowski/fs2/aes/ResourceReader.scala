package me.wojnowski.fs2.aes

trait ResourceReader {
  def readResource(filename: String): Array[Byte] =
    getClass.getClassLoader.getResourceAsStream(filename).readAllBytes()
}
