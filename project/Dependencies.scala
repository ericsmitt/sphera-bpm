import sbt._

object Dependencies {
  object Version {
    val scala = "2.12.4"
    val akka = "2.5.3"
    val akkaPersistenceCassandra = "0.54"
    val akkaPersistenceInmemoryVersion = "2.5.1.1"
    val akkaHttp = "10.0.9"
    val phantom = "2.12.1"
    val protobuf = "3.2.0"
    val guice = "4.2.0"
    val jwt = "1.2.2"
    val akkaHttpCircle = "1.17.0"
    val circle = "0.8.0"
    val scalaj = "2.3.0"
    val scalamock = "3.6.0"
    val apachePoi = "3.17"
    val alpakka = "0.13"
    val spheraCore = "3.4.1"
    val enumeratum = "1.5.13"
  }

  val core: Seq[sbt.ModuleID] = Seq (
    "com.sphera" %% "sphera-core" % Version.spheraCore,
    "com.sphera" %% "sphera-core-test" % Version.spheraCore % Test
  )

  lazy val bpm: Seq[sbt.ModuleID] = Seq(
    "com.sun.mail" % "javax.mail" % "1.6.0",
    "org.scalaj" %% "scalaj-http" % Version.scalaj,
    "org.scalamock" %% "scalamock-scalatest-support" % Version.scalamock % Test,
    "com.roundeights" %% "hasher" % "1.2.0",
    "com.jsuereth" %% "scala-arm" % "2.0",
    "io.circe" %% "circe-core" % Version.circle,
    "io.circe" %% "circe-generic" % Version.circle,
    "io.circe" %% "circe-parser" % Version.circle,
    "io.circe" %% "circe-java8" % Version.circle,
    "io.circe" %% "circe-optics" % Version.circle,
    "com.beachape"   %% "enumeratum"           % Version.enumeratum,
    "com.beachape"   %% "enumeratum-circe"     % Version.enumeratum,
    "com.github.everit-org.json-schema" % "org.everit.json.schema" % "1.8.0"
  ) ++ core ++ tests ++ guice ++ reflect

  val reflect = Seq(
    "org.scala-lang" % "scala-reflect" % Version.scala
  )
  val guice = Seq(
    "com.google.inject" % "guice" % Version.guice,
    "net.codingwell" %% "scala-guice" % Version.guice
  )

  val tests = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "com.typesafe.akka" %% "akka-testkit" % Version.akka % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % Version.akkaPersistenceInmemoryVersion % Test,
    "commons-io" % "commons-io" % "2.5" % "test"
  )
}
