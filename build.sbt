import dependencies._

val warts = Warts.allBut(Wart.Recursion, Wart.Overloading, Wart.Product)

ThisBuild / organization := "fss"
ThisBuild / scalaVersion := "2.12.12"
ThisBuild / version := "0.1.0"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "utf-8", // all source files utf-8
  "-explaintypes", // explain type errors in more detail
  "-feature", // reduce the magic
  "language:existentials",
  "language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint:adapted-args",
  "-Xlint:by-name-right-associative",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-override",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Xlint:unsound-match",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
//  "-Ywarn-unused:params",
  "-Ywarn-macros:after",
  "-Ywarn-unused:patvars",
  "-Ywarn-value-discard",
  "-Ylog-classpath"
)

lazy val settings = Seq(
  name := "fss-fss.app",
  libraryDependencies ++= mainDependencies ++ testDependencies,
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  wartremoverErrors ++= warts,
  assembly / mainClass := Some("fss.app.Main"),
  assembly / assemblyJarName := s"fss-${version.value}.jar"
//  publishArtifact := false // only push assembly jar which we will specify
)

lazy val root = (project in file("."))
  .settings(settings)
