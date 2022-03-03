package fss.layer

import zio.{Has, RIO, Task, TaskLayer, ZIO, ZLayer}

/** This object will contain our config layer, this should encapsulate our access to configuration.
  *
  * The [[Config.Service]] object defines the methods that will be implemented by the layer.
  *
  * We will use [[Config.configLayer]] to access these functions from within a function that
  * has the layer as part of it's environment.
  *
  * For our deployed runtime, we will use [[Config.Live]] to provide access to our actual configuration
  * with full implementations of [[Config.Service]] methods.
  *
  * For testing purposes, we will use [[Config.Test]] to provide access to mock implementations
  * of [[Config.Service]]. When we initialize this layer we can pass the mock data that is
  * required.
  */
@SuppressWarnings(Array("org.wartremover.warts.Nothing"))
object Config {

  sealed abstract class EnvVar(val s: String) {
    override def toString: String = s
  }
  object EnvVar {
    case object AppName extends EnvVar("SPARK_JOB_NAME")
  }
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def envVarToString(e: EnvVar): String = e.toString

  /** Defines methods that will be implimented by the layer
    */
  trait Service {
    def envGet(e: EnvVar): Task[String]
  }

  /** Defines the environment requirements for the config layer
    */
  type Env = TaskLayer[Has[Service]]

  /** Accessor object for layer methods
    */
  object configLayer {
    def envGet(e: EnvVar): RIO[Has[Service], String] = ZIO.serviceWith[Service](_.envGet(e))
  }

  /** Returns the live implementation of the config service. This will source
    * from the running environment to supply configuration related information
    */
  final case class Live() extends Service {
    private val env = Map[EnvVar, Option[String]](
      EnvVar.AppName -> sys.env.get(EnvVar.AppName)
    )

    /** Implements the getting of environment variable
      *
      * @param e [[EnvVar]] for the requesting variable
      * @return [[Task]][ [[String]] ] representing the variable set
      */
    override def envGet(e: EnvVar): Task[String] =
      ZIO.getOrFailWith[Throwable, String](new Error(s"could not source EnvVar `${e}`"))(
        env.getOrElse(e, None)
      )
  }
  object Live {

    /** Constructed layer to be used in cake
      */
    val layer: Env = ZLayer.succeed(Live())
  }

  /** Returns a mock environment based on the input variable map
    *
    * @param envGet [[Map]][ [[EnvVar]], [[Option]][ [[String]] ] ]
    */
  final case class Test(env: Map[EnvVar, Option[String]]) extends Service {
    def envGet(e: EnvVar): Task[String] =
      ZIO.getOrFailWith(new Error(s"could not source EnvVar `${e}`"))(
        env.getOrElse(e, None)
      )
  }

  object Test {

    /** Returns the mock config layer
      *
      * @param env: [[Map]][ [[Config.EnvVar]], [[Option]][ [[String]] ] ]
      * @return [[Config.Env]]
      */
    def layer(env: Map[EnvVar, Option[String]]): Env = {
      ZLayer.succeed(Test(env))
    }
  }
}
