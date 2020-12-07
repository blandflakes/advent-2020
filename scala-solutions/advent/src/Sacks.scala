import cats.effect.{ExitCode, IO, IOApp}
import io._

object Sacks extends IOApp {
  final case class Bag(value: String) extends AnyVal
  final case class ContentsRule(value: Map[Bag, Int]) extends AnyVal

  private def parseRules: IO[Map[Bag, ContentsRule]] = withLines("../inputs/day7.txt") { stream =>
    IO.delay(stream.foldLeft(Map[Bag, ContentsRule]()) { case (acc, line) =>
      // Just do some ugly parsing, I guess
      val parts = line.split(" bags contain ")
      val subject = Bag(parts(0))
      val contents: ContentsRule = parts(1) match {
        case "no other bags." => ContentsRule(Map.empty)
        case bagList =>
          val items = bagList.stripSuffix(" bags.").stripSuffix("bag.").split("bag(s)?, ")
          ContentsRule(items.map { item =>
            val itemParts = item.split(" ")
            val count = Integer.parseInt(itemParts(0))
            (Bag(itemParts.slice(1, itemParts.length).mkString(" ")), count)
          }.toMap)
      }
      acc + (subject -> contents)
    })
  }

  private def searchForGoldHoldingBags(bag: Bag, rules: Map[Bag, ContentsRule]): Boolean = rules(bag).value match {
    case empty if empty.isEmpty => false
    case contents if contents.contains(Bag("shiny gold")) => true
    case contents =>
      contents.exists { case (bag, _) => searchForGoldHoldingBags(bag, rules) }
  }

  private def countNeededBags(rootBag: Bag, rules: Map[Bag, ContentsRule]): Int = rules(rootBag).value match {
    case empty if empty.isEmpty => 0
    case contents =>
      contents.map { case (bag, count) => count + count * countNeededBags(bag, rules)}.sum
  }

  def part1(): IO[Int]  = for {
    rules <- parseRules
    bagsContainingGold <- IO.delay(rules.keys.filter { bag => searchForGoldHoldingBags(bag, rules) })
  } yield bagsContainingGold.size

  def part2(): IO[Int] = for {
    rules <- parseRules
  } yield countNeededBags(Bag("shiny gold"), rules)

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Part 1: ${part1().unsafeRunSync()}")
    println(s"Part 2: ${part2().unsafeRunSync()}")
    IO(ExitCode.Success)
  }
}
