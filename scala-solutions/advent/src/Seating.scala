import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

import scala.annotation.tailrec

object Seating extends IOApp {

  private def countOccupiedNeighbors(rowIndex: Int, colIndex: Int, grid: Array[Array[Char]]) = {
    var count = 0
    for (x <- Math.max(rowIndex - 1, 0) to Math.min(rowIndex + 1, grid.length - 1)) {
      val row = grid(x)
      for (y <- Math.max(colIndex - 1, 0) to Math.min(colIndex + 1, row.length - 1)) {
        if (!(x == rowIndex && y == colIndex) && row(y) == '#')
          count += 1
      }
    }
    count
  }

  private def countLineOfSightOccupiedNeighbors(rowIndex: Int, colIndex: Int, grid: Array[Array[Char]]) = {
    var count = 0
    // Ok so what are the directions?
    // Search "down"
    var y = rowIndex + 1
    var stop = false
    while (y < grid.length && !stop) {
      grid(y)(colIndex) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      y += 1
    }
    // Search "up"
    y = rowIndex - 1
    stop = false
    while (y >= 0 && !stop) {
      grid(y)(colIndex) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      y -= 1
    }
    // Search "right"
    val row = grid(rowIndex)
    var x = colIndex + 1
    stop = false
    while (x < row.length && !stop) {
      row(x) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      x += 1
    }
    // Search "left"
    x = colIndex - 1
    stop = false
    while (x >= 0 && !stop) {
      row(x) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      x -= 1
    }
    // Ok, 4 more exhausting directions.
    // "down-right"
    x = colIndex + 1
    y = rowIndex - 1
    stop = false
    while (x < row.length && y >= 0 && !stop) {
      grid(y)(x) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      x += 1
      y -= 1
    }
    // "down-left"
    x = colIndex  - 1
    y = rowIndex - 1
    stop = false
    while (x >= 0 && y >= 0 && !stop) {
      grid(y)(x) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      x -= 1
      y -= 1
    }
    // "up-right"
    x = colIndex + 1
    y = rowIndex + 1
    stop = false
    while (x < row.length && y < grid.length && !stop) {
      grid(y)(x) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      x += 1
      y += 1
    }
    // "up-left"
    x = colIndex  - 1
    y = rowIndex + 1
    stop = false
    while (x >= 0 && y < grid.length && !stop) {
      grid(y)(x) match {
        case '#' =>
          count += 1
          stop = true
        case 'L' =>
          stop = true
        case _ =>
      }
      x -= 1
      y += 1
    }
    count
  }

  private def mutate(grid: Array[Array[Char]], countNeighbors: (Int, Int, Array[Array[Char]]) => Int, swapThreshold: Int): Array[Array[Char]] = {
    grid.zipWithIndex.map { case (row, indexOfRow) =>
      row.zipWithIndex.map { case (cell, indexOfColumn) =>
        cell match {
          case '#' =>
            if (countNeighbors(indexOfRow, indexOfColumn, grid) >= swapThreshold)
              'L'
            else
              '#'
          case 'L' =>
            if (countNeighbors(indexOfRow, indexOfColumn, grid) == 0)
              '#'
            else
              'L'
            // if no neighbors are occupied, set to occupied
          case other => other
        }
      }
    }
  }

  @tailrec
  def mutateUntilStable(grid: Array[Array[Char]], countNeighbors: (Int, Int, Array[Array[Char]]) => Int, swapThreshold: Int): Array[Array[Char]] = {
    val next = mutate(grid, countNeighbors, swapThreshold)
    if (next.zip(grid).forall {case (left, right) => left sameElements right})
      grid
    else
      mutateUntilStable(next, countNeighbors, swapThreshold)
  }

  def part1: IO[Int] = for {
    initialGrid <- withLines("../inputs/day11.txt") { stream =>
      // This grid is row[cols]
      IO(stream.map { line => line.toCharArray }.toArray)
    }
    stable <- IO(mutateUntilStable(initialGrid, countOccupiedNeighbors, 4))
    count <- IO(stable.map(_.count(_ == '#')).sum)
  } yield count

  def part2: IO[Int] = for {
    initialGrid <- withLines("../inputs/day11.txt") { stream =>
      // This grid is row[cols]
      IO(stream.map { line => line.toCharArray }.toArray)
    }
    stable <- IO(mutateUntilStable(initialGrid, countLineOfSightOccupiedNeighbors, 5))
    count <- IO(stable.map(_.count(_ == '#')).sum)
  } yield count

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1.unsafeRunSync()}")
    println(s"Part 2: ${part2.unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
