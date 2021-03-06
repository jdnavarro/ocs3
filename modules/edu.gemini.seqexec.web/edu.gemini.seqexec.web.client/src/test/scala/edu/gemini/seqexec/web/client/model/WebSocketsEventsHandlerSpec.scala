package edu.gemini.seqexec.web.client.model

import edu.gemini.seqexec.model.Model.SeqexecEvent.{ConnectionOpenEvent, SequenceLoaded}
import edu.gemini.seqexec.model.Model.{Conditions, SequenceView, SequencesQueue}
import edu.gemini.seqexec.model.UserDetails
import edu.gemini.seqexec.web.client.model.SeqexecCircuit.zoomRW
import org.scalatest.{FlatSpec, Matchers}

import scalaz._
import Scalaz._

class WebSocketsEventsHandlerSpec extends FlatSpec with Matchers {

  "WebSocketsEventsHandler" should "accept connection open events anonymously" in {
    val handler = new WebSocketEventsHandler(zoomRW(m => (m.uiModel.sequences, m.uiModel.user))((m, v) => m.copy(uiModel = m.uiModel.copy(sequences = v._1, user = v._2))))
    val result = handler.handle(ServerMessage(ConnectionOpenEvent(None)))
    // No user set
    result.newModelOpt.flatMap(_.uiModel.user) shouldBe None
  }
  it should "set the user if the response contains one" in {
    val handler = new WebSocketEventsHandler(zoomRW(m => (m.uiModel.sequences, m.uiModel.user))((m, v) => m.copy(uiModel = m.uiModel.copy(sequences = v._1, user = v._2))))
    val user = UserDetails("user", "User name")
    val result = handler.handle(ServerMessage(ConnectionOpenEvent(Some(user))))
    // No user set
    result.newModelOpt.flatMap(_.uiModel.user) shouldBe Some(user)
  }
  it should "accept a loaded SequencesQueue" in {
    val sequences = SequencesQueue(Conditions.default, None, List.empty[SequenceView])
    val handler = new WebSocketEventsHandler(zoomRW(m => (m.uiModel.sequences, m.uiModel.user))((m, v) => m.copy(uiModel = m.uiModel.copy(sequences = v._1, user = v._2))))
    val result = handler.handle(ServerMessage(SequenceLoaded("TESTS-1", sequences)))
    // No user set
    result.newModelOpt.exists(_.uiModel.sequences === sequences) shouldBe true
  }

}
