package uk.gov.nationalarchives.tdr.keycloak

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.tngtech.keycloakmock.api.KeycloakVerificationMock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.ExecutionContext

class ServiceTest extends AnyFlatSpec with BeforeAndAfterEach with BeforeAndAfterAll {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val mock: KeycloakVerificationMock = new KeycloakVerificationMock(9050, "tdr")
  val port = 9050
  val utils = KeycloakUtils(s"http://localhost:$port/auth")

  val wiremockAuthServer = new WireMockServer(0)

  val authPath = "/auth/realms/tdr/protocol/openid-connect/token"
  def authUrl = wiremockAuthServer.url("/auth")

  def authOk = wiremockAuthServer.stubFor(post(urlEqualTo(authPath))
    .willReturn(okJson("""{"access_token": "token"}""")))

  def authUnavailable = wiremockAuthServer.stubFor(post(urlEqualTo(authPath)).willReturn(serverError()))

  override def beforeAll(): Unit = {
    mock.start()
    wiremockAuthServer.start()
  }

  override def afterAll(): Unit = {
    mock.stop()
    wiremockAuthServer.stop()
  }

  override def afterEach(): Unit = {
    wiremockAuthServer.resetAll()
  }
}
