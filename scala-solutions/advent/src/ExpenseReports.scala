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

  private def withLines[T](filename: String)(block: => LazyList[String] => IO[T]): IO[T] =
    linesResource(filename).use { javaStream =>
      block(javaStream.toScala(LazyList))
    }
  @tailrec
  private def entrySearch(expenses: LazyList[Int], addsTo: Int, visited: Set[Int]): Option[(Int, Int)] =
    expenses match {
      case head #:: _ if visited(addsTo - head) => Some((head, addsTo - head))
      case head #:: tail => entrySearch(tail, addsTo, visited + head)
      case LazyList() => None
    }

  def day1(): IO[Option[Int]] =
    withLines("../inputs/day1.txt") { stream =>
      // TODO exceptions/applicative flow
      IO.delay(entrySearch(stream.map { line => Integer.parseInt(line)}, 2020, Set()).map { case (first, second) => first * second})
    }

  def day2(): IO[Option[Int]] = {
    withLines("../inputs/day1.txt") { stream =>
      IO.delay(stream.map { line => Integer.parseInt(line) }.toSet)
    }.map { expenses =>
      val matchingSolutions = for {
        n1 <- expenses
        n2 <- expenses
        n3 = 2020 - n1 - n2
        if expenses(n3)
        solution = n1 * n2 * n3
      } yield solution
      // Don't really care if there are more than one answer
      matchingSolutions.headOption
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    day1().unsafeRunSync() match {
      case Some(solution) => println(s"Day 1: $solution")
      case None => println("Day 1: Couldn't find a solution")
    }
    day2().unsafeRunSync() match {
      case Some(solution) => println(s"Day 2: $solution")
      case None => println("Day 2: Couldn't find a solution")
    }
    IO(ExitCode.Success)
  }
}
