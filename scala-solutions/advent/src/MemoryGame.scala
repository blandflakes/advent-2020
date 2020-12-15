object MemoryGame {

  type History = List[Int]

  def playGame(initialState: Array[Int], target: Int): Int = {
    // Track both lastNumber and turnNumber instead of having to use a bidimap to look up
    // need turnNumber to calculate distance from last time a number was spoken. have lastNumber to avoid bidimap/looking up the values with turnNumber as a key
    def iterate(memory: Map[Int, History], lastNumber: Int, turnNumber: Int): LazyList[Int] = {
      memory(lastNumber) match {
        case _ :: Nil =>
          val updatedMemory = memory.updated(0, turnNumber :: memory(0))
          LazyList.cons(0, iterate(updatedMemory, 0, turnNumber + 1))
        case once :: prior :: _ =>
          val answer = once - prior
          val updatedMemory = memory.updated(answer, turnNumber :: memory.getOrElse(answer, List()))
          LazyList.cons(answer, iterate(updatedMemory, answer, turnNumber + 1))
      }
    }
    iterate(initialState.zipWithIndex.map { case (num, position) => (num, List(position + 1)) }.toMap, initialState.last, initialState.length + 1)(target - initialState.length - 1)
  }

  def part1: Int = {
    playGame("0,14,6,20,1,4".split(",").map(_.toInt), 2020)
  }

  def part2: Int = {
    playGame("0,14,6,20,1,4".split(",").map(_.toInt), 30000000)
  }

  def main(args: Array[String]): Unit = {
    println(s"Part 1: $part1")
    println(s"Part 1: $part2")
  }
}
