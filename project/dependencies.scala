import sbt._

object dependencies {
  val zioVersion: String = "1.0.12"

  val testDependencies = Seq(
    "dev.zio" %% "zio-test" % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.11"
  )

  val mainDependencies = Seq(
    "dev.zio" %% "zio" % zioVersion,
    "org.typelevel" %% "cats-kernel" % "2.5.0",
    "org.typelevel" %% "cats-core" % "2.5.0",
    "org.postgresql" % "postgresql" % "42.3.0",
    "org.apache.spark" %% "spark-sql" % "3.1.2" % "provided"
  )
}
