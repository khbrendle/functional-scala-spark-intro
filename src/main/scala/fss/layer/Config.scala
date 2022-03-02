package fss.layer

import zio.{Has, RIO, Task, TaskLayer, ZIO, ZLayer}

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

  // testing resources

  /** Returns a mock environment based on the input variable map
    *
    * @param env [[Map]][ [[EnvVar]], [[Option]][ [[String]] ] ]
    */
  final case class Test(val env: Map[EnvVar, Option[String]]) extends Service {
    def envGet(e: EnvVar): Task[String] =
      ZIO.getOrFailWith(new Error(s"could not source EnvVar `${e}`"))(
        env.getOrElse(e, None)
      )
  }

  object Test {

    /** Returns the mock config layer
      *
      * @param env
      * @return
      */
    def layer(env: Map[EnvVar, Option[String]]): Env = {
      ZLayer.succeed(Test(env))
    }
  }
}
