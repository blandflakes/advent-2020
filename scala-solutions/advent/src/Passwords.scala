import cats.effect.{ExitCode, IO, IOApp}
import io._
object Passwords extends IOApp {

  private def parseBounds(element: String): (Int, Int) = {
    val parts = element.split("-").map { num => Integer.parseInt(num)}
    (parts(0), parts(1))
  }

  // lol validation
  private def parseRequiredChar(element: String) = element.charAt(0)

  def passwordValidator(validate: => (String, (Int, Int), Char) => Boolean): IO[Int] = {
    withLines("../inputs/day2.txt") { stream =>
      IO {
        val validPasswords = for {
          line <- stream
          parts = line.split(" ")
          bounds = parseBounds(parts(0))
          requiredChar = parseRequiredChar(parts(1))
          pass = parts(2)
          if validate(pass, bounds, requiredChar)
        } yield pass
        validPasswords.size
      }
    }
  }

  def part1(): IO[Int] = passwordValidator { case (password: String, bounds: (Int, Int), requiredChar: Char) =>
    val charCount = password.count(_ == requiredChar)
    charCount >= bounds._1 && charCount <= bounds._2
  }

  def part2(): IO[Int] = passwordValidator { case (password: String, bounds: (Int, Int), requiredChar: Char) =>
    val c1 = password(bounds._1 - 1)
    val c2 = password(bounds._2 - 1)
    c1 != c2 && (c1 == requiredChar || c2 == requiredChar)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
