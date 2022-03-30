import sbt._

object Dependencies {
  private lazy val circeVersion = "0.14.1"
  private lazy val keycloakVersion = "16.1.0"
  private lazy val softWareMillVersion = "2.3.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0-M2"
  lazy val keycloakAdapterCore  = "org.keycloak" % "keycloak-adapter-core" % keycloakVersion
  lazy val keycloakCore = "org.keycloak" % "keycloak-core" % keycloakVersion
  lazy val logger = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
  lazy val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.11"
  lazy val jbossLogging = "org.jboss.logging" % "jboss-logging" % "3.4.3.Final"
  lazy val keycloakMock = "com.tngtech.keycloakmock" % "mock" % "0.11.0"
  lazy val wiremock = "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3"
  lazy val oauth2 = "com.nimbusds" % "oauth2-oidc-sdk" % "7.1.3"
  lazy val sttp = "com.softwaremill.sttp.client" %% "core" % softWareMillVersion
  lazy val sttpCirce = "com.softwaremill.sttp.client" %% "circe" % softWareMillVersion
  lazy val sttpAsyncClient = "com.softwaremill.sttp.client" %% "async-http-client-backend-future" % "2.3.0"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
}
