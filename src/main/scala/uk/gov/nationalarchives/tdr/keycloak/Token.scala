package uk.gov.nationalarchives.tdr.keycloak

import java.util.UUID

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import org.keycloak.representations.AccessToken

import scala.collection.JavaConverters._

class Token(private val token: AccessToken, val bearerAccessToken: BearerAccessToken) {
  private def getOtherClaim(name: String): Option[String] = token.getOtherClaims.asScala.get(name).map(_.toString)

  // The method to get the token verifies that the user_id is set so this should never be empty
  def userId: UUID = UUID.fromString(getOtherClaim("user_id").get)
  def transferringBody: Option[String] = getOtherClaim("body")
  def roles: Set[String] =
    Option(token.getResourceAccess("tdr")) match {
      case Some(access) => access.getRoles.asScala.toSet
      case None => Set()
  }
  def backendChecksRoles: Set[String] =
    Option(token.getResourceAccess("tdr-backend-checks")) match {
      case Some(access) => access.getRoles.asScala.toSet
      case None => Set()
    }
}
object Token {
  def apply(token: AccessToken, bearerAccessToken: BearerAccessToken): Token = new Token(token, bearerAccessToken)
}