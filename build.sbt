
resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "io.spray" % "spray-can" % "1.2.0",
  "io.spray" % "spray-routing" % "1.2.0",
  "io.spray" %% "spray-json" % "1.2.5",
  "io.spray" % "spray-client" % "1.2.0",
  "io.spray" % "spray-util" % "1.2.0",
  "io.spray" % "spray-http" % "1.2.0",
  "io.spray" % "spray-httpx" % "1.2.0",
  "org.json4s" %% "json4s-native" % "3.2.4",
  "org.json4s" %% "json4s-jackson" % "3.2.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13"
)
