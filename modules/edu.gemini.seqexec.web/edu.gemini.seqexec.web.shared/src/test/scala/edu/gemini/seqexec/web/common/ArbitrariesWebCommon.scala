package edu.gemini.seqexec.web.common

import java.util.logging.Level

import org.scalacheck.{Arbitrary, _}
import org.scalacheck.Arbitrary._

trait ArbitrariesWebCommon {

  implicit val arbLevel: Arbitrary[LogMessage] =
    Arbitrary {
      for {
        m <- arbitrary[String]
        l <- Gen.oneOf(Seq(Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST))
      } yield LogMessage(l, m)
    }
}
