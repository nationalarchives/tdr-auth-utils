package uk.gov.nationalarchives.tdr.keycloak

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig
import org.scalatest.matchers.should.Matchers._
import sttp.client.HttpError

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration.Duration

class KeycloakUtilsTest extends ServiceTest {
  def await[T](result: Awaitable[T]): T = Await.result(result, Duration(5, TimeUnit.SECONDS))

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

  "The service account token method" should "call the auth service" in {
    authOk
    val utils = KeycloakUtils(authUrl)
    await(utils.serviceAccountToken("id", "secret"))

    wiremockAuthServer.verify(postRequestedFor(urlEqualTo(authPath))
      .withRequestBody(equalTo("grant_type=client_credentials"))
      .withHeader("Authorization", equalTo("Basic aWQ6c2VjcmV0")))
  }

  "The service account token method" should "return an error if the api is unavailable" in {
    authUnavailable
    val utils = KeycloakUtils(authUrl)
    val exception = intercept[HttpError] {
      await(utils.serviceAccountToken("id", "secret"))
    }
    exception.body should equal("")
  }
}
