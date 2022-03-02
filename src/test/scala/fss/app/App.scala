package fss.app

import zio._
import zio.console.Console
import zio.blocking.Blocking
import fss.layer.Spark.{Test => SparkTest}
import fss.layer.Config.{EnvVar, Test => ConfigTest}
import fss.app.Types.Film
import fss.app.TestData._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

import scala.util.{Failure, Success}

@SuppressWarnings(
  Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements"))
class App extends AnyFunSpec with should.Matchers {

  describe("main execution") {
    it("should run successfully") {
      val testConfig = ConfigTest.layer(Map(EnvVar.AppName -> Some("unit-testing")))
      val testSpark =
        (testConfig ++ Blocking.live ++ Console.live) >>> SparkTest.layer(
          Map(
            "film" -> Seq(defaultFilm),
            "film_actor" -> Seq(defaultFilmActor),
            "actor" -> Seq(defaultActor)
          )
        )
      val cake = testConfig ++ Console.live ++ testSpark
      val r = Runtime.default.unsafeRun(Main.prog.provideLayer(cake).either).toTry

      r match {
        case Success(_) =>
          succeed
        case Failure(f) =>
          fail(s"main execution failed: $f")
      }
    }
  }
}
