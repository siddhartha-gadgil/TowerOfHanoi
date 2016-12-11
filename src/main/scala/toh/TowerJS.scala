package toh

 import org.scalajs.dom
//import org.scalajs.dom.html

import org.scalajs.dom._
// import scalajs.js.annotation._
import scalatags.JsDom.all._
//
// import scalatags.JsDom.svgTags._
// import scalatags.JsDom.{svgAttrs => svga}
//
import scala.scalajs.js.JSApp
//
// import scala.scalajs.js
//
// import dom.ext._
//
// import scalajs.concurrent.JSExecutionContext.Implicits.queue

//import scala.scalajs.js.Dynamic.{global => g}

object TowerJS extends JSApp{
  def main(): Unit = {
    var intervalId: Int = 0

    var towerState = TowerState.simple(10, Middle)

    var numRings = 10

    var running = false

    val jsDiv = document.getElementById("js-div")

    // import scalatags.JsDom.implicits._

    val towDiv = div.render

    val startStopBox = input(`type` := "button", value := "Start", `class` := "button2").render

    val resetBox = input(`type` := "button", value := "Reset", `class` := "button3").render


    def showTower(tow: TowerState) = {
      towDiv.innerHTML = ""
      towDiv.appendChild(SvgView.towerView(tow).render)
    }

    def showMoves(init: TowerState, moves: Vector[Move], delay: Double = 1000) = {
        towerState = init
        var anim = moves
        intervalId = dom.window.setInterval(() =>
          {
            if (anim.size > 0)
            {towerState = anim.head(towerState)
             anim = anim.tail}
             else {
               running = false
               startStopBox.value = "Start"
               dom.window.clearTimeout(intervalId)
             }
            showTower(towerState)
          },
          delay)
    }

    showTower(towerState)

    def goal = TowerState.simple(numRings, Left)




    val ringsBox = input (`type` := "text", value:= numRings).render

    ringsBox.onchange = (_ : dom.Event) => {
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
      showMoves(state, moves)
    }

    def stop() = {
      running = false
      startStopBox.value = "Start"
      dom.window.clearTimeout(intervalId)
    }

    def startStop = (_ : dom.Event) => if  (running) stop() else start()

    val d = div(towDiv, span("Number of rings:"), ringsBox, startStopBox, resetBox)

    startStopBox.onclick = startStop

    resetBox.onclick = (_ : dom.Event) => {
      if (running) stop() else ()
      towerState = TowerState.simple(numRings, Middle)
      showTower(towerState)
    }

    jsDiv.appendChild(d.render)
  }
}

object SvgView{
  import scalatags.JsDom.svgTags._
  import scalatags.JsDom.svgAttrs._

  val W = 1000
  val H = 350
  val B = 300
  val sc = 8

  def colour(n: Int) = Vector("red", "brown",  "magenta", "purple")(n % 4)

  val box =  svg(height:=H, width:= W)(
    rect(height:= H, width:= W, fill:="white"),
    line(x1:= 0, y1:= B, x2:= W, y2:= B, stroke:="black", strokeWidth:= 3)
  )

  def pile(centre: Int, rings: Vector[Int]) = {
     rings.zipWithIndex map {case (n, j) =>
       rect(x := centre  - (n * sc), y := B - (sc * 1.5 * (j + 1)), fill := colour(n), height := sc * 3 / 2, width := sc * n * 2)
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
      svg(height:=H, width:= W)(
        rect(height:= H, width:= W, fill:="black"),
        line(x1:= 0, y1:= B, x2:= W, y2:= B, stroke:="white", strokeWidth:= 3)
      )(towerPiles(tower) : _*
      )


}
