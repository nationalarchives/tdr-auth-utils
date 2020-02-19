package uk.gov.nationalarchives.tdr.keycloak

import org.scalatest.flatspec.AnyFlatSpec
import com.tngtech.keycloakmock.api.KeycloakVerificationMock
import com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers


class KeycloakUtilsTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  var mock: KeycloakVerificationMock = _
  val port = 9050
  val utils = KeycloakUtils(s"http://localhost:$port/auth")

  override def beforeEach(): Unit = {
    mock = new KeycloakVerificationMock(9050, "tdr")
    mock.start()
  }

  override def afterEach(): Unit = {
    mock.stop()
  }

  "The verifyToken method " should "return a token for a valid token string " in {
    val token = utils.verifyToken(mock.getAccessToken(aTokenConfig().build()))
    assert(token.isDefined)
  }

  "The verifyToken method " should "return a option empty for an invalid token string " in {
    val token = utils.verifyToken("faketoken")
    assert(token.isEmpty)
  }
}
