package uk.gov.nationalarchives.tdr.keycloak

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.Error

import org.keycloak.adapters.rotation.AdapterTokenVerifier
import org.keycloak.representations.AccessToken
import sttp.client._
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.future.AsyncHttpClientFutureBackend
import sttp.client.circe._
import uk.gov.nationalarchives.tdr.keycloak.KeycloakUtils.AuthResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class KeycloakUtils(url: String)(implicit val executionContext: ExecutionContext) {

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

  def token(token:String): Option[Token] = {
    getAccessToken(token).map(at => Token(at, new BearerAccessToken(token)))
  }

  def serviceAccountToken(clientId: String, clientSecret: String): Future[BearerAccessToken] = {
    implicit val backend: SttpBackend[Future, Nothing, WebSocketHandler] = AsyncHttpClientFutureBackend()

    val body: Map[String, String] = Map("grant_type" -> "client_credentials")

    val response: Future[Response[Either[ResponseError[Error], AuthResponse]]] = basicRequest
      .body(body)
      .auth.basic(clientId, clientSecret)
      .post(uri"$url/realms/tdr/protocol/openid-connect/token")
      .response(asJson[AuthResponse])
      .send()

    val authResponse = response.flatMap { r =>
      r.body match {
        case Right(body) => Future.successful(body)
        case Left(e) => Future.failed(e)
      }
    }

    authResponse.map(res => new BearerAccessToken(res.access_token))
  }
}

object KeycloakUtils {
  case class AuthResponse(access_token: String)

  def apply(url: String)(implicit executionContext: ExecutionContext): KeycloakUtils = new KeycloakUtils(url)
}
