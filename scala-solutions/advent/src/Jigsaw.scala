import cats.effect.IO
import io.withLines

import scala.collection.immutable.ArraySeq

object Jigsaw {

  type Grid = ArraySeq[ArraySeq[Char]]
  type CoordTransform = Point => Point
  case class Point(x: Int, y: Int)
  case class GridView(underlying: Grid, transform: CoordTransform = identity) {
    def get(point: Point): Char = {
      val transformed = transform(point)
      underlying(transformed.x)(transformed.y)
    }
    // What are my operations? A grid can rotate, and a grid can "flip".
    // Flipping X: length - oldX
    // Flipping Y: length - oldY
    // How do those maps get applied? Thinking some weird clever thing about having mappings of positions that translate
    // into the same initial grid... not bad, doesn't really solve the issue (especially because we need to do row and column order matching).
    // What questions do I need to ask?
    //  "give me a column" (for each transform)
    //  "give mea  row" (for each transform)
    // so transforms could basically be a view on the Tile.
  }
  object GridView {
    def transformations(grid: Grid): List[GridView] = {
      def flipX: CoordTransform = point => Point(grid.size - point.x - 1, point.y)
      def flipY: CoordTransform = point => Point(point.x, grid(0).size - point.y - 1)
      def rotate(degrees: Int): CoordTransform = { point =>
        degrees match {
          case 90 => Point(point.y, -point.x)
          case 180 => Point(-point.x, -point.y)
          case 270 => Point(-point.y, point.x)
        }
      }

      // So, what are combos? Can we both flipX and flipY?
      List(
        GridView(grid),
        GridView(grid, transform = { point =>
          Point(point.x - grid.size, point.y)
        })
      )
    }
  }
  case class Tile(id: Int, grid: Grid) {
    def representations: List[Grid] = {

    }
  }

  private def parseInput: IO[Iterator[Tile]] = withLines("../inputs/day20.txt") { stream =>
    IO(stream.filterNot(_.isEmpty).grouped(11).map { lines =>
      val id = lines.head.stripPrefix("Tile ").stripSuffix(":").toInt
      val grid = ArraySeq.from(lines.tail.map(line =>
        ArraySeq.from(line.toCharArray)
      ))
      Tile(id, grid)
    })
  }
}
