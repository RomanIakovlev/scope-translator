name := "scope-translator"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.11", "2.12.4")

organization := "net.iakovlev"

resolvers += Resolver.bintrayRepo("tek", "maven")

libraryDependencies ++= Seq("com.chuusai" %% "shapeless" % "2.3.2",
                            "org.specs2" %% "specs2-core" % "3.8.6" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

addCompilerPlugin("io.tryp" % "splain" % "0.2.7" cross CrossVersion.patch)
