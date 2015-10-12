import sbt._
import sbt.Classpaths.publishTask
import Keys._

import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm, extraOptions, jvmOptions, scalatestOptions, multiNodeExecuteTests, multiNodeJavaName, multiNodeHostsFileName, multiNodeTargetDirName, multiTestOptions }
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import sbtassembly.AssemblyPlugin.autoImport._

import org.apache.maven.artifact.handler.DefaultArtifactHandler

import com.typesafe.sbt.SbtNativePackager._, autoImport._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd, CmdLike}

import com.goshoplane.sbt.standard.libraries.StandardLibraries


object CreedBuild extends Build with StandardLibraries {

  lazy val makeScript = TaskKey[Unit]("make-script", "make script in local directory to run main classes")

  def sharedSettings = Seq(
    organization := "com.goshoplane",
    version := "1.0.0",
    scalaVersion := Version.scala,
    crossScalaVersions := Seq(Version.scala, "2.11.4"),
    scalacOptions := Seq("-unchecked", "-optimize", "-deprecation", "-feature", "-language:higherKinds", "-language:implicitConversions", "-language:postfixOps", "-language:reflectiveCalls", "-Yinline-warnings", "-encoding", "utf8"),
    retrieveManaged := true,

    fork := true,
    javaOptions += "-Xmx2500M",

    resolvers ++= StandardResolvers,

    publishMavenStyle := true
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings


  lazy val creed = Project(
    id = "creed",
    base = file("."),
    settings = Project.defaultSettings ++
      sharedSettings
  ) aggregate (core, search, query, service)



  lazy val core = Project(
    id = "creed-core",
    base = file("core"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "creed-core",

    libraryDependencies ++= Seq(
      "com.goshoplane" %% "neutrino-core" % "1.1.1"
    ) ++ Libs.commonsCore
      ++ Libs.commonsCatalogue
      ++ Libs.playJson
  )

  lazy val query = Project(
    id = "creed-query",
    base = file("query"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "creed-query",

    libraryDependencies ++= Seq(
      "com.goshoplane" %% "neutrino-core" % "1.1.1"
    ) ++ Libs.lucene
      ++ Libs.akka
  ).dependsOn(core)

  lazy val search = Project(
    id = "creed-search",
    base = file("search"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "creed-search",

    libraryDependencies ++= Seq(
    ) ++ Libs.lucene
      ++ Libs.akka
      ++ Libs.akkaCluster
      ++ Libs.fastutil
      ++ Libs.slf4j
      ++ Libs.logback
  ).dependsOn(core, query)

  lazy val service = Project(
    id = "creed-service",
    base = file("service"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).enablePlugins(JavaAppPackaging)
  .settings(
    name := "creed-service",
    mainClass in Compile := Some("creed.service.CreedServer"),

    libraryDependencies ++= Seq(
    ) ++ Libs.microservice,

    makeScript <<= (stage in Universal, stagingDirectory in Universal, baseDirectory in ThisBuild, streams) map { (_, dir, cwd, streams) =>
      var path = dir / "bin" / "creed-service"
      sbt.Process(Seq("ln", "-sf", path.toString, "creed-service"), cwd) ! streams.log
    }
  ).dependsOn(search)
}