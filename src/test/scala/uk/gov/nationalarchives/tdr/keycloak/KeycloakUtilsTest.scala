package uk.gov.nationalarchives.tdr.keycloak


import java.util.UUID
import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import com.tngtech.keycloakmock.api.TokenConfig
import com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig
import org.scalatest.matchers.should.Matchers._
import sttp.client.{HttpError, HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

class KeycloakUtilsTest extends ServiceTest {
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
  def await[T](result: Awaitable[T]): T = Await.result(result, Duration(5, TimeUnit.SECONDS))
  val userId: UUID = UUID.randomUUID()
  def configWithUser: TokenConfig.Builder = aTokenConfig().withClaim("user_id", userId.toString)

  "The token method " should "return a bearer token for a valid token string " in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build)
    val token: Token = utils.token(mockToken).right.value
    token.bearerAccessToken.getValue should equal(mockToken)
  }

  "The token method " should "return the correct user id for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).right.value
    token.userId should equal(userId)
  }

  "The token method " should "return the correct transferring body for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val body = "body"
    val mockToken = mock.getAccessToken(configWithUser.withClaim("body", body).build())
    val token = utils.token(mockToken).right.value
    token.transferringBody should equal(Some(body))
  }

  "The token method " should "return the correct roles for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "role_admin"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr", role).build())
    val token = utils.token(mockToken).right.value
    token.roles.size should be(1)
    token.roles should contain(role)
  }

  "The token method" should "return the correct back end checks roles for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "backend_check_role"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr-backend-checks", role).build())
    val token = utils.token(mockToken).right.value
    token.backendChecksRoles.size should be (1)
    token.backendChecksRoles should contain(role)
  }

  "The token method" should "return no back end checks roles for a valid token if no roles defined" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).right.value
    token.backendChecksRoles.size should be (0)
  }

  "The token method" should "return the correct reporting role for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "reporting_role"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr-reporting", role).build())
    val token = utils.token(mockToken).right.value
    token.reportingRoles.size should be (1)
    token.reportingRoles should contain(role)
  }

  "The token method" should "return no reporting roles for a valid token if not roles defined" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).right.value
    token.reportingRoles.size should be (0)
  }

  "The token method " should "return an error for an invalid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = "faketoken"
    val token = utils.token(mockToken)
    token.isLeft should be(true)
  }

  "The token method" should "return an error if the user id is not set in the token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(aTokenConfig().build())
    val error = utils.token(mockToken).left.value
    error.getMessage should equal("The user id in the token is missing")
  }

  "The service account token method" should "call the auth service" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(authUrl, "tdr", 3600)
    authOk
    val utils = KeycloakUtils()
    await(utils.serviceAccountToken("id", "secret"))

    wiremockAuthServer.verify(postRequestedFor(urlEqualTo(authPath))
      .withRequestBody(equalTo("grant_type=client_credentials"))
      .withHeader("Authorization", equalTo("Basic aWQ6c2VjcmV0")))
  }

  "The service account token method" should "return an error if the api is unavailable" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(authUrl, "tdr", 3600)
    authUnavailable
    val utils = KeycloakUtils()
    val exception = intercept[HttpError] {
      await(utils.serviceAccountToken("id", "secret"))
    }
    exception.body should equal("")
  }
}
