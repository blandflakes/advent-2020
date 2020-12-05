import cats.effect.{IO, Resource}

import java.nio.file.{Files, Paths}
import java.util.stream
import scala.jdk.StreamConverters._


package object io {
  // These IO methods will likely be extracted to a common module
  def linesResource(filename: String): Resource[IO, stream.Stream[String]] = {
    val acquire: IO[stream.Stream[String]] = IO.delay {
      Files.lines(Paths.get(filename))
    }
    def release(s: stream.Stream[String]): IO[Unit] = IO.delay(s.close())
    Resource.make(acquire)(release)
  }

  def withLines[T](filename: String)(block: => LazyList[String] => IO[T]): IO[T] =
    linesResource(filename).use { javaStream =>
      block(javaStream.toScala(LazyList))
    }
}
