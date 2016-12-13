package edu.gemini.seqexec.web.server

import org.scalatest._
import selenium._

import org.openqa.selenium._
import remote.{DesiredCapabilities, RemoteWebDriver}
import java.net.URL

class HomepageSpec extends FlatSpec with Matchers with WebBrowser {

  var username = sys.env("SAUCE_USERNAME")

  var access_key = sys.env("SAUCE_ACCESS_KEY")

  var caps = DesiredCapabilities.chrome()
  caps.setCapability("platform", "Windows XP")
  caps.setCapability("version", "43.0")
  caps.setCapability("tunnel_identifier", "TRAVIS_JOB_NUMBER")

  implicit val webDriver: WebDriver = new RemoteWebDriver(
    new URL("https://" + username + ":" + access_key + "@ondemand.saucelabs.com/wd/hub"),
    caps
  )

  val host = "http://localhost:9090/"

  "The home page" should "have the correct title" in {
    go to host
    pageTitle should be ("Seqexec")
  }

}
