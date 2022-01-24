package toh

sealed class Peg {
  def ->(that: Peg) = Move(this, that)
}

object Peg {
  case object Left extends Peg
  case object Middle extends Peg
  case object Right extends Peg

  def otherPeg(first: Peg, second: Peg): Peg =
    Vector(Left, Middle, Right).filter((x) => x != first && x != second).head

  val all: Vector[Peg] = Vector(Left, Middle, Right)
  val pairs: Vector[(Peg, Peg)] = {
    for {
      a <- all
      b <- all
      if a != b
    } yield (a, b)
  }.toVector
}

object TowerSolver {
  def solve(start: TowerState, goal: TowerState): Vector[Move] = {
    import start.rings
    if (rings == 0) Vector() // For no rings, no moves needed
    else {
      val source = start.ringMap(rings) // largest peg for starting tower
      val sink = goal.ringMap(rings) // largest peg for goal tower
      if (source == sink)
        solve(
          start.init,
          goal.init
        ) // if largest ring is in correct place, ignore it and solve
      else {
        val third = Peg.otherPeg(
          source,
          sink
        ) // the peg not having largest ring in start or goal
        val onThird = TowerState.simple(
          rings - 1,
          third
        ) // set goal for all but largest ring
        (solve(
          start.init,
          onThird
        ) :+ // move all but largest ring to `third peg`
          Move(source, sink)) ++ // move largest peg
          solve(
            onThird,
            TowerState.simple(rings - 1, sink)
          ) // move all but largest peg from third peg
      }
    }

  }

  def standard(n: Int): Vector[Move] =
    solve(TowerState.simple(n, Peg.Middle), TowerState.simple(n, Peg.Left))

  def apply(start: Peg, goal: Peg, rings: Int) =
    solve(TowerState.simple(rings, start), TowerState.simple(rings, goal))

  def apply(rings: Int) =
    solve(
      TowerState.simple(rings, Peg.Middle),
      TowerState.simple(rings, Peg.Left)
    )
}

case class TowerState(rings: Int, ringMap: Map[Int, Peg]) {
  def onPeg(peg: Peg): Vector[Int] =
    (1 to rings).toVector.filter(ringMap(_) == peg)

  def init = TowerState(rings - 1, ringMap filter (_._1 != rings))

  def apply(moves: Vector[Move]) = moves.foldLeft(this)((st, mv) => mv(st))

  def seq(moves: Vector[Move]) = {
    (1 to moves.size).toVector map (moves.take(_)) map (apply)
  }

  def isLegal(source: Peg, sink: Peg): Boolean =
    source != sink &&
      onPeg(source).headOption
        .map(src =>
          onPeg(sink).headOption.map(sink => src < sink).getOrElse(true)
        )
        .getOrElse(false)
}
object TowerState {
  def simple(rings: Int, peg: Peg) =
    TowerState(rings, ((1 to rings) map (_ -> peg)).toMap)

  val rnd = new scala.util.Random()

  import Peg._

  def random(rings: Int) =
    TowerState(
      rings, {
        (1 to rings).toVector map ((j) =>
          (j, Vector(Left, Right, Middle)((rnd.nextInt(3))))
        )
      }.toMap
    )

}

case class Move(source: Peg, sink: Peg) {
  def apply(st: TowerState) = {
    val ring = st.onPeg(source).head
    val newMap = (st.ringMap filter (_._1 != ring)) + (ring -> sink)
    TowerState(st.rings, newMap)
  }

  override def toString = s"$source -> $sink"
}
