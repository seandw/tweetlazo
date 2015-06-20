lazy val root = (project in file(".")).
  settings(
    organization := "org.cognoseed",
    name := "tweetlazo",
    version := "0.1.0",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "org.twitter4j" % "twitter4j-stream" % "[4.0,)",
      "com.typesafe.akka" %% "akka-actor" % "2.3.11",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
    )
  )
