organization := "io.waylay"

name := "Bayes app plugins"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.1"

scalacOptions += "-target:jvm-1.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "commons-logging" % "commons-logging" % "1.0.4",
  "org.jsoup" % "jsoup" % "1.7.3",
  "org.twitter4j" % "twitter4j-core" % "3.0.4",
  "org.twitter4j" % "twitter4j-stream" % "3.0.4",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "de.congrace" % "exp4j" % "0.3.11",
  "org.antlr" % "ST4" % "4.0.7",
  "javax.mail" % "mail" % "1.4.7",
  // test
  "junit" % "junit" % "4.11" % "test",
  // scala test integration
//  "org.specs2" %% "specs2" % "2.3.12" % "test",
//  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
//  "org.scalatest" %% "scalatest" % "2.1.5" % "test",
  "com.novocode" % "junit-interface" % "0.11-RC1" % "test"
)

unmanagedBase := baseDirectory.value / "donotuse"

unmanagedJars in Compile <<= baseDirectory map { base =>
  val customJars = (base ** "waylay*.jar") +++ (base ** "jspf*.jar")
  customJars.classpath
}

javaSource in Test := baseDirectory.value / "src" / "main" / "test"

//autoScalaLibrary := false
//
//crossPaths := false


