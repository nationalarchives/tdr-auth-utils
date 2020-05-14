package uk.gov.nationalarchives.tdr.keycloak

import org.scalatest.flatspec.AnyFlatSpec
import com.tngtech.keycloakmock.api.KeycloakVerificationMock
import com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext


class KeycloakUtilsTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val mock: KeycloakVerificationMock = new KeycloakVerificationMock(9050, "tdr")
  val port = 9050
  val utils = KeycloakUtils(s"http://localhost:$port/auth")

  override def beforeEach(): Unit = {
    mock.start()
  }

  override def afterEach(): Unit = {
    mock.stop()
  }

  "The token method " should "return a bearer token for a valid token string " in {
    val mockToken = mock.getAccessToken(aTokenConfig().build())
    val token = utils.token(mockToken).get
    token.bearerAccessToken.getValue should equal(mockToken)
  }

  "The token method " should "return the correct user id for a valid token" in {
    val userId = "1"
    val mockToken = mock.getAccessToken(aTokenConfig().withClaim("user_id", userId).build())
    val token = utils.token(mockToken).get
    token.userId should equal(Some(userId))
  }

  "The token method " should "return the correct transferring body for a valid token" in {
    val body = "body"
    val mockToken = mock.getAccessToken(aTokenConfig().withClaim("body", body).build())
    val token = utils.token(mockToken).get
    token.transferringBody should equal(Some(body))
  }

  "The token method " should "return the correct roles for a valid token" in {
    val role = "role_admin"
    val mockToken = mock.getAccessToken(aTokenConfig().withResourceRole("tdr", role).build())
    val token = utils.token(mockToken).get
    token.roles.size should be(1)
    token.roles should contain(role)
  }

  "The token method " should "return an empty user id for an invalid token" in {
    val mockToken = "faketoken"
    val token = utils.token(mockToken)
    token.isDefined should be(false)
  }
}
