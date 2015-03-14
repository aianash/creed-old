package creed.service

import scaldi.Module
import scaldi.Injectable._

package object injectors {
  class CreedServiceModule extends Module {
    bind [CreedService] to new CreedService
  }
}