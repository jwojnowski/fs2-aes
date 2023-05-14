# fs2-aes
[![License](http://img.shields.io/:license-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/me.wojnowski/fs2-aes_3.svg?color=blue)](https://search.maven.org/search?q=fs2-aes)

This is a micro library providing AES encryption/decryption of `fs2.Stream[F, Byte]`.

It uses AES GCM (Galois/Counter Mode) without padding.

It is published for Scala 2.13.x and 3.x and uses (and builds on) Cats Effect 3 and fs2 3.

## Getting started

### Adding the dependency to SBT
```scala
"me.wojnowski" %% "fs2-aes" % "x.y.z"
```
[![Maven Central](https://img.shields.io/maven-central/v/me.wojnowski/fs2-aes_3.svg?color=blue)](https://search.maven.org/search?q=fs2-aes)

### Encryption
#### The juicy part
```scala
val streamOfBytes: fs2.Stream[F, Byte] = ...

val encryptedStreamOfBytes: fs2.Stream[F, Byte] = 
  streamOfBytes.through(Aes.encrypt(key, chunkSize))
```

#### Full example


```scala
import me.wojnowski.fs2.aes.Aes

import cats.effect.std.SecureRandom
import fs2.Stream

import javax.crypto.SecretKey


val streamOfBytes: Stream[F, Byte] = ...

val key: SecretKey = ... // hint: Aes.keyFromHex

SecureRandom.javaSecuritySecureRandom[F].flatMap { implicit secureRandom =>
  val encryptedStreamOfBytes = streamOfBytes.through(Aes.encrypt(key))
  ...
}
```

### Decryption
#### Juicy part
```scala
val encryptedStreamOfBytes: fs2.Stream[F, Byte] = ...

val decryptedStreamOfBytes: fs2.Stream[F, Byte] =
  streamOfBytes.through(Aes.decrypt(key))
```

#### Full example


```scala
import me.wojnowski.fs2.aes.Aes

import fs2.Stream

import javax.crypto.SecretKey


val encryptedStreamOfBytes: Stream[F, Byte] = ...

val key: SecretKey = ... // hint: Aes.keyFromHex

val decryptedStreamOfBytes = streamOfBytes.through(Aes.decrypt(key))
```

## SecretKey utilities
### Reading keys
There's a handy `keyFromHex` method provided:

```scala
import me.wojnowski.fs2.aes.Aes

val maybeKey: Option[SecretKey] =
  Aes.keyFromHex("8460623cf2eb7059f5a2f653513cfe66d9c21196a330414d4b3bf1f3f838d884") 

```

### Generating keys
`Aes.generateKeyHexString[IO]` method might be used to generate a key like in a following example:
```scala
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.std.SecureRandom

object KeyGenerator extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      secureRandom <- SecureRandom.javaSecuritySecureRandom[IO]
      key          <- Aes.generateKeyHexString[IO](256)(secureRandom, implicitly)
      _            <- IO.println(s"😱 Secret: $key")
    } yield ()

}
```

## Chunk size
For processing of large data it is useful to split it into smaller chunks.
This library assumes the chunk size is constant throughout the whole stream,
which it ensures by re-chunking it to provided chunk size.

While a default chunk size of 4 MiBs is provided, it **is strongly advised to use
a custom chunk size** suited for a particular use-case.

Chunk size used for encryption is embedded at in the encrypted stream, so it would
be automatically read during encryption. For details, take a look at Data format section.

## Data format
The encrypted stream consist of:
1. Chunk size [4 bytes]
2. Chunks of encrypted data
   1. Initialisation Vector (IV) [12 bytes]
   2. Encrypted data [ChunkSize]
   3. Authentication Tag [16 bytes]

Therefore, the resulting stream/file size will increase by 4 bytes and 28 bytes per each chunk.