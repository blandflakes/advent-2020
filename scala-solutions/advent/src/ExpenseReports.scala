import cats.effect.{ExitCode, IO, IOApp}
import io._

import scala.annotation.tailrec

object ExpenseReports extends IOApp {

  @tailrec
  private def entrySearch(expenses: LazyList[Int], addsTo: Int, visited: Set[Int]): Option[(Int, Int)] =
    expenses match {
      case head #:: _ if visited(addsTo - head) => Some((head, addsTo - head))
      case head #:: tail => entrySearch(tail, addsTo, visited + head)
      case LazyList() => None
    }

  def part1(): IO[Option[Int]] =
    withLines("../inputs/day1.txt") { stream =>
      // TODO exceptions/applicative flow
      IO.delay(entrySearch(stream.map { line => Integer.parseInt(line)}, 2020, Set()).map { case (first, second) => first * second})
    }

  def part2(): IO[Option[Int]] = {
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
    part1().unsafeRunSync() match {
      case Some(solution) => println(s"Part 1: $solution")
      case None => println("Part 1: Couldn't find a solution")
    }
    part2().unsafeRunSync() match {
      case Some(solution) => println(s"Part 2: $solution")
      case None => println("Part 2: Couldn't find a solution")
    }
    IO(ExitCode.Success)
  }
}
