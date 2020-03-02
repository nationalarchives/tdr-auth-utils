package uk.gov.nationalarchives.tdr.keycloak

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import org.keycloak.representations.AccessToken

import scala.collection.JavaConverters._

class Token(private val token: Option[AccessToken], val bearerAccessToken: BearerAccessToken) {
  private def getOtherClaim(name: String): Option[String] = token.flatMap(t => t.getOtherClaims.asScala.get(name).asInstanceOf[Option[String]])

  def userId: Option[String] = getOtherClaim("user_id")
  def transferringBody: Option[String] = getOtherClaim("body")
  def roles: Set[String] =
    token.map(_.getResourceAccess("tdr")) match {
      case Some(access) => access.getRoles.asScala.toSet
      case None => Set()
  }
}
object Token {
  def apply(token: Option[AccessToken], bearerAccessToken: BearerAccessToken): Token = new Token(token, bearerAccessToken)
}