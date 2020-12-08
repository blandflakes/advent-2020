import cats.effect.{ExitCode, IO, IOApp}
import io._

import scala.collection.mutable

object Handheld extends IOApp {

  sealed abstract class Instruction extends Serializable with Product
  case class Nop(arg: Int) extends Instruction
  case class Acc(arg: Int) extends Instruction
  case class Jmp(arg: Int) extends Instruction

  def parseProgram(): IO[Array[Instruction]] = withLines("../inputs/day8.txt") { stream =>
    IO {
      stream.map { line =>
        line.split(" ").toList match {
          // Apparently parseInt knows what to do with a leading '+' sign.
          case "nop" :: arg :: Nil => Nop(Integer.parseInt(arg))
          case "acc" :: arg :: Nil => Acc(Integer.parseInt(arg))
          case "jmp" :: arg :: Nil => Jmp(Integer.parseInt(arg))
        }
      }.toArray
    }
  }

  sealed abstract class ExecutionResult
  case class InfiniteLoop(acc: Int) extends ExecutionResult
  case class Successful(acc: Int) extends ExecutionResult

  def evaluate(program: Array[Instruction]): ExecutionResult = {
    var acc = 0
    val visitedPositions: mutable.Set[Int] = mutable.Set()
    var instructionCounter = 0
    while (!visitedPositions.contains(instructionCounter) && instructionCounter < program.length) {
      visitedPositions += instructionCounter
      program(instructionCounter) match {
        case Nop(_) => instructionCounter += 1
        case Acc(arg) =>
          acc += arg
          instructionCounter += 1
        case Jmp(arg) =>
          instructionCounter += arg
      }
    }
    if (visitedPositions.contains(instructionCounter)) {
      InfiniteLoop(acc)
    }
    else {
      Successful(acc)
    }
  }

  def part1(): IO[Int] = parseProgram().map(evaluate).flatMap {
    case InfiniteLoop(acc) => IO(acc)
    case _ => IO.raiseError(new IllegalArgumentException("Expected an infinite loop"))
  }

  private def mutate(lineNumber: Int, program: Array[Instruction]): Option[Array[Instruction]] = {
    program(lineNumber) match {
      case Acc(_) => None
      case Nop(arg)=>
        val newProg = program.map(identity)
        // I did not know that Scala Arrays were mutable.
        newProg(lineNumber) = Jmp(arg)
        Some(newProg)
      case Jmp(arg) =>
        val newProg = program.map(identity)
        // I did not know that Scala Arrays were mutable.
        newProg(lineNumber) = Nop(arg)
        Some(newProg)
    }
  }

  private def findMutation(originalProgram: Array[Instruction]): Int = {
    // Building mutations is expensive and annoying. Lazy mapping to the rescue... Maybe I should revisit my feelings on
    // laziness as a default in Clojure, which were pretty negative
    val potentialMutations = originalProgram.indices.view.flatMap { lineNumber => mutate(lineNumber, originalProgram) }
    // Now should be able to freely map an evaluation onto this, keeping only successes
    val successfulMutations = potentialMutations.flatMap { program =>
      evaluate(program) match {
        case Successful(acc) => Some(acc)
        case InfiniteLoop(_) => None
      }
    }
    // Let's just assume we found one
    successfulMutations.head
  }

  def part2(): IO[Int] = parseProgram().map(findMutation)

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
