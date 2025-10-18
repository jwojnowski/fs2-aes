package me.wojnowski.fs2.aes

import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.scalajs.js.typedarray.Int8Array

import cats.syntax.all.*
import cats.effect.Async
import cats.effect.kernel.Sync

trait ResourceReader {

  def readResource(filename: String): Array[Byte] = {
    val buffer = FS.readFileSync(s"shared/src/test/resources/$filename")

    new Int8Array(buffer.buffer, buffer.byteOffset, buffer.length).toArray
  }

}

@js.native
@JSImport("fs", JSImport.Namespace)
object FS extends js.Object {
  def readFileSync(path: String): js.typedarray.Uint8Array = js.native
}

/* object NodeFs {

  def readFile[F[_]: Async](path: String): F[String] =
    Async[F].fromPromise {
      Sync[F].delay {
        val promise = scala.concurrent.Promise[String]()

        fs.readFile(
          path,
          "utf-8",
          (err: js.Any, data: String) =>
            if (err != null) promise.failure(js.JavaScriptException(err))
            else promise.success(data)
        )
      }
    }

}

@js.native
@JSImport("fs", JSImport.Namespace)
object fs extends js.Object {
  def readFile(path: String, encoding: String, callback: js.Function2[js.Any, String, Unit]): Unit = js.native
}
 */
