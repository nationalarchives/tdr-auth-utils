package uk.gov.nationalarchives.tdr.keycloak

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.scalalogging.Logger
import io.circe.Error
import io.circe.generic.auto._
import org.keycloak.adapters.rotation.AdapterTokenVerifier
import org.keycloak.representations.AccessToken
import sttp.client3._
import sttp.client3.circe._
import uk.gov.nationalarchives.tdr.keycloak.KeycloakUtils.{AuthResponse, UserDetails}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.reflect.{ClassTag, classTag}
import scala.util.Try

class KeycloakUtils(implicit val executionContext: ExecutionContext) {

  val logger: Logger = Logger("KeycloakUtils")
  val ttlSeconds: Int = 10

  case class MissingUserIdException() extends Exception("The user id in the token is missing")

  private def getAccessToken(token: String)(implicit keycloakDeployment: TdrKeycloakDeployment): Either[Throwable, AccessToken] = {
    Try {
      AdapterTokenVerifier.verifyToken(token, keycloakDeployment)
    }.toEither
  }

  def token(token:String)(implicit keycloakDeployment: TdrKeycloakDeployment): Either[Throwable, Token] = {
    getAccessToken(token).flatMap(at => {
      val validatedToken = Token(at, new BearerAccessToken(token))
      at.getOtherClaims.asScala.get("user_id") match {
        case Some(_) => Right(validatedToken)
        case None => Left(MissingUserIdException())
      }
    })
  }

  def serviceAccountToken[T[_]](clientId: String, clientSecret: String)(implicit backend: SttpBackend[T, Any], tag: ClassTag[T[_]], keycloakDeployment: TdrKeycloakDeployment): Future[BearerAccessToken] = {

    val body: Map[String, String] = Map("grant_type" -> "client_credentials")

    val response: T[Response[Either[ResponseException[String, Error], AuthResponse]]] = basicRequest
      .body(body)
      .auth.basic(clientId, clientSecret)
      .post(uri"${keycloakDeployment.getAuthServerBaseUrl}/realms/tdr/protocol/openid-connect/token")
      .response(asJson[AuthResponse])
      .send(backend)

    def process(response: Response[Either[ResponseException[String, Error], AuthResponse]]): Future[AuthResponse] = {
      response.body match {
        case Right(body) => Future.successful(body)
        case Left(e) => Future.failed(e)
      }
    }

    //The backend type is either SttpBackend[Future, Nothing, NothingT] for async backends or SttpBackend[Identity, Nothing, NothingT] for sync ones
    //There probably are other choices but these are the only ones we're using and we can always add another match in
    val authResponse = tag match {
      case futureTag if futureTag == classTag[Future[_]] => response.asInstanceOf[Future[Response[Either[ResponseException[String, Error], AuthResponse]]]].flatMap(process)
      case identityTag if identityTag == classTag[Identity[_]] => process(response.asInstanceOf[Identity[Response[Either[ResponseException[String, Error], AuthResponse]]]])
    }

    authResponse.map(res => new BearerAccessToken(res.access_token))
  }

  def userDetails[T[_]](userId: String, clientId: String, clientSecret: String)(implicit backend: SttpBackend[T, Any], tag: ClassTag[T[_]], keycloakDeployment: TdrKeycloakDeployment): Future[UserDetails] = {
    val body: Map[String, String] = Map("grant_type" -> "client_credentials")
    val response = for {
      bearerAccessToken <- serviceAccountToken(clientId, clientSecret)
      response = basicRequest
        .body(body)
        .auth.bearer(bearerAccessToken.toString)
        .get(uri"${keycloakDeployment.getAuthServerBaseUrl}/admin/realms/tdr/users/$userId")
        .response(asJson[UserDetails])
        .send(backend)
    } yield response

    def process(response: Response[Either[ResponseException[String, Error], UserDetails]]): Future[UserDetails] = {
      response.body match {
        case Right(userDetails) => Future.successful(userDetails)
        case Left(error) => Future.failed(error)
      }
    }

    //The backend type is either SttpBackend[Future, Nothing, NothingT] for async backends or SttpBackend[Identity, Nothing, NothingT] for sync ones
    //There probably are other choices but these are the only ones we're using and we can always add another match in
    val userResponse = tag match {
      case futureTag if futureTag == classTag[Future[_]] => response.asInstanceOf[Future[Response[Either[ResponseException[String, Error], UserDetails]]]].flatMap(process)
      case identityTag if identityTag == classTag[Identity[_]] => process(response.asInstanceOf[Identity[Response[Either[ResponseException[String, Error], UserDetails]]]])
    }
    logger.info(s"Details for user $userId requested by client $clientId")
    userResponse
  }
}

object KeycloakUtils {
  case class AuthResponse(access_token: String)
  case class UserDetails(email: String)

  def apply()(implicit executionContext: ExecutionContext): KeycloakUtils = new KeycloakUtils()
}
