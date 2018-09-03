organization := "org.virtuslab"

name := "play-slick-configuration"

version := "2.2.0-SNAPSHOT"

scalaVersion := "2.11.8"

crossScalaVersions := Seq(scalaVersion.value)

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "com.typesafe.play" %% "play-slick" % "2.1.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.play" %% "play-test" % "2.5.14" % "test",
  "com.h2database" % "h2" % "1.4.187" % "test",
  "org.scalatestplus" %% "play" % "1.4.0" % "test"
)

incOptions := incOptions.value.withNameHashing(true)

pomExtra := <url>https://github.com/VirtusLab/play-slick-configuration</url>
  <licenses>
    <license>
      <name>Apache-style</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/VirtusLab/play-slick-configuration.git</url>
    <connection>scm:git:git@github.com:VirtusLab/play-slick-configuration.git</connection>
  </scm>
  <developers>
    <developer>
      <id>VirtusLab</id>
      <name>VirtusLab</name>
      <url>http://www.virtuslab.com/</url>
    </developer>
    <developer>
      <id>JerzyMuller</id>
      <name>Jerzy Müller</name>
      <url>https://github.com/Kwestor</url>
    </developer>
  </developers>;

xerial.sbt.Sonatype.sonatypeSettings
