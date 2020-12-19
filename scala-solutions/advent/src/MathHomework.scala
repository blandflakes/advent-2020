import atto._
import Atto._
import cats.effect.{ExitCode, IO, IOApp}
import io.withLines

object MathHomework extends IOApp {

  sealed abstract class Operator {
    def parser: Parser[Operator]

  }
  final case object Add extends Operator {
    def parser: Parser[Operator] = char('+').token.map(_ => Add)

  }
  final case object Multiply extends Operator {
    def parser: Parser[Operator] = char('*').token.map(_ => Multiply)

  }

  sealed abstract class Expression {
    def evaluate: Long
  }
  final case class LongArg(value: Long) extends Expression {
    override def evaluate: Long = value
  }
  final case class Operation(left: Expression, operator: Operator, right: Expression) extends Expression {
    override def evaluate: Long = operator match {
      case Add => left.evaluate + right.evaluate
      case Multiply => left.evaluate * right.evaluate
    }
  }

  object assocParser {
    val parseOperator: Parser[Operator] = {
      Add.parser | Multiply.parser
    }

    lazy val parseTerm: Parser[Expression] = delay {
      val expression = for {
        _ <- char('(')
        e <- parseExpression
        _ <- char(')')
      } yield e
      expression | long.map(LongArg).token
    }

    lazy val parseExpressionRecur: Parser[Expression => Operation] = delay {
      val recursive = for {
        operator <- parseOperator
        rh <- parseTerm
        more <- parseExpressionRecur
      } yield (lh: Expression) => more(Operation(lh, operator, rh))
      val terminal = for {
        operator <- parseOperator
        rh <- parseTerm
      } yield (lh: Expression) => Operation(lh, operator, rh)
      recursive | terminal
    }

    lazy val parseExpression: Parser[Operation] = for {
      lh <- parseTerm
      more <- parseExpressionRecur
    } yield more(lh)
  }

  def part1: IO[Long] = withLines("../inputs/day18.txt") { stream =>
    IO(stream.flatMap(assocParser.parseExpression.parseOnly(_).option.map(_.evaluate)).sum)
  }

  object precedentParser {
    lazy val literal: Parser[LongArg] = long.token.map(LongArg)
    lazy val term: Parser[Expression] = {
      val expression =  for {
        _ <- char('(')
        e <- parseExpression
        _ <- char(')')
      } yield e
      expression.token | literal
    }
    lazy val addExprRecur: Parser[Expression => Expression] = {
      val recursive = for {
        operator <- Add.parser
        rh <- term
        more <- addExprRecur
      } yield (lh: Expression) => more(Operation(lh, operator, rh))
      val terminal = for {
        operator <- Add.parser
        rh <- term
      } yield (lh: Expression) => Operation(lh, operator, rh)

      recursive | terminal | ok((lh: Expression) => lh)
    }
    lazy val addExpr: Parser[Expression] =
      for {
        lh <- term
        more <- addExprRecur
      } yield more(lh)
    lazy val multExprRecur: Parser[Expression => Expression] = {
      val recursive = for {
        operator <- Multiply.parser
        rh <- addExpr
        more <- multExprRecur
      } yield (lh: Expression) => more(Operation(lh, operator, rh))
      val terminal = for {
        operator <- Multiply.parser
        rh <- addExpr
      } yield (lh: Expression) => Operation(lh, operator, rh)
      recursive | terminal | ok((lh: Expression) => lh)
    }
    lazy val parseExpression: Parser[Expression] =
      for {
        lh <- addExpr
        more <- multExprRecur
      } yield more(lh)
  }

  def part2: IO[Long] = withLines("../inputs/day18.txt") { stream =>
    IO(stream.flatMap(precedentParser.parseExpression.parseOnly(_).option.map(_.evaluate)).sum)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    IO(println(s"Part 1: ${part1.unsafeRunSync()}")) *>
    IO(println(s"Part 2: ${part2.unsafeRunSync()}")) *>
    IO(ExitCode.Success)
  }
}
