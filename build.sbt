resolvers += "jitpack" at "https://jitpack.io"

lazy val commonSettings = Seq(
  publishArtifact in(Compile, packageSrc) := false,
  resolvers += "jitpack" at "https://jitpack.io",
  organization := "com.sphera",
  scalaVersion := Dependencies.Version.scala,
  logLevel := Level.Warn,
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-deprecation", // warning and location for usages of deprecated APIs
    "-feature", // warning and location for usages of features that should be imported explicitly
    "-unchecked", // additional warnings where generated code depends on assumptions
    "-language:postfixOps",
    "-language:implicitConversions",
    //"-Xlint", // recommended additional warnings
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    //"-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-Ywarn-inaccessible",
    "-Ywarn-dead-code"
  ),

  mainClass in Compile := Some("sphera.core.server.SpheraApplication"),
  parallelExecution in Test := true,
  //testOptions in Test += Tests.Argument("-P"),
  fork in run := false
)



lazy val root = (project in file("."))
//  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "bpm-project",
    version := "3.2.1-SNAPSHOT"
  )
  .aggregate(spheraBpmServer, spheraBpm)
  //.dependsOn(spheraBpm, spheraFrontend)

lazy val spheraBpm = Project(
  id = "sphera-bpm",
  base = file("sphera-bpm")
)
//  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "sphera-bpm",
    version := "3.2.1-SNAPSHOT",
//    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, buildInfoBuildNumber),
//    buildInfoOptions += BuildInfoOption.BuildTime,
//    buildInfoPackage := "sphera.bpm",
    libraryDependencies ++= Dependencies.bpm,
    //PB.protobufSettings,

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),

    commonSettings
  )

lazy val spheraBpmServer = Project(
  id = "sphera-bpm-server",
  base = file("sphera-bpm-server")
)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    commonSettings,
    name := "sphera-bpm-server",
    version := "3.2.1-SNAPSHOT"
  ).dependsOn(spheraBpm)

mainClass in Compile := Some("sphera.core.server.SpheraApplication")
