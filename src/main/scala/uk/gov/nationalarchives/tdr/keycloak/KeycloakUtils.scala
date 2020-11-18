package uk.gov.nationalarchives.tdr.keycloak

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.scalalogging.Logger
import io.circe.Error
import io.circe.generic.auto._
import org.keycloak.adapters.rotation.AdapterTokenVerifier
import org.keycloak.representations.AccessToken
import sttp.client._
import sttp.client.circe._
import uk.gov.nationalarchives.tdr.keycloak.KeycloakUtils.AuthResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.language.higherKinds
import scala.reflect.{ClassTag, classTag}
import scala.util.Try

class KeycloakUtils(url: String)(implicit val executionContext: ExecutionContext) {

  val logger = Logger("KeycloakUtils")
  val ttlSeconds: Int = 10
  val keycloakDeployment = TdrKeycloakDeployment(url, "tdr", ttlSeconds)

  case class MissingUserIdException() extends Exception("The user id in the token is missing")

  private def getAccessToken(token: String): Either[Throwable, AccessToken] = {
    Try {
      AdapterTokenVerifier.verifyToken(token, keycloakDeployment)
    }.toEither
  }

  def token(token:String): Either[Throwable, Token] = {
    getAccessToken(token).flatMap(at => {
      val validatedToken = Token(at, new BearerAccessToken(token))
      at.getOtherClaims.asScala.get("user_id") match {
        case Some(_) => Right(validatedToken)
        case None => Left(MissingUserIdException())
      }
    })
  }

  def serviceAccountToken[T[_]](clientId: String, clientSecret: String)(implicit backend: SttpBackend[T, Nothing, NothingT], tag: ClassTag[T[_]]): Future[BearerAccessToken] = {

    val body: Map[String, String] = Map("grant_type" -> "client_credentials")

    val response: T[Response[Either[ResponseError[Error], AuthResponse]]] = basicRequest
      .body(body)
      .auth.basic(clientId, clientSecret)
      .post(uri"$url/realms/tdr/protocol/openid-connect/token")
      .response(asJson[AuthResponse])
      .send()

    def process(response: Response[Either[ResponseError[Error], AuthResponse]]) = {
      response.body match {
        case Right(body) => Future.successful(body)
        case Left(e) => Future.failed(e)
      }
    }

    //The backend type is either SttpBackend[Future, Nothing, NothingT] for async backends or SttpBackend[Identity, Nothing, NothingT] for sync ones
    //There probably are other choices but these are the only ones we're using and we can always add another match in
    val authResponse = tag match {
      case futureTag if futureTag == classTag[Future[_]] => response.asInstanceOf[Future[Response[Either[ResponseError[Error], AuthResponse]]]].flatMap(process)
      case identityTag if identityTag == classTag[Identity[_]] => process(response.asInstanceOf[Identity[Response[Either[ResponseError[Error], AuthResponse]]]])
    }

    authResponse.map(res => new BearerAccessToken(res.access_token))
  }
}

object KeycloakUtils {
  case class AuthResponse(access_token: String)

  def apply(url: String)(implicit executionContext: ExecutionContext): KeycloakUtils = new KeycloakUtils(url)
}
