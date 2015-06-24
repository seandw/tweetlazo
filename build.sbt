val akkaVersion = "2.3.11"

lazy val root = (project in file(".")).
  settings(
    organization := "org.cognoseed",
    name := "tweetlazo",
    version := "0.1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.twitter4j" % "twitter4j-stream" % "[4.0,)",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "org.scalafx" %% "scalafx" % "[8.0,)",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    ),
    // to catch the default styles used in javafx applications
    unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))
  )
