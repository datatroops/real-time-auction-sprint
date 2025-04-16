ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "Akka HTTP"
  )

resolvers += "Akka library repository".at("https://repo.akka.io/maven")
val akkaVersion = "2.6.20"
val akkaHttpVersion = "10.2.10"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "org.postgresql" % "postgresql" % "42.7.5",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.2",
  "com.typesafe.slick" %% "slick" % "3.5.2",
  "ch.qos.logback" % "logback-classic" % "1.4.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.mockito" %% "mockito-scala" % "1.17.37" % Test,
  "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.testcontainers" % "testcontainers" % "1.19.3" % Test,
  "org.testcontainers" % "postgresql" % "1.19.3" % Test,
)

mainClass in compile := Some("io.datatroops.MainApp")
