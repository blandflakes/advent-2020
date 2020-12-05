import cats.effect.{ExitCode, IO, IOApp}
import io._

import scala.jdk.StreamConverters._


object TobogganTrajectory extends IOApp {

  private def parseCourse: IO[List[LazyList[Int]]] = withLines("../inputs/day3.txt") { stream =>
    IO {
      stream.foldLeft(List[LazyList[Int]]()) { case (rows, line) =>
        lazy val rowStream: LazyList[Int] = line.chars().toScala(LazyList) #::: rowStream
        rowStream :: rows
      }.reverse
    }
  }

  private def countTrees(right: Int, down: Int): IO[Long] = parseCourse.map { grid =>
    var trees = 0
    var position = (0, 0)
    while (position._1 < grid.size) {
      if (grid(position._1)(position._2) == '#') trees += 1
      // Transposed, because my grid is columnar-oriented
      position = (position._1 + down, position._2 + right)
    }
    trees
  }

  def part1(): IO[Long] = countTrees(3, 1)

  def part2(): IO[Long] = {
    for {
      count1 <- countTrees(1, 1)
      count2 <- countTrees(3, 1)
      count3 <- countTrees(5, 1)
      count4 <- countTrees(7, 1)
      count5 <- countTrees(1, 2)
    } yield count1 * count2 * count3 * count4 * count5
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
