import cats.effect.{ExitCode, IO, IOApp, Resource}

import java.nio.file.{Files, Paths}
import java.util.stream
import scala.annotation.tailrec
import scala.jdk.StreamConverters._

object ExpenseReports extends IOApp {

  // These IO methods will likely be extracted to a common module
  private def linesResource(filename: String): Resource[IO, stream.Stream[String]] = {
    val acquire: IO[stream.Stream[String]] = IO.delay {
      Files.lines(Paths.get(filename))
    }
    def release(s: stream.Stream[String]): IO[Unit] = IO.delay(s.close())
    Resource.make(acquire)(release)
  }

  private def withLines[T](filename: String)(block: => LazyList[String] => IO[T]): IO[T] = {
    linesResource(filename).use { javaStream =>
      block(javaStream.toScala(LazyList))
    }
  }
  @tailrec
  private def entrySearch(expenses: LazyList[Int], addsTo: Int, visited: Set[Int]): Option[(Int, Int)] = {
    expenses match {
      case head #:: _ if visited(addsTo - head) => Some((head, addsTo - head))
      case head #:: tail => entrySearch(tail, addsTo, visited + head)
      case LazyList() => None
    }
  }

  def day1(): IO[Option[Int]] = {
    withLines("../inputs/sample.txt") { stream =>
      // TODO exceptions/applicative flow
      IO.delay(entrySearch(stream.map { line => Integer.parseInt(line)}, 2020, Set()).map { case (first, second) => first * second})
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    day1().unsafeRunSync() match {
      case Some(solution) => IO { println(s"Day 1: $solution")}.as(ExitCode.Success)
      case None => IO { println("Day 1: Couldn't find a solution")}.as(ExitCode.Error)
    }
  }
}
