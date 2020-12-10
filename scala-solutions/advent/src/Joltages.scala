import cats.effect.{ExitCode, IO, IOApp}
import io.withLines


object Joltages extends IOApp {
  def part1(): IO[Long] = withLines("../inputs/day10.txt") { stream =>
    IO {
      stream.map(_.toLong).sorted.prepended(0L).sliding(2).foldLeft(Map[Long, Long](3L -> 1L).withDefaultValue(0L)) { case (counts, gap) =>
        val difference = gap(1) - gap.head
        counts + (difference -> (counts(difference) + 1L))
      }
    }.map { counts => counts(1) * counts(3)}
  }

  def generateAdapterCombinations(joltages: List[Long]): List[List[Long]] = {
    joltages match {
      case head :: Nil => List(List(head, head + 3L))
      case head :: tail =>
        val nextSteps = tail.takeWhile(_ - 3 <= head)
        nextSteps.zipWithIndex.flatMap { case (next, index) =>
          tail.drop(index + 1) match {
            case Nil => List(head :: next :: next + 3L :: Nil)
            case more =>
              val newLists = generateAdapterCombinations(more).map(tail => head :: next :: tail)
              newLists
          }
        }
    }
  }

  def countAdapterCombinations(joltages: List[Long]): Long = {
    var connections: Map[Long, Long] = Map(0L -> 1L).withDefaultValue(0)
    joltages.zipWithIndex.dropRight(1).foreach { case (adapter, index) =>
      val connectionsFrom = joltages.drop(index + 1).takeWhile(_ - 3 <= adapter)
      connectionsFrom.foreach { connectedAdapter =>
        val priorCount = connections(connectedAdapter)
        val nextCount = priorCount + connections(adapter)
        connections += (connectedAdapter -> nextCount)
      }
    }
    connections(joltages.last)
  }

  def part2(): IO[Long] = withLines("../inputs/day10.txt") { stream =>
    IO {
      val withoutEnd = stream.map(_.toLong).sorted.prepended(0L)
      withoutEnd.appended(withoutEnd.last + 3).toList
    }.map(countAdapterCombinations)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")

    IO(ExitCode.Success)
  }
}
