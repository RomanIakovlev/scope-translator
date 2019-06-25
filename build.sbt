import sbt.Keys.crossScalaVersions

scalaVersion := "2.13.0"

crossScalaVersions := Seq("2.11.11", "2.12.8", "2.13.0")

organization := "net.iakovlev"

libraryDependencies ++= Seq(
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor < 13 =>
      "com.chuusai" %% "shapeless" % "2.3.3"
    case _ =>
      "com.chuusai" %% "shapeless" % "2.3.3" % "test"
  },
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.specs2" %% "specs2-core" % "4.5.1" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")
