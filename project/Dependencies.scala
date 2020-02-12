import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  lazy val keycloakAdapterCore  = "org.keycloak" % "keycloak-adapter-core" % "8.0.1"
  lazy val keycloakCore = "org.keycloak" % "keycloak-core" % "8.0.1"
  lazy val logger = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  lazy val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.11"
  lazy val jbossLogging = "org.jboss.logging" % "jboss-logging" % "3.4.1.Final"

}
