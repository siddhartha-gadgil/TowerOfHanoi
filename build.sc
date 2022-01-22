import mill._, scalalib._, scalajslib._, define.Task
import ammonite.ops._

object toh extends ScalaJSModule {
  def scalaVersion = "2.13.3"
  def scalaJSVersion = "1.5.0"
  import coursier.maven.MavenRepository

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases")
  )

  def ivyDeps = Agg(
    ivy"org.scala-js::scalajs-dom::2.1.0",
    ivy"com.lihaoyi::scalatags::0.11.1"
  )

  def pack() = T.command {
    def js = fastOpt()
    cp.over(js.path, pwd/ "docs" / "out.js")
    cp.over(js.path, pwd/ "docs" / "out.js.map")
    js
  }
}
