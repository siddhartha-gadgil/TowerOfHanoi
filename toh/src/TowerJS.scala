package toh

import org.scalajs.dom
import org.scalajs.dom._
import scalatags.JsDom.all._
import scala.scalajs.js.annotation._

import Peg._

@JSExportTopLevel("Toh")
object TowerJS  {
  @JSExport
  def main(): Unit = {
    var intervalId: Int = 0

    var speed = 1.0

    var towerState = TowerState.simple(10, Middle)

    var numRings = 10

    var running = false

    var counter = 0

    val jsDiv = document.getElementById("js-div")

    val towDiv = div.render

    val startStopBox = input(
        `type` := "button", value := "Start", `class` := "input-btn btn btn-success").render

    val resetBox = input(
        `type` := "button", value := "Reset", `class` := "input-btn btn btn-warning").render

    val randomBox = input(
        `type` := "button", value := "Random Tower", `class` := "input-btn btn btn-primary").render

    val speedBar = input(
      `type` := "range", value := "10", min := "0", max := "30", step:= "1", id :="speed"
    ).render

    def counterSpanHTML = span(`class` := "h4")(span("Moves: "), span(`class` := "bg-info")(s"$counter")).render

    val counterDiv = div(`class` := "col-md-2")(counterSpanHTML).render



    def showTower(tow: TowerState) = {
      towDiv.innerHTML = ""
      towDiv.appendChild(SvgView.towerView(tow).render)
      counterDiv.innerHTML = ""
      counterDiv.appendChild(counterSpanHTML)
    }

    def showMoves(
        init: TowerState, moves: Vector[Move], delay: Double = 10000/(math.pow(10, speed))) = {
      towerState = init
      var anim = moves
      intervalId = dom.window.setInterval(
          () =>
            {
              if (anim.size > 0) {
                towerState = anim.head(towerState)
                anim = anim.tail
                counter += 1
              } else {
                running = false
                startStopBox.value = "Start"
                startStopBox.classList.remove("btn-danger")
                startStopBox.classList.add("btn-success")
                dom.window.clearTimeout(intervalId)
              }
              showTower(towerState)
          },
          delay)
    }

    showTower(towerState)

    def goal = TowerState.simple(numRings, Left)

    val ringsBox = input(`type` := "text", value := numRings, size := "2").render

    ringsBox.onchange = (_: dom.Event) =>
      {
        stop()
        numRings = ringsBox.value.toInt
        towerState = TowerState.simple(numRings, Middle)
        showTower(towerState)
    }

    def start() = {
      val state = towerState
      val moves = TowerSolver.solve(state, goal)
      // dom.window.clearInterval(intervalId)
      running = true
      startStopBox.value = "Stop"
      startStopBox.classList.remove("btn-success")
      startStopBox.classList.add("btn-danger")
      showMoves(state, moves)
    }

    def stop() = {
      running = false
      startStopBox.value = "Start"
      startStopBox.classList.remove("btn-danger")
      startStopBox.classList.add("btn-success")
      dom.window.clearTimeout(intervalId)
    }

    def startStop = (_: dom.Event) => if (running) stop() else start()


    val d = div(
        towDiv,
        div(`class` := "row")(
          div(`class`:= "col-md-5")(
          label("Rings :"), ringsBox, span(" "),
          startStopBox, span(" "), resetBox, span(" "), randomBox),
          counterDiv,
          div(`class` := "col-md-2")(label(`for` := "speed")("Speed:"), speedBar))
    )

    startStopBox.onclick = startStop

    speedBar.onchange = (_ : dom.Event) => {
      stop()
      speed = speedBar.value.toDouble / 10
    }

    resetBox.onclick = (_: dom.Event) =>
      {
        if (running) stop() else ()
        towerState = TowerState.simple(numRings, Middle)
        counter = 0
        showTower(towerState)
    }

    randomBox.onclick = (_: dom.Event) =>
      {
        if (running) stop() else ()
        towerState = TowerState.random(numRings)
        counter = 0
        showTower(towerState)
    }

    jsDiv.appendChild(d.render)
  }
}

object SvgView {
  import scalatags.JsDom.svgTags._
  import scalatags.JsDom.svgAttrs._

  val W = 1000
  val H = 350
  val B = 300
  val sc = 8

  def colour(n: Int) =
    Vector("red", "magenta",  "orangered", "tomato", "darkorange")(n % 5)

  def pile(centre: Int, rings: Vector[Int]) = {
    rings.zipWithIndex map {
      case (n, j) =>
        rect(x := centre - (n * sc),
             y := B - (sc * 1.5 * (j + 1)),
             fill := colour(n),
             height := sc * 3 / 2,
             width := sc * n * 2,
            strokeWidth := 1,
            stroke := "black")
    }
  }

  def towerPiles(tower: TowerState) =
    Vector(Left, Middle, Right).zipWithIndex.map {
      case (peg, i) =>
        val c = (i + 1) * W / 4
        val rings = tower.onPeg(peg).reverse
        pile(c, rings)
    }.flatten

  def towerView(tower: TowerState) =
    svg(height := H, width := W)(
        rect(height := H, width := W, fill := "skyblue"),
        rect(x := 0,
             y := B,
             width := W,
             height := H - B,
             fill := "green",
             strokeWidth := 3)
    )(towerPiles(tower): _*)
}
