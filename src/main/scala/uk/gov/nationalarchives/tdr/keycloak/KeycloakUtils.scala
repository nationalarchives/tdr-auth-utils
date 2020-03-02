package uk.gov.nationalarchives.tdr.keycloak

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.scalalogging.Logger
import org.keycloak.adapters.rotation.AdapterTokenVerifier
import org.keycloak.representations.AccessToken

import scala.util.{Failure, Success, Try}

class KeycloakUtils(url: String) {

  val logger = Logger("KeycloakUtils")
  val ttlSeconds: Int = 10
  val keycloakDeployment = TdrKeycloakDeployment(url, "tdr", ttlSeconds)

  private def getAccessToken(token: String): Option[AccessToken] = {
    val tryVerify = Try {
      AdapterTokenVerifier.verifyToken(token, keycloakDeployment)
    }
    tryVerify match {
      case Success(token) => Some(token)
      case Failure(e) =>
        logger.warn(e.getMessage)
        Option.empty
    }
  }

  def token(token:String): Token = {
    Token(getAccessToken(token), new BearerAccessToken(token))
  }
}

object KeycloakUtils {
  def apply(url: String): KeycloakUtils = new KeycloakUtils(url)
}
