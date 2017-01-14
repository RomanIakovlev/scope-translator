name := "scope-translator"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

organization := "net.iakovlev"

resolvers += Resolver.bintrayRepo("tek", "maven")

libraryDependencies ++= Seq("com.chuusai" %% "shapeless" % "2.3.2",
"org.specs2" %% "specs2-core" % "3.8.6" % "test",
    compilerPlugin("tryp" %% "splain" % "0.1.11"))

scalacOptions ++= Seq("-Xlog-implicits")
scalacOptions in Test ++= Seq("-Yrangepos")
