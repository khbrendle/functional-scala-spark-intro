package fss.app

import zio._
import zio.console.Console
import zio.blocking.Blocking
import fss.layer.Spark.{Test => SparkTest}
import fss.layer.Config.{EnvVar, Test => ConfigTest}
import fss.app.TestData._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

import scala.util.{Failure, Success}

@SuppressWarnings(
  Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements"))
class App extends AnyFunSpec with should.Matchers {

  describe("main execution") {
    it("should run successfully") {
      // define our mock config variables
      val mockConfig = Map[EnvVar, Option[String]](EnvVar.AppName -> Some("unit-testing"))
      // define out mock data to be used
      val mockData = Map[String, Seq[_]](
        "film" -> Seq(defaultFilm),
        "film_actor" -> Seq(defaultFilmActor),
        "actor" -> Seq(defaultActor)
      )

      // create the test config layer with mock values
      val testConfig = ConfigTest.layer(mockConfig)

      // create the test spark layer with mock values
      // this syntax is as follows
      //   1. (testConfig ++ Blocking.live ++ Console.live)
      //     combine these 3 layers
      //   2. (...) >>> SparkTest.layer(mockData)
      //     pass the combined layers as input to SparkTest.layer
      //     these 3 combined services are passed in their defined order to the (_, _, _) args in
      //     the layer definition `(Live(_, _, _)).toLayer`
      val testSpark = (testConfig ++ Blocking.live ++ Console.live) >>> SparkTest.layer(mockData)

      // combine the final layers to satisfy the requirements for `Main.prog`
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
