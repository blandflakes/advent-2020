import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

import scala.annotation.tailrec

object EncodingError extends IOApp {

  def part1(): IO[Option[Long]] = withLines("../inputs/day9.txt") { stream =>
    IO {
      stream.map(_.toLong).sliding(26, 1).map { window =>
        val summands: Seq[Long] = window.take(window.size - 1)
        val sums: Set[Long] = summands.foldLeft(Set[Long]()) { case (acc, summand1) =>
          acc ++ summands.foldLeft(Set[Long]()){ case (internalAcc, summand2) =>
            if (summand1 != summand2) {
              internalAcc + (summand1 + summand2)
            }
            else {
              internalAcc
            }
          }
        }

        if (sums(window.last)) {
          None
        }
        else {
          Some(window.last)
        }
      }.find(_.isDefined).flatten
    }
  }

  @tailrec
  def seekContiguousSummands(numbers: List[Long], targetSum: Long, windowSize: Int = 2): Option[List[Long]] = {
    if (windowSize > numbers.length) {
      None
    } else {
      numbers.sliding(windowSize, 1).find(_.sum == targetSum) match {
        case found: Some[List[Long]] => found
        case None => seekContiguousSummands(numbers, targetSum, windowSize + 1)
      }
    }
  }

  def part2(): IO[Option[Long]] = withLines("../inputs/day9.txt") { stream =>
    IO {
      stream.map(_.toLong)
    }.map { numbers =>
      seekContiguousSummands(numbers.toList, 144381670).map(segment => segment.min + segment.max)
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
