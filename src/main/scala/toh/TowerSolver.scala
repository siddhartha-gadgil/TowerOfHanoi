package toh

sealed class Peg {
  def ->(that: Peg) = Move(this, that)
}

case object Left extends Peg
case object Middle extends Peg
case object Right extends Peg

object Peg {
  def otherPeg(first: Peg, second: Peg): Peg =
    Vector(Left, Right, Middle).filter((x) => x != first && x != second).head
}

object TowerSolver {
  def solve(start: TowerState, goal: TowerState): Vector[Move] = {
    import start.rings
    if (rings == 0) Vector()
    else {
      val source = start.ringMap(rings)
      val sink = goal.ringMap(rings)
      if (source == sink) solve(start.init, goal.init)
      else {
        val third = Peg.otherPeg(source, sink)
        val onThird = TowerState.simple(rings - 1, third)
        (solve(start.init, onThird) :+ Move(source, sink)) ++ solve(
            onThird, TowerState.simple(rings - 1, sink))
      }
    }
  }

  def apply(start: Peg, goal: Peg, rings: Int) =
    solve(TowerState.simple(rings, start), TowerState.simple(rings, goal))

  def apply(rings: Int) =
    solve(TowerState.simple(rings, Middle), TowerState.simple(rings, Left))
}

case class TowerState(rings: Int, ringMap: Map[Int, Peg]) {
  def onPeg(peg: Peg) = (1 to rings).toVector.filter(ringMap(_) == peg)

  def init = TowerState(rings - 1, ringMap filter (_._1 != rings))

  def apply(moves: Vector[Move]) = moves.foldLeft(this)((st, mv) => mv(st))

  def seq(moves: Vector[Move]) = {
    (1 to moves.size).toVector map (moves.take(_)) map (apply)
  }
}

object TowerState {
  def simple(rings: Int, peg: Peg) =
    TowerState(rings, ((1 to rings) map (_ -> peg)).toMap)
}

case class Move(source: Peg, sink: Peg) {
  def apply(st: TowerState) = {
    val ring = st.onPeg(source).head
    val newMap = (st.ringMap filter (_._1 != ring)) + (ring -> sink)
    TowerState(st.rings, newMap)
  }

  override def toString = s"$source -> $sink"
}
