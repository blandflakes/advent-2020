import scala.jdk.StreamConverters._


object ConwayCubes {
  private val input= """#......#
                        |##.#..#.
                        |#.#.###.
                        |.##.....
                        |.##.#...
                        |##.#....
                        |#####.#.
                        |##.#.###""".stripMargin


  sealed abstract class State extends Product
  final case object Active extends State
  final case object Inactive extends State
  sealed trait CartesianPoint[T] {
    def neighbors: Set[T]
  }
  final case class Point3(x: Int, y: Int, z: Int) extends CartesianPoint[Point3] {
    override def neighbors: Set[Point3] = {
      for {
        xs <- (x - 1) to (x + 1)
        ys <- (y - 1) to (y + 1)
        zs <- (z - 1) to (z + 1)
        if !(xs == x && ys == y && zs == z)
      } yield Point3(xs, ys, zs)
    }.toSet
  }
  type Grid[T] = Map[CartesianPoint[T], State]

  def gridStates[T <: CartesianPoint[T]](startingGrid: Grid[T]): LazyList[Grid[T]] = {

    def iterate(grid: Grid[T]): LazyList[Grid[T]] = {
      // Ok so how do we actually iterate? we get a list of all neighbors, append it to guys in the grid, and for each
      // of them apply the state change, then flatten out inactive
      val toEvaluate = grid.keys ++ grid.keys.flatMap(_.neighbors)
      val updatedGrid: Grid[T] = toEvaluate.map { position =>
        val activeNeighbors = position.neighbors.count(grid.getOrElse(_, Inactive) == Active)
        (grid.getOrElse(position, Inactive), activeNeighbors) match {
          case (Active, 2 | 3) => (position, Active)
          case (Active, _) => (position, Inactive)
          case (Inactive, 3) => (position, Active)
          case (Inactive, _) => (position, Inactive)
        }
      }.toMap

      LazyList.cons(updatedGrid, iterate(updatedGrid))
    }

    LazyList.cons(startingGrid, iterate(startingGrid))
  }

  def part1: Int = {
    val startingGrid: Grid[Point3] = input.lines().toScala(LazyList).zipWithIndex.flatMap { case (line, y) =>
      line.zipWithIndex.map { case (char, x) =>
        char match {
          case '#' => (Point3(x, y, 0), Active)
          case '.' => (Point3(x,  y, 0), Inactive)
        }
      }
    }.toMap
    gridStates(startingGrid)(6).count(_._2 == Active)
  }

  final case class Point4(x: Int, y: Int, z: Int, w: Int) extends CartesianPoint[Point4] {
    override def neighbors: Set[Point4] = {
      for {
        xs <- (x - 1) to (x + 1)
        ys <- (y - 1) to (y + 1)
        zs <- (z - 1) to (z + 1)
        ws <- (w - 1) to (w + 1)
        if !(xs == x && ys == y && zs == z && ws == w)
      } yield Point4(xs, ys, zs, ws)
    }.toSet
  }

  def part2: Int = {
    val startingGrid: Grid[Point4] = input.lines().toScala(LazyList).zipWithIndex.flatMap { case (line, y) =>
      line.zipWithIndex.map { case (char, x) =>
        char match {
          case '#' => (Point4(x, y, 0, 0), Active)
          case '.' => (Point4(x,  y, 0, 0), Inactive)
        }
      }
    }.toMap
    gridStates(startingGrid)(6).count(_._2 == Active)
  }

  def main(args: Array[String]): Unit = {
    println(s"Part 1: $part1")
    println(s"Part 2: $part2")
  }
}
