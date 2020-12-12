import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

object Navigation extends IOApp {
  sealed abstract class Direction
  final case object East extends Direction
  final case object West extends Direction
  final case object North extends Direction
  final case object South extends Direction
  final case class Instruction(operation: Char, value: Int)

  private def withDirections[T](block: => LazyList[Instruction] => T): IO[T] = withLines("../inputs/day12.txt") { stream =>
    IO(block(
      stream.map { line =>
      Instruction(line.head, Integer.parseInt(line.tail))})
    )
  }

  private val directions: Array[Direction] = Array(North, East, West, South)
  private def shiftDirection(degrees: Int, currentDirection: Direction): Direction = {
    val positionShift = degrees / 90
    val currentPosition = directions.indexOf(currentDirection)
    val newPosition = (currentPosition + positionShift) % directions.length
    if (newPosition < 0)
      directions(directions.length + newPosition)
    else
      directions(newPosition)
  }

  def part1: IO[Int] = {
    val origin = (0, 0)
    val direction: Direction = East
    withDirections { directions =>
      directions.foldLeft((origin, direction)) { case ((location, currentDirection), instruction: Instruction) =>
        instruction match {
          case Instruction('N', units) =>
            val newLocation = (location._1 + units, location._2)
            (newLocation, currentDirection)
          case Instruction('S', units) =>
            val newLocation = (location._1 - units, location._2)
            (newLocation, currentDirection)
          case Instruction('E', units) =>
            val newLocation = (location._1, location._2 + units)
            (newLocation, currentDirection)
          case Instruction('W', units) =>
            val newLocation = (location._1, location._2 - units)
            (newLocation, currentDirection)
          case Instruction('L', degrees) =>
            (location, shiftDirection(-degrees, currentDirection))
          case Instruction('R', degrees) =>
            (location, shiftDirection(degrees, currentDirection))
          case Instruction('F', units) =>
            currentDirection match {
              case North =>
                val newLocation = (location._1 + units, location._2)
                (newLocation, currentDirection)
              case South =>
                val newLocation = (location._1 - units, location._2)
                (newLocation, currentDirection)
              case East =>
                val newLocation = (location._1, location._2 + units)
                (newLocation, currentDirection)
              case West =>
                val newLocation = (location._1, location._2 - units)
                (newLocation, currentDirection)
            }
        }
      }
    }.map { case (finalLocation, _) =>
      finalLocation._1.abs + finalLocation._2.abs
    }
  }

  case class Waypoint(x: Int,  y: Int)

  private def rotate(waypoint: Waypoint, degrees: Int) = {
    // maybe just normalize negatives to the positive
    val actualDegrees = if (degrees < 0) 360 + degrees else degrees
    val newWaypoint = actualDegrees match {
      case 90 =>
        // Example: 2, 0 -> 0, -2
        Waypoint(waypoint.y, -waypoint.x)
      case 180 =>
        // So i.e. 2, 0 -> -2, 0
        Waypoint(-waypoint.x, -waypoint.y)
      case 270 =>
        // Example: 2, 0 -> 0, 2
        Waypoint(-waypoint.y, waypoint.x)
    }
    println(s"$waypoint rotated $degrees is $newWaypoint")
    newWaypoint
  }

  def part2: IO[Int] = {
    withDirections { directions =>
      directions.foldLeft(((0, 0), Waypoint(10, 1))) { case ((location, waypoint), instruction: Instruction) =>
        instruction match {
          case Instruction('N', units) =>
            (location, waypoint.copy(y = waypoint.y + units))
          case Instruction('S', units) =>
            (location, waypoint.copy(y = waypoint.y - units))
          case Instruction('E', units) =>
            (location, waypoint.copy(x = waypoint.x + units))
          case Instruction('W', units) =>
            (location, waypoint.copy(x = waypoint.x - units))
          case Instruction('L', degrees) =>
            (location, rotate(waypoint, -degrees))
          case Instruction('R', degrees) =>
            (location, rotate(waypoint, degrees))
          case Instruction('F', times) =>
            println(s"old $location, before $times with $waypoint")
            val newS = ((location._1 + waypoint.x * times, location._2 + waypoint.y * times), waypoint)
            println(s"new: ${newS._1}")
            newS
        }
      }
    }.map { case (finalLocation, _) =>
      finalLocation._1.abs + finalLocation._2.abs
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1.unsafeRunSync()}")
    println(s"Part 2: ${part2.unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
