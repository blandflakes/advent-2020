import cats.effect.{ExitCode, IO, IOApp}
import io._

import java.util.regex.Pattern

object Passports extends IOApp {
  type Passport = Map[String, Array[String]]
  private def parsePassports: IO[List[Passport]] = withLines("../inputs/day4.txt") { stream =>
    final case class Accumulator(passports: List[Passport], accLines: List[String])
    IO {
      stream.foldLeft(Accumulator(List(), List())) { case (passports, nextLine) =>
        if (nextLine.isBlank) {
          val converted = passports.accLines
            // Make this into one big line
            .mkString(" ")
            // get the pairs
            .split(" ")
            // split them into their key, value
            .map(_.split(":"))
            // Lets just put them in a map for easy access
            .groupMap(_(0))(_(1))
          Accumulator(converted :: passports.passports, List())
        } else {
          passports.copy(accLines = nextLine :: passports.accLines)
        }
      }.passports
    }
  }

  private val yearPattern = Pattern.compile("^\\d{4}$")
  private def yearValidator(min: Int, max: Int): String => Boolean = {
    value => {
      if (yearPattern.matcher(value).matches()) {
        val parsed = Integer.parseInt(value)
        parsed >= min && parsed <= max
      }
      else false
    }
  }

  private val heightPattern = Pattern.compile("^\\d+(cm|in)$")
  private def heightValidator(value: String): Boolean = {
    val matcher = heightPattern.matcher(value)
    if (matcher.matches()) {
      matcher.group(1) match {
        case "cm" =>
          val parsed = Integer.parseInt(value.stripSuffix("cm"))
          parsed >= 150 && parsed <= 193
        case "in" =>
          val parsed = Integer.parseInt(value.stripSuffix("in"))
          parsed >= 59 && parsed <= 76
      }
    } else false
  }

  private val hairColorPattern = Pattern.compile("^#[0-9a-f]{6}$")
  private val eyeColors = Set("amb", "blu", "brn", "gry", "grn", "hzl", "oth")
  private val passportIdPattern = Pattern.compile("^\\d{9}$")

  // This could accept options to make the calling code cleaner, but regexes put a pretty low ceiling on how clean things
  // can be
  private val validators = Map[String, String => Boolean](
    "byr" -> yearValidator(1920, 2002),
    "iyr" -> yearValidator(2010, 2020),
    "eyr" -> yearValidator(2020, 2030),
    "hgt" -> heightValidator,
    "hcl" -> (value => hairColorPattern.matcher(value).matches()),
    "ecl" -> eyeColors,
    "pid" -> (value => passportIdPattern.matcher(value).matches())
  )

  def part1(): IO[Int] = {
    parsePassports.map { passports =>
      passports.count { passport => validators.keys.forall(neededField => passport.contains(neededField)) }
    }
  }

  def part2(): IO[Int] = {
    parsePassports.map { passports =>
      passports.count { passport => validators.keys.forall(neededField => passport.contains(neededField) &&
        validators(neededField)(passport(neededField)(0))) }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
