organization := "io.waylay"

name := "Bayes app plugins"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.2"

// disable using the Scala version in output paths and artifacts
crossPaths := false

scalacOptions += "-target:jvm-1.7" // "-target:jvm-1.8"

javacOptions in compile ++= Seq("-source", "1.8", "-target", "1.8")

javacOptions in doc ++= Seq("-source", "1.8")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

testOptions += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")

// disable java/scaladoc generation
publishArtifact in (Compile, packageDoc) := false

// needed to get the scripting engine working
// http://stackoverflow.com/questions/23567500/how-to-use-scriptengine-in-scalatest
fork in Test := true

val slf4jVersion = "1.7.7"
val twitter4jVersion = "4.0.1"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.jsoup" % "jsoup" % "1.7.3",
  "org.twitter4j" % "twitter4j-core" % twitter4jVersion,
  "org.twitter4j" % "twitter4j-stream" % twitter4jVersion,
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "javax.mail" % "mail" % "1.4.7",
  "com.google.code.gson" % "gson" % "2.2.4",
  "com.squareup.retrofit" % "retrofit" % "1.6.0",
  // the xively java client is currently useless
  // see https://github.com/xively/Xively4J/issues/12
  //"com.xively.client" % "xively-java-client" % "0.1.0-SNAPSHOT",
  // ===== test =====
  // if we want scala scripting (only since scala 2.11+)
  // we only enable this for testing, it' up to the distribution to
  // decide if they want to enable scala scripting
  // "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.typesafe.play" %% "play-json" % "2.4.0-M1" % "test",
  // scala test integration
  // Newer versions are even more broken https://github.com/etorreborre/specs2/issues/283
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  //  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
  //  "org.scalatest" %% "scalatest" % "2.1.5" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2" % "test"
)

unmanagedBase := baseDirectory.value / "donotuse"

unmanagedJars in Compile <<= baseDirectory map { base =>
  val baseLib = base / "lib"
  val customJars = (baseLib ** "waylay*.jar") +++ (baseLib ** "jspf*.jar") +++ (base ** "hue*.jar")
  customJars.classpath
}
