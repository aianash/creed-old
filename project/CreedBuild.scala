import sbt._
import sbt.Classpaths.publishTask
import Keys._

import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm, extraOptions, jvmOptions, scalatestOptions, multiNodeExecuteTests, multiNodeJavaName, multiNodeHostsFileName, multiNodeTargetDirName, multiTestOptions }

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import com.typesafe.sbt.SbtStartScript

import sbtassembly.AssemblyPlugin.autoImport._

import com.twitter.scrooge.ScroogeSBT

object CreedBuild extends Build with Libraries {

  def sharedSettings = Seq(
    organization := "com.goshoplane",
    version := "0.0.1",
    scalaVersion := Version.scala,
    crossScalaVersions := Seq(Version.scala, "2.11.4"),
    scalacOptions := Seq("-unchecked", "-optimize", "-deprecation", "-feature", "-language:higherKinds", "-language:implicitConversions", "-language:postfixOps", "-language:reflectiveCalls", "-Yinline-warnings", "-encoding", "utf8"),
    retrieveManaged := true,

    fork := true,
    javaOptions += "-Xmx2500M",

    resolvers ++= Seq(
      "ReaderDeck Releases" at "http://repo.readerdeck.com/artifactory/readerdeck-releases",
      "anormcypher" at "http://repo.anormcypher.org/",
      "Akka Repository" at "http://repo.akka.io/releases",
      "Spray Repository" at "http://repo.spray.io/",
      "twitter-repo" at "http://maven.twttr.com",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
    ),

    publishMavenStyle := true
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings


  lazy val creed = Project(
    id = "creed",
    base = file("."),
    settings = Project.defaultSettings ++
      sharedSettings
  ) aggregate (core, indexer, service)



  lazy val core = Project(
    id = "creed-core",
    base = file("core"),
    settings = Project.defaultSettings ++
      sharedSettings ++
      SbtStartScript.startScriptForClassesSettings ++
      ScroogeSBT.newSettings
  ).settings(
    name := "creed-core",

    libraryDependencies ++= Seq(
    ) ++ Libs.scalaz
      ++ Libs.scroogeCore
      ++ Libs.finagleThrift
      ++ Libs.libThrift
      ++ Libs.akka
      ++ Libs.scaldi
  )

  lazy val indexer = Project(
    id = "creed-indexer",
    base = file("indexer"),
    settings = Project.defaultSettings ++
      sharedSettings ++
      SbtStartScript.startScriptForClassesSettings
  ).settings(
    name := "creed-indexer",

    libraryDependencies ++= Seq(
    ) ++ Libs.lucene
      ++ Libs.akka
      ++ Libs.scalaz
      ++ Libs.slf4j
      ++ Libs.logback
      ++ Libs.scaldiAkka
      ++ Libs.bijection
  ).dependsOn(core)

  lazy val service = Project(
    id = "creed-service",
    base = file("service"),
    settings = Project.defaultSettings ++
      sharedSettings ++
      SbtStartScript.startScriptForClassesSettings
  ).settings(
    name := "creed-service",

    libraryDependencies ++= Seq(
    ) ++ Libs.akka
      ++ Libs.slf4j
      ++ Libs.logback
      ++ Libs.finagleCore
      ++ Libs.scalaJLine
      ++ Libs.mimepull
      ++ Libs.scaldi
      ++ Libs.scaldiAkka
      ++ Libs.bijection
  ).dependsOn(core)

}