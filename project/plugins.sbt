// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.1")


addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.11")
libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.2"


addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.7.1")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

//addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")