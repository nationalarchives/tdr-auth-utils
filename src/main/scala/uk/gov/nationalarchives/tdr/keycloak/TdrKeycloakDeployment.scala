package uk.gov.nationalarchives.tdr.keycloak

import org.keycloak.adapters.{HttpClientBuilder, KeycloakDeployment}
import org.keycloak.adapters.rotation.JWKPublicKeyLocator
import org.keycloak.representations.adapters.config.AdapterConfig

class TdrKeycloakDeployment extends KeycloakDeployment {
}

object TdrKeycloakDeployment {
  def apply(authServer: String, realm: String, ttlSeconds: Int): TdrKeycloakDeployment = {
    val keycloakDeployment = new TdrKeycloakDeployment()
    keycloakDeployment.setClient(new HttpClientBuilder().build())

    keycloakDeployment.setPublicKeyCacheTtl(ttlSeconds)

    val adaptorConfig = new AdapterConfig()
    adaptorConfig.setAuthServerUrl(authServer)
    keycloakDeployment.setAuthServerBaseUrl(adaptorConfig)

    keycloakDeployment.setRealm(realm)

    keycloakDeployment.setPublicKeyLocator(new JWKPublicKeyLocator)
    keycloakDeployment
  }
}
