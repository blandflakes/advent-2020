import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

import scala.annotation.tailrec

object Shuttles extends IOApp {
  case class Schedule(departure: Int, busses: List[Int])

  private def parseInput: IO[Schedule] = withLines("../inputs/day13.txt") { stream =>
    IO {
      // Whatever this format isn't really a stream
      val earliestDeparture = Integer.parseInt(stream.head)
      val busses = stream.tail.head.split(",").filterNot(_ == "x").map { _.toInt }
      Schedule(earliestDeparture, busses.toList)
    }
  }

  private def inputAsRequirements(busSchedule: List[String]): LazyList[Schedule] => Boolean = {
      val requirements = busSchedule.map {
        case "x" => (_: List[Int]) => true
        case int => (busses: List[Int]) => busses.contains(int.toInt)
      }
      (window: LazyList[Schedule]) =>
        window.zipWithIndex.forall { case (schedule, position) =>
          requirements(position)(schedule.busses) }
  }

  private def busStream(busses: List[Int]): LazyList[Schedule] = {
    LazyList.from(0).map { timestamp =>
      Schedule(timestamp, busses.filter(busId => timestamp % busId == 0))
    }
  }

  def part1: IO[Option[Int]] = for {
    initialSchedule <- parseInput
    bussesPerTimestamp = busStream(initialSchedule.busses)
    advanced = bussesPerTimestamp.drop(initialSchedule.departure)
    nextSlot = advanced.find(_.busses.nonEmpty)
  } yield nextSlot.map { schedule => (schedule.departure - initialSchedule.departure) * schedule.busses.head}

  def part2: IO[Option[Int]] = for {
    busSchedule <- withLines("../inputs/day13.txt") { stream => IO(stream.tail.head.split(",").toList) }
    checker = inputAsRequirements(busSchedule)
    busses = busSchedule.filterNot(_ == "x").map { _.toInt }
    bussesPerTimestamp = busStream(busses)
    _ = println(busSchedule.indexOf("761"))
    windows = bussesPerTimestamp.sliding(busSchedule.length, 761 + 50)
    matching = windows.find(checker)
    timestamp = matching.map(_.head.departure)
  } yield timestamp

  def part2Faster: IO[Long] = for {
    busSchedule <- withLines("../inputs/day13.txt") { stream => IO(stream.tail.head.split(",").toList) }
    busses = busSchedule.zipWithIndex.filterNot(_._1 == "x").map { case (value, pos) => (value.toLong, pos) }
    someThing = busses.foldLeft((1L, 1L)) { case ((timestamp, stepSize), (id, pos)) =>
      // Can we basically say "find me the next spot that I'm on, and then we'll increase the step size"
      @tailrec
      def loop(timestamp: Long, id: Long, pos: Long): Long = {
        // See if this timestamp satisfies this bus - we already satisfied all "prior" busses
        if ((timestamp + pos) % id == 0) {
          timestamp
        } else {
          loop(timestamp + stepSize, id, pos)
        }
      }

      val nextMatch = loop(timestamp, id, pos)
      (nextMatch, stepSize * id)
    }
  } yield someThing._1

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1.unsafeRunSync()}")
    println(s"Part 2: ${part2Faster.unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
