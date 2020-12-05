import cats.effect.{ExitCode, IO, IOApp}
import io._

import scala.annotation.tailrec

object BoardingPass extends IOApp {

  // let's think mathematically, right?
  // 0 to 7
  // there are 8 elements
  // any midpoint calc is going to pick an int, but what is the actual semantic value of thatint?
  // if we do (lower + upper) / 2, we get 7 / 2 == 3. 3 is the last element of the lower boys. So, in this case,
  // because we're shooting for the upper... ok so if the midpoint skews low (does it?), we can add 1 if we're going up, and not if we're going down

  // 0 1 2 3 4 5 6 7
  @tailrec
  private def calculateRow(operations: List[Char], lowerBound: Int, upperBound: Int): Int = operations match {
    case 'F' :: Nil => lowerBound
    case 'B' :: Nil => upperBound
    case 'F' :: tail => calculateRow(tail, lowerBound, (lowerBound + upperBound) / 2)
    case 'B' :: tail => calculateRow(tail, (lowerBound + upperBound) / 2 + 1, upperBound)
  }

  @tailrec
  private def calculateColumn(operations: List[Char], lowerBound: Int, upperBound: Int): Int = operations match {
    case 'L' :: Nil => lowerBound
    case 'R' :: Nil => upperBound
    case 'L' :: tail => calculateColumn(tail, lowerBound, (lowerBound + upperBound) / 2)
    case 'R' :: tail => calculateColumn(tail, (lowerBound + upperBound) / 2 + 1, upperBound)
  }

  private def calcSeats(lines: LazyList[String]) = for {
    line <- lines
    rowOps = line.substring(0, 7).toList
    colOps = line.substring(7).toList
    row = calculateRow(rowOps, 0, 127)
    col = calculateColumn(colOps, 0, 7)
  } yield row * 8 + col

  def part1(): IO[Int] = {
    withLines("../inputs/day5.txt") { stream =>
      IO.delay{
        calcSeats(stream).max
      }
    }
  }

  def part2(): IO[Int] = withLines("../inputs/day5.txt") { stream =>
    IO.delay {
      calcSeats(stream).sorted.sliding(2, 1).find { twoSeats => twoSeats.head + 2 == twoSeats(1)}.map { twoSeats => twoSeats.head + 1 }.head
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
