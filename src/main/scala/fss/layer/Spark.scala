package fss.layer

import zio._
import org.apache.spark.sql.{Dataset, SparkSession}
import zio.blocking.Blocking
import fss.layer.Config.{EnvVar, Service => ConfigService}
import zio.console.Console

import java.util.Properties
import scala.collection.JavaConverters.mapAsJavaMap
import scala.reflect.runtime.universe.TypeTag

/** This object will contain our Spark layer, this should encapsulate our access to spark.
  *
  * The [[Spark.Service]] object defines the methods that will be implemented by the layer.
  *
  * We will use [[Spark.sparkLayer]] to access these functions from within a function that
  * has the layer as part of it's environment.
  *
  * For our deployed runtime, we will use [[Spark.Live]] to provide access to our actual Spark
  * with full implementations of [[Spark.Service]] methods.
  *
  * For testing purposes, we will use [[Spark.Test]] to provide access to mock implementations
  * of [[Spark.Service]]. When we initialize this layer we can pass the mock data that is
  * required.
  */
@SuppressWarnings(Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any"))
object Spark {

  /** Defines the methods that will be implemented by the layer
    */
  trait Service {
    def _spark: RIO[Has[Service], SparkSession]
    def readDatabase[A <: Product: TypeTag](
        table: String,
        host: String,
        port: Long,
        database: String,
        user: String,
        password: String
    ): RIO[Has[Service], Dataset[A]]
  }

  /** Defines the layer that will be created
    */
  type Env = RLayer[
    Has[ConfigService] with Has[Blocking.Service] with Has[Console.Service],
    Has[Service]
  ]

  /** Accessor object for layer methods
    */
  object sparkLayer {
    def spark: RIO[Has[Service], SparkSession] = ZIO.serviceWith[Service](_._spark)

    def readDatabase[A <: Product: TypeTag](
        table: String,
        host: String,
        port: Long,
        database: String,
        user: String,
        password: String
    ): ZIO[Has[Service], Throwable, Dataset[A]] = {
      ZIO.serviceWith[Service](_.readDatabase[A](table, host, port, database, user, password))
    }
  }

  private def postgresURI(host: String, port: Long, database: String): UIO[String] =
    ZIO.succeed(s"jdbc:postgresql://$host:$port/$database")

  /** Return the live implementation of the spark layer. This will source
    * from the system to initiate a spark connection to provide required
    * methods
    */
  final case class Live(config: ConfigService, blocking: Blocking.Service, console: Console.Service) extends Service {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    override val _spark: RIO[Has[Service], SparkSession] =
      for {
        appName <- config.envGet(EnvVar.AppName)
        spark <- blocking.effectBlocking(
          SparkSession.builder().appName(appName).getOrCreate()
        )
        _ <- ZIO.effect(spark.sparkContext.setLogLevel("ERROR"))
      } yield spark

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    override def readDatabase[A <: Product: TypeTag](
        table: String,
        host: String,
        port: Long,
        database: String,
        user: String,
        password: String
    ): RIO[Has[Service], Dataset[A]] = {
      for {
        spark <- _spark
        host <- postgresURI(host, port, database)
        props <- ZIO.effect[Properties] {
          val p = new Properties()
          p.putAll(
            mapAsJavaMap(
              Map[String, String]("user" -> user, "password" -> password, "driver" -> "org.postgresql.Driver")))
          p
        }
        df <- blocking.effectBlocking {
          import spark.implicits._
          spark.read.jdbc(host, table, props).as[A]
        }
      } yield df
    }
  }
  object Live {

    /** constructed layer to be used in our application
      */
    val layer: Env = (Live(_, _, _)).toLayer
  }

  /** Returns a mock spark handler that runs on our development machine
    *
    * This function is curried so that we can execute `Test(mockData)` which
    * returns the layer still needing config, blocking, and console passed in
    */
  final case class Test(databaseData: Map[String, Seq[_]])(
      config: ConfigService,
      blocking: Blocking.Service,
      console: Console.Service
  ) extends Service {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    override val _spark: RIO[Has[Service], SparkSession] =
      for {
        appName <- config.envGet(EnvVar.AppName)
        spark <- blocking.effectBlocking(
          SparkSession
            .builder()
            .master("local[2]") // provide at least 2 cores
            .appName(appName)
            .getOrCreate()
        )
        _ <- ZIO.effect(spark.sparkContext.setLogLevel("ERROR"))
      } yield spark

    @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.AsInstanceOf"))
    override def readDatabase[A <: Product: TypeTag](
        table: String,
        host: String,
        port: Long,
        database: String,
        user: String,
        password: String
    ): RIO[Has[Service], Dataset[A]] = {
      for {
        _ <- console.putStrLn("importing spark session")
        spark <- _spark
        _ <- console.putStrLn("getting table")
        seq <- ZIO.getOrFailWith(new Error(s"could not load table $table"))(databaseData.get(table))
        _ <- console.putStrLn("converting dataset")
        ds <- ZIO.effect {
          import spark.implicits._
          seq.asInstanceOf[Seq[A]].toDS().as[A]
        }
      } yield ds
    }
  }

  /** Returns the mock spark layer
    */
  object Test {
    def layer(databaseData: Map[String, Seq[_]]): Env = (Test(databaseData)(_, _, _)).toLayer
  }
}
