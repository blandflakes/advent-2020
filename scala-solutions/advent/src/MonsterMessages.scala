import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

object MonsterMessages extends IOApp {

  // Ok but can I use these in the rules themselves? like Match needs to return what is matched?
  sealed abstract class MatchOutcome {
    def ++(other: MatchOutcome): MatchOutcome
  }
  object NoMatch extends MatchOutcome {
    override def ++(other: MatchOutcome): MatchOutcome = other match {
      case NoMatch => NoMatch
      case matched: Match => matched
    }
  }

  final case class MatchEntry(remaining: List[Char])
  final case class Match(matches: List[MatchEntry]) extends MatchOutcome {
    override def ++(other: MatchOutcome): MatchOutcome = other match {
      case NoMatch => this
      case matched: Match => Match(this.matches ++ matched.matches)
    }
  }

  object Match {
    def single(remaining: List[Char]): Match = Match(List(MatchEntry(remaining)))
  }

  sealed abstract class Rule extends Product {
    // Ok how do we match some of the more complicated rules? like if I do an OR, and I match the left,
    // each rule will return potentially multiple matches (only Or does this, actually...)
    // Current contract: a match consumes the matching characters and returns the tail
    def matches(input: List[Char])(implicit rulesBank: Map[Int, Rule]): MatchOutcome
  }

  final case class CharacterIs(value: Char) extends Rule {
    override def matches(input: List[Char])(implicit rulesBank: Map[Int, Rule]): MatchOutcome = input.head match {
      case char if char == value => Match.single(input.tail)
      case _ => NoMatch
    }
  }

  final case class Reference(ruleIndex: Int) extends Rule {
    override def matches(input: List[Char])(implicit rulesBank: Map[Int, Rule]): MatchOutcome = rulesBank(ruleIndex).matches(input)
  }

  final case class Or(left: Rule, right: Rule) extends Rule {
    override def matches(input: List[Char])(implicit rulesBank: Map[Int, Rule]): MatchOutcome = {
      left.matches(input) ++ right.matches(input)
    }
  }

  final case class InSequence(rules: List[Rule]) extends Rule{
    override def matches(input: List[Char])(implicit rulesBank: Map[Int, Rule]): MatchOutcome = {
      def solve(remainingInput: List[Char], remainingRules: List[Rule]): MatchOutcome = {
        (remainingInput, remainingRules) match {
          // Perfect match
          case (Nil, Nil) => Match.single(List.empty)
          // Out of chars to satisfy rules
          case (Nil, _) => NoMatch
          // Chars not yet consumed by the rules, but we match so far
          case (_, Nil) => Match.single(remainingInput)
          // Ok, time to evaluate rules and recurse
          case (moreChars, nextRule :: moreRules) =>
            nextRule.matches(moreChars) match {
              case NoMatch => NoMatch
              case Match(matches) =>
                matches.map { case MatchEntry(remaining) =>
                  solve(remaining, moreRules)
                }.reduce(_ ++ _)
            }
        }
      }

      solve(input, rules)
    }
  }

  object RuleParser {
    import atto._
    import Atto._
    val index: Parser[Int] = (int <~ char(':')).token
    val charPattern: Parser[CharacterIs] = for {
      _ <- char('"')
      value <- charRange('a' to 'z')
      _ <- char('"').token
    } yield CharacterIs(value)
    val reference: Parser[Reference] = int.map(Reference).token
    // So how do we define OR? OR is mutually recursive with InSequence. We want to group OR first?
    val inSequence: Parser[InSequence] = many(reference).map(InSequence)
    lazy val or: Parser[Or] = for {
      left <- inSequence
      _ <- char('|').token
      right <- or | inSequence
    } yield Or(left, right)

    val parseRule: Parser[(Int, Rule)] = index ~ (charPattern | or | inSequence)

    def parseRule(line: String): ParseResult[(Int, Rule)] = parseRule.parseOnly(line)
  }

  def parseInput: IO[(Map[Int, Rule], List[String])] = withLines("../inputs/day19.txt") { stream =>
    IO {
      val (rulesText, tail) = stream.span(_.nonEmpty)
      val rules = rulesText.map(RuleParser.parseRule(_)).map(_.option.get).toMap
      val messages = tail.drop(1)
      // Let's just realize this into memory
      (rules, messages.toList)
    }
  }

  def part1: IO[Int] = parseInput.map { case (rules, messages) =>
    val target = rules(0)
    messages.count { message =>
      target.matches(message.toList)(rules) match {
        case NoMatch => false
        case Match(matches) =>
          // have to find a match that completely consumes
          matches.exists(_.remaining.isEmpty)
      }
    }
  }

  def part2: IO[Int] = parseInput.map { case (rules, messages) =>
    val updatedRules = rules ++ Map(
      8 -> Or(Reference(42), InSequence(List(Reference(42), Reference(8)))),
      11 -> Or(InSequence(List(Reference(42), Reference(31))), InSequence(List(Reference(42), Reference(11), Reference(31))))
    )
    val target = updatedRules(0)
    messages.count { message =>
      target.matches(message.toList)(updatedRules) match {
        case NoMatch => false
        case Match(matches) =>
          // have to find a match that completely consumes
          matches.exists(_.remaining.isEmpty)
      }
    }
  }
  override def run(args: List[String]): IO[ExitCode] = {
    part1.map { validMessageCount =>
      println(s"Part 1: $validMessageCount")
    } *> part2.map { validMessageCount =>
      println(s"Part 2: $validMessageCount")
    } *> IO(ExitCode.Success)
  }
}
