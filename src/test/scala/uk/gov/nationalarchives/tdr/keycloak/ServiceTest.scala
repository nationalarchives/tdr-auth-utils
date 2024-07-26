package uk.gov.nationalarchives.tdr.keycloak

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.tngtech.keycloakmock.api.{KeycloakMock, ServerConfig}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, EitherValues}

import scala.concurrent.ExecutionContext

class ServiceTest extends AnyFlatSpec with BeforeAndAfterEach with BeforeAndAfterAll with EitherValues {

  implicit val ec: ExecutionContext = ExecutionContext.global
  val serverConfig: ServerConfig = ServerConfig.aServerConfig()
    .withPort(9050)
    .withDefaultRealm("tdr")
    .build()
  val mock: KeycloakMock = new KeycloakMock(serverConfig)
  val port = 9050
  val url = s"http://localhost:$port/auth"
  val utils: KeycloakUtils = KeycloakUtils()

  val wiremockAuthServer = new WireMockServer(0)

  val authPath = "/auth/realms/tdr/protocol/openid-connect/token"

  def authUrl: String = wiremockAuthServer.url("/auth")

  def authOk: StubMapping = wiremockAuthServer.stubFor(post(urlEqualTo(authPath))
    .willReturn(okJson("""{"access_token": "token"}""")))

  def authUnavailable(url: String = authPath): StubMapping = wiremockAuthServer.stubFor(post(urlEqualTo(url)).willReturn(serverError()))

  val userPath = "/auth/admin/realms/tdr/users"

  def userOk(userId: String): StubMapping = wiremockAuthServer.stubFor(get(urlEqualTo(s"$userPath/$userId"))
    .willReturn(okJson("""{"email":  "some.person@some.xy"}""")))

  def userDetailsUnavailable(url: String = userPath): StubMapping = wiremockAuthServer.stubFor(get(urlEqualTo(url)).willReturn(serverError()))

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
