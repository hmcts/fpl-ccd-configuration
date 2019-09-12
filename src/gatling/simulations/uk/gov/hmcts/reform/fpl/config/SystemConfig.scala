package uk.gov.hmcts.reform.fpl.config

import com.typesafe.config.ConfigFactory

object SystemConfig {
  private val config = ConfigFactory.load()

  val url: String = config.getString("url")

  val idamUrl: String = config.getString("auth.idam.url")
  val idamClientId: String = config.getString("auth.idam.clientId")
  val idamClientSecret: String = config.getString("auth.idam.clientSecret")
  val idamRedirectUri: String = config.getString("auth.idam.redirectUri")

  val s2sUrl: String = config.getString("auth.s2s.url")
  val s2sSecret: String = config.getString("auth.s2s.secret")
}
