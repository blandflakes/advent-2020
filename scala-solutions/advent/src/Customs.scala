import cats.effect.{ExitCode, IO, IOApp}
import io._

object Customs extends IOApp {

  final case class Question(value: Char) extends AnyVal
  type SurveyResponse = Set[Question]
  type QuestionBatch = List[SurveyResponse]
  private def parseBatches: IO[List[QuestionBatch]] = withLines("../inputs/day6.txt") { stream =>
    final case class Accumulator(batches: List[QuestionBatch], lines: List[String]) {
      def consumeBatch: Accumulator = {
        val batch = lines.map(_.map(Question).toSet)
        Accumulator(batches.appended(batch), List())
      }
    }
    IO {
      stream.foldLeft(Accumulator(List(), List())) { case (acc, nextLine) =>
        if (nextLine.isBlank) {
          acc.consumeBatch
        } else {
          acc.copy(lines = nextLine :: acc.lines)
        }
      }.consumeBatch.batches
    }
  }

  def part1(): IO[Int] = for {
    batches <- parseBatches
    sum = batches.map(_.flatten.toSet.size).sum
  } yield sum

  def part2(): IO[Int] = for {
    batches <- parseBatches
    counts <- IO.delay(batches.map(batch => batch.reduce(_ intersect _).size))
  } yield counts.sum

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
