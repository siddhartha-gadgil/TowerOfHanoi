lazy val scalaV = "2.11.8"

lazy val root = (project in file(".")).
  settings(
    organization := "in.ac.iisc",
    name := "Tower-of-Hanoi",
    version := "0.5",
    scalaVersion := scalaV
  )
