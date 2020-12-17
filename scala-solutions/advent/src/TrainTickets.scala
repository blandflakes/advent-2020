import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

object TrainTickets extends IOApp {

  final case class Restriction(fieldName: String, ranges: List[Range])
  type Ticket = List[Int]
  final case class ProblemInput(restrictions: Map[String, Restriction], myTicket: Ticket, nearbyTickets: List[Ticket])

  private def parseRestriction(line: String) = {
    val parts = line.split(": ")
    val ranges = parts(1).split(" or ").map { rangeString =>
      val minAndMax = rangeString.split("-")
      minAndMax(0).toInt to minAndMax(1).toInt
    }
    Restriction(parts(0), ranges.toList)
  }

  private def parseTicket(line: String): Ticket = line.split(",").map(_.toInt).toList

  private def parseInput: IO[ProblemInput] = withLines("../inputs/day16.txt") { stream =>
    IO {
      val (restrictionLines, ticketLines) = stream.span(_.nonEmpty)
      val restrictions = restrictionLines.map(parseRestriction).map(r => (r.fieldName, r)).toMap
      // drop empty line and "your ticket"
      val mine = parseTicket(ticketLines.drop(2).head)
      // drop empty line and "your ticket:", the processed line, the blank line, and "nearby tickets"
      val theRest = ticketLines.drop(5).map(parseTicket).toList
      ProblemInput(restrictions, mine, theRest)
    }
  }

  // Ok so I need to find all the values that aren't in any ranges
  def part1: IO[Int] = {
    parseInput.map { input =>
      val allValues = input.nearbyTickets.flatten
      val allRanges = input.restrictions.values.flatMap(_.ranges)
      allValues.filterNot { value =>
        allRanges.exists(_.contains(value))
      }.sum
    }
  }

  def part2: IO[Option[Long]] = {
    parseInput.map { input =>
      // ok, first, dump invalid
      val validTickets = input.nearbyTickets.filter { ticket =>
        ticket.forall(value => input.restrictions.values.flatMap(_.ranges).exists(_.contains(value)))
      }
      // What is the actual mapping we do? position -> sumOfCandidateIndexes
      val fieldCandidates: Array[Set[String]] = Array.fill(input.restrictions.size)(input.restrictions.keySet)
      validTickets.foreach { ticket =>
        ticket.zipWithIndex.foreach { case (value, potentialPosition) =>
          input.restrictions.foreach { case (name, restriction) =>
            if (!restriction.ranges.exists(_.contains(value))) {
              fieldCandidates(potentialPosition) -= name
            }
          }
        }
      }

      // This is faster than the backtracking below. Ug, must have had a bug first time I tried this
      val fastSolution = fieldCandidates.zipWithIndex.sortBy(_._1.size).foldLeft(Map[String, Int]()) { case (selected, candidateList) =>
        // assume there is just one
        val nextField = (candidateList._1 diff selected.keySet).head
        selected.updated(nextField, candidateList._2)
      }
      val _ = fastSolution.filter(_._1.startsWith("departure")).map { case (_, position) =>
        input.myTicket(position).toLong
      }
      // What are my base cases and return types? I'm building up a solution, and need to pop stuff back off the solution
      // I need to build a list of stuff, in order of position. So I need to keep the array indexable by position.
      // Oh who cares. just do it in order. So we're going to pick a value for this from its candidates, recurse
      // with tail.map(removeTheThingIPicked). we return None if no candidates, Some(candidate :: returnedBoy) if there was one
      // headOption doing a lot of work here
      def solve(orderedCandidates: List[Set[String]]): Option[List[String]] =
        orderedCandidates match {
          case last :: Nil => last.headOption.map(List(_))
          case next :: more =>
            // Ok, so here's where we want to try to "backtrack". for each candidate, we'll recurse as if we chose it
            // if we find a solution, yay, we know that this thread succeeded, and want to pass the candidate +: the tail
            // if we don't, we want to signal that "nah, this didn't work out, try again"
            next.flatMap { candidate =>
              solve(more.map(_ - candidate)).map(candidate +: _)
            }.headOption
        }

      solve(fieldCandidates.toList).map(_.zipWithIndex.toMap).map { positionsByField =>
        positionsByField.filter(_._1.startsWith("departure")).map { case (_, position) =>
          input.myTicket(position).toLong
        }.product
      }
    }
  }
  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1.unsafeRunSync()}")
    println(s"Part 1: ${part2.unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
