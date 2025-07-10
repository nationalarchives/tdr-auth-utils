import Dependencies._
import sbt.url
import sbtrelease.ReleaseStateTransformations.{checkSnapshotDependencies, commitNextVersion, commitReleaseVersion, inquireVersions, pushChanges, runClean, runTest, setNextVersion, setReleaseVersion, tagRelease}
import sbt.internal.librarymanagement.Publishing.sonaRelease

ThisBuild / version := (ThisBuild / version).value
ThisBuild / organization     := "uk.gov.nationalarchives"
ThisBuild / organizationName := "National Archives"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/nationalarchives/tdr-auth-utils-data"),
    "git@github.com:nationalarchives/tdr-auth-utils.git"
  )
)
developers := List(
  Developer(
    id    = "tna-da-bot",
    name  = "TNA Digital Archiving",
    email = "s-GitHubDABot@nationalarchives.gov.uk",
    url   = url("https://github.com/nationalarchives/tdr-generated-grapqhl")
  )
)

ThisBuild / description := "Helper classes related to Keycloak and authentication for the Transfer Digital Records service"
ThisBuild / licenses := List("MIT" -> new URL("https://choosealicense.com/licenses/mit/"))
ThisBuild / homepage := Some(url("https://github.com/nationalarchives/tdr-auth-utils"))

scalaVersion := "2.13.16"

useGpgPinentry := true
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
publishMavenStyle := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val root = (project in file("."))
  .settings(
    name := "tdr-auth-utils",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      keycloakAdapterCore,
      keycloakCore,
      httpComponents,
      logger,
      jbossLogging,
      oauth2,
      sttp,
      sttpCirce,
      circeCore,
      circeGeneric,
      keycloakMock % Test,
      wiremock % Test
    )
  )
