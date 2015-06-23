val akkaVersion = "2.3.11"

lazy val root = (project in file(".")).
  settings(
    organization := "org.cognoseed",
    name := "tweetlazo",
    version := "0.1.0",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "org.twitter4j" % "twitter4j-stream" % "[4.0,)",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.github.wookietreiber" %% "scala-chart" % "latest.integration",
      "com.lowagie" % "itext" % "4.2.1", /* Not planning on making PDFs, but needed to shut it up. */
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    )
  )
