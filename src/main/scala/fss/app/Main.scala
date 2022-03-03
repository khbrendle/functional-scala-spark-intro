package fss.app

import fss.layer.Spark.{sparkLayer, Live => SparkLive, Service => SparkService}
import fss.layer.Config.{EnvVar, configLayer, Live => ConfigLive, Service => ConfigService}
import fss.app.Transform._
import fss.app.Types._
import zio.{ExitCode, Has, ZEnv, ZIO}
import zio.blocking.Blocking
import zio.console.{Console, putStrLn}

/** Main is our application entry point. We split into 2 parts
  *
  * The first part is `prog` which is the business logic that we want to execute. This is split
  * so that we can run this in unit testing with mock layers
  *
  * The second part is `run` which is executed when we run the compiled jar. This will have
  * the layers defined for the live runtime
  */
@SuppressWarnings(
  Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any", "org.wartremover.warts.ImplicitConversion"))
object Main extends zio.App {
  val prog: ZIO[Console with Has[ConfigService] with Has[SparkService], Throwable, Unit] =
    for {
      spark <- sparkLayer.spark
      appName <- configLayer.envGet(EnvVar.AppName)
      _ <- putStrLn(s"Running app $appName")
//      read in required data
      films <- {
        sparkLayer.readDatabase[Film](
          table = "film",
          host = "localhost",
          port = 15432L,
          database = "postgres",
          user = "postgres",
          password = "test")
      }
      _ <- putStrLn(s"there are ${films.count()} films")
      actors <- {
        sparkLayer.readDatabase[Actor](
          table = "actor",
          host = "localhost",
          port = 15432L,
          database = "postgres",
          user = "postgres",
          password = "test")
      }
      _ <- putStrLn(s"there are ${actors.count()} actors")
      filmActors <- {
        sparkLayer.readDatabase[FilmActor](
          table = "film_actor",
          host = "localhost",
          port = 15432L,
          database = "postgres",
          user = "postgres",
          password = "test")
      }
      _ <- putStrLn(s"there are ${filmActors.count()} actor-film relationships")
//      perform our safe transformations
      longFilms <- ZIO.effect(filterFilms(spark, films))
      filmActors <- ZIO.effect(joinFilmsAndActors(spark, longFilms, filmActors, actors))
      actorFilmCount <- ZIO.effect(calculateActorFilmCount(spark, filmActors))
      _ <- putStrLn(s"there are ${actorFilmCount.count()} rows")
//      do something unsafe with the output
      _ <- ZIO.foreach(actorFilmCount.collect())(r => putStrLn(s"${r.actor_name} -> ${r.film_count}"))
    } yield ()

  /* This is the function that will be executed by ZIO when we pass Main class to be executed
   */
  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    (prog
      .provideLayer(
        ConfigLive.layer ++ Console.live ++ ((ConfigLive.layer ++ Blocking.live ++ Console.live) >>> SparkLive.layer)))
      .exitCode
}
