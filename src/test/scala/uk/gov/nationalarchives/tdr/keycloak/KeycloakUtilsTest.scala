package uk.gov.nationalarchives.tdr.keycloak

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, postRequestedFor, urlEqualTo}
import com.tngtech.keycloakmock.api.TokenConfig
import com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig
import org.scalatest.matchers.should.Matchers._
import sttp.client3.{HttpError, HttpURLConnectionBackend, Identity, SttpBackend}

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

class KeycloakUtilsTest extends ServiceTest {
  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  def await[T](result: Awaitable[T]): T = Await.result(result, Duration(5, TimeUnit.SECONDS))

  val userId: UUID = UUID.randomUUID()

  def configWithUser: TokenConfig.Builder = aTokenConfig().withClaim("user_id", userId.toString)

  "The token method " should "return a bearer token for a valid token string " in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build)
    val token: Token = utils.token(mockToken).value
    token.bearerAccessToken.getValue should equal(mockToken)
  }

  "The token method " should "return the correct user id for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).value
    token.userId should equal(userId)
  }

  "The token method " should "return the correct transferring body for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val body = "body"
    val mockToken = mock.getAccessToken(configWithUser.withClaim("body", body).build())
    val token = utils.token(mockToken).value
    token.transferringBody should equal(Some(body))
  }

  "The token method " should "return the correct name for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val name = "name"
    val mockToken = mock.getAccessToken(configWithUser.withGivenName(name).build())
    val token = utils.token(mockToken).value
    token.name should equal(name)
  }

  "The token method " should "return the email for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val email = "email"
    val mockToken = mock.getAccessToken(configWithUser.withEmail(email).build())
    val token = utils.token(mockToken).value
    token.email should equal(email)
  }

  "The token method " should "return judgment user type 'true' where claim set to true" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("judgment_user", "true").build())
    val token = utils.token(mockToken).value
    token.isJudgmentUser should equal(true)
  }

  "The token method " should "return judgment user type 'false' where claim set to false" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("judgment_user", "false").build())
    val token = utils.token(mockToken).value
    token.isJudgmentUser should equal(false)
  }

  "The token method" should "return judgment user type 'false' where claim not set" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).value
    token.isJudgmentUser should equal(false)
  }

  "The token method " should "return standard user type 'true' where claim set to true" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("standard_user", "true").build())
    val token = utils.token(mockToken).value
    token.isStandardUser should equal(true)
  }

  "The token method " should "return standard user type 'false' where claim set to false" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("standard_user", "false").build())
    val token = utils.token(mockToken).value
    token.isStandardUser should equal(false)
  }

  "The token method " should "return tna user type 'true' where claim set to true" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("tna_user", "true").build())
    val token = utils.token(mockToken).value
    token.isTNAUser should equal(true)
  }

  "The token method " should "return transfer advisor user type 'false' where claim set to false" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("tna_user", "false").build())
    val token = utils.token(mockToken).value
    token.isTNAUser should equal(false)
  }

  "The token method " should "return transfer advisor user type 'true' where claim set to true" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("transfer_adviser", "true").build())
    val token = utils.token(mockToken).value
    token.isTransferAdviser should equal(true)
  }

  "The token method " should "return tna user type 'false' where claim set to false" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.withClaim("transfer_adviser", "false").build())
    val token = utils.token(mockToken).value
    token.isTransferAdviser should equal(false)
  }

  "The token method " should "return standard user type 'false' where claim not test" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).value
    token.isStandardUser should equal(false)
  }

  "The token method " should "return the correct roles for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "role_admin"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr", role).build())
    val token = utils.token(mockToken).value
    token.roles.size should be(1)
    token.roles should contain(role)
  }

  "The token method" should "return the correct back end checks roles for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "backend_check_role"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr-backend-checks", role).build())
    val token = utils.token(mockToken).value
    token.backendChecksRoles.size should be(1)
    token.backendChecksRoles should contain(role)
  }

  "The token method" should "return no back end checks roles for a valid token if no roles defined" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).value
    token.backendChecksRoles.size should be(0)
  }

  "The token method" should "return the correct reporting role for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "reporting_role"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr-reporting", role).build())
    val token = utils.token(mockToken).value
    token.reportingRoles.size should be(1)
    token.reportingRoles should contain(role)
  }

  "The token method" should "return no reporting roles for a valid token if no roles defined" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).value
    token.reportingRoles.size should be(0)
  }

  "The token method" should "return the correct transfer service role for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "service_role"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr-transfer-service", role).build())
    val token = utils.token(mockToken).value
    token.transferServiceRoles.size should be(1)
    token.transferServiceRoles should contain(role)
  }

  "The token method" should "return no transfer service roles for a valid token if no roles defined" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).value
    token.transferServiceRoles.size should be(0)
  }

  "The token method" should "return the correct draft metadata role for a valid token" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val role = "draft_metadata_role"
    val mockToken = mock.getAccessToken(configWithUser.withResourceRole("tdr-draft-metadata", role).build())
    val token = utils.token(mockToken).value
    token.draftMetadataRoles.size should be(1)
    token.draftMetadataRoles should contain(role)
  }

  "The token method" should "return no draft metadata roles for a valid token if no roles defined" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(url, "tdr", 3600)
    val mockToken = mock.getAccessToken(configWithUser.build())
    val token = utils.token(mockToken).value
    token.draftMetadataRoles.size should be(0)
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
    authUnavailable()
    val utils = KeycloakUtils()
    val exception = intercept[HttpError[String]] {
      await(utils.serviceAccountToken("id", "secret"))
    }
    exception.body should equal("")
  }

  "'user details' method for the given user id" should "return the user's details" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(authUrl, "tdr", 3600)
    authOk
    userOk(userId.toString)
    val utils = KeycloakUtils()

    val response = await(utils.userDetails(userId.toString, "clientId", "secret"))

    wiremockAuthServer.verify(getRequestedFor(urlEqualTo(s"$userPath/${userId.toString}"))
      .withHeader("Authorization", equalTo("Bearer token")))

    response.email should equal("some.person@some.xy")
  }

  "'user details' method for the given user id" should "return an error if the api is unavailable" in {
    implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(authUrl, "tdr", 3600)
    authOk
    userDetailsUnavailable(s"$userPath/${userId.toString}")
    val utils = KeycloakUtils()
    val exception = intercept[HttpError[String]] {
      await(utils.userDetails(userId.toString, "clientId", "secret"))
    }
    exception.body should equal("")
  }
}
