import sbt._

object Dependencies {
  private lazy val circeVersion = "0.14.4"
  private lazy val keycloakVersion = "21.0.0"
  private lazy val softWareMillVersion = "3.8.11"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15"
  lazy val keycloakAdapterCore  = "org.keycloak" % "keycloak-adapter-core" % keycloakVersion
  lazy val keycloakCore = "org.keycloak" % "keycloak-core" % keycloakVersion
  lazy val logger = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
  lazy val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.14"
  lazy val jbossLogging = "org.jboss.logging" % "jboss-logging" % "3.5.0.Final"
  lazy val keycloakMock = "com.tngtech.keycloakmock" % "mock" % "0.13.0"
  lazy val wiremock = "com.github.tomakehurst" % "wiremock-jre8" % "2.35.0"
  lazy val oauth2 = "com.nimbusds" % "oauth2-oidc-sdk" % "10.7"
  lazy val sttp = "com.softwaremill.sttp.client3" %% "core" % softWareMillVersion
  lazy val sttpCirce = "com.softwaremill.sttp.client3" %% "circe" % softWareMillVersion
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
}
