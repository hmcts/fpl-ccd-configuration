package uk.gov.hmcts.reform.fpl.config

import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

object SimulationParams {
  private val config = ConfigFactory.load()

  val numberOfUsers: Int = config.getInt("params.numberOfUsers")
  val rampUpTimeInSeconds: FiniteDuration = config.getInt("params.rampUpTimeInSeconds").seconds
}
