lazy val root = (project in file(".")).
  settings(
    organization := "org.cognoseed",
    name := "tweetlazo",
    version := "0.1.0",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "org.twitter4j" % "twitter4j-stream" % "[4.0,)",
      "com.typesafe.akka" %% "akka-actor" % "2.3.11",
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
    )
  )