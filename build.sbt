enablePlugins(ScalaJSPlugin)

lazy val scalaV = "2.12.4"

lazy val root = (project in file(".")).
  settings(
    organization := "in.ac.iisc",
    name := "Tower-of-Hanoi",
    version := "1.0",
    scalaVersion := scalaV,
    libraryDependencies ++=
      Seq(
        "com.lihaoyi" %%% "scalatags" % "0.6.7",
        "org.scala-js" %%% "scalajs-dom" % "0.9.1")
  )

initialCommands in console := "import toh._; import TowerSolver._"
