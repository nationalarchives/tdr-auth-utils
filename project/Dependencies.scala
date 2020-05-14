import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0-M2"
  lazy val keycloakAdapterCore  = "org.keycloak" % "keycloak-adapter-core" % "8.0.1"
  lazy val keycloakCore = "org.keycloak" % "keycloak-core" % "8.0.1"
  lazy val logger = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  lazy val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.11"
  lazy val jbossLogging = "org.jboss.logging" % "jboss-logging" % "3.4.1.Final"
  lazy val keycloakMock = "com.tngtech.keycloakmock" % "mock" % "0.2.0"
  lazy val wiremock = "com.github.tomakehurst" % "wiremock-jre8" % "2.26.0"
  lazy val oauth2 = "com.nimbusds" % "oauth2-oidc-sdk" % "7.1.1"
  lazy val sttp = "com.softwaremill.sttp.client" %% "core" % "2.1.1"
  lazy val sttpCirce = "com.softwaremill.sttp.client" %% "circe" % "2.1.1"
  lazy val sttpAsyncClient = "com.softwaremill.sttp.client" %% "async-http-client-backend-future" % "2.0.0-RC9"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.13.0"
  lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.13.0"



}
