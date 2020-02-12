import Dependencies._

lazy val supportedScalaVersions = List("2.13.0", "2.12.8")
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "uk.gov.nationalarchives.tdr"

lazy val root = (project in file("."))
  .settings(
    name := "tdr-auth-utils",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      keycloakAdapterCore,
      keycloakCore,
      httpComponents,
      logger,
      jbossLogging
    ),
    crossScalaVersions := supportedScalaVersions
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
