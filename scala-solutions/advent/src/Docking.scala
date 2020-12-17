import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

import scala.util.matching.Regex


object Docking extends IOApp {

  final case class Question(value: Char) extends AnyVal
  type SurveyResponse = Set[Question]
  type QuestionBatch = List[SurveyResponse]


  final case class Mask(value: String) extends AnyVal
  final case class MemoryAssignment(destination: Long, value: Long)
  final case class InstructionBatch(mask: Mask, instructions: List[MemoryAssignment])
  // Intellij hates the raw macro, apparently
  val memIndexPattern: Regex = raw"^mem\[(\d+)\]".r
  val memValuePattern: Regex = "(\\d+)$".r
  private def withBatches[T](block: => LazyList[InstructionBatch] => T): IO[T] = withLines("../inputs/day14.txt") { stream =>

    def batchStream(lines: LazyList[String]): LazyList[InstructionBatch] = {
      if (lines.isEmpty)
        LazyList.empty
      else {
        val mask = Mask(lines.head.stripPrefix("mask = "))
        val (assignmentLines, tail) = lines.tail.span(_.startsWith("mem["))
        val assignments = assignmentLines.map { line =>
          val destination = memIndexPattern.findAllIn(line).group(1).toLong
          val value = memValuePattern.findAllIn(line).group(1).toLong
          MemoryAssignment(destination, value)
        }.toList
        InstructionBatch(mask, assignments) #:: batchStream(tail)
        }
      }

    IO(block(batchStream(stream)))
  }

  def part1(): IO[Long] = withBatches { batches =>
    batches.foldLeft(Map[Long, Long]()) { case (acc, batch) =>
      // We can apply two masks to get our values. The OR mask is responsible for 1 replacements - if we put a 0
      // for all "x" characters, then we'll only update where 1s existed. Similarly, the AND mask will replace all
      // "x" characters with a 1, which overrides only the 0 spots.
      val orMask = java.lang.Long.parseLong(batch.mask.value.replaceAll("X", "0"), 2)
      val andMask = java.lang.Long.parseLong(batch.mask.value.replaceAll("X", "1"), 2)
      batch.instructions.foldLeft(acc) { case (memory, assignment) =>
        memory.updated(assignment.destination, (assignment.value & andMask) | orMask)
      }
    }.values.sum
  }

  def part2(): IO[Long] = withBatches { batches =>
    batches.foldLeft(Map[Long, Long]()) { case (acc, batch) =>

      batch.instructions.flatMap { assignment =>
        // This unholy chain does the work of creating a starting place for the spraying of destinations. We can't
        // use bitwise logic as far as I can tell, so we do our own masking - leave 0s alone, but copy in 1 or X (X
        // effectively overrides since it provides both)
        val oneMasked: String = assignment.destination.toBinaryString.reverse.padTo(batch.mask.value.length, '0').reverse.zip(batch.mask.value).map { case (premask, mask) =>
          mask match {
            case '0' => premask
            case '1' => '1'
            case 'X' => 'X'
          }
        }.mkString("")

        // Ok, so we applied the overwriting of 1, and copied the Xs in so they can be overridden and sprayed out.
        // Let's do the actual spraying.
        var destinations = List(oneMasked)
        for (_ <- 1 to batch.mask.value.count(_ == 'X')) {
          destinations = destinations.flatMap { destination =>
            List(destination.replaceFirst("X", "0"), destination.replaceFirst("X", "1"))
          }
        }
        destinations.map(destStr => MemoryAssignment(java.lang.Long.parseLong(destStr, 2), assignment.value))
      }.foldLeft(acc) { case (memory, assignment) =>
        memory.updated(assignment.destination, assignment.value)
      }
    }.values.sum
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}

