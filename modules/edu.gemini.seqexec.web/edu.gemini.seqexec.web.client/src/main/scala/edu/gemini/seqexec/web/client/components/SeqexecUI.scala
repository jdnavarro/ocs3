package edu.gemini.seqexec.web.client.components

import edu.gemini.seqexec.web.client.components.sequence.SequenceArea
import edu.gemini.seqexec.web.client.model.SeqexecCircuit
import edu.gemini.seqexec.web.client.model.InstrumentNames
import edu.gemini.seqexec.model.Model.{Instrument, SequenceId}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._

/**
  * Top level UI component
  */
object SeqexecUI {
  private val lbConnect = SeqexecCircuit.connect(_.loginBox)
  private val wsConsoleConnect = SeqexecCircuit.connect(m => (m.devConsoleState, m.webSocketLog))
  sealed trait SeqexecPages

  case object Root extends SeqexecPages
  case class InstrumentPage(i: Instrument, obsId: Option[SequenceId]) extends SeqexecPages

  case class RouterProps(page: InstrumentPage, router: RouterCtl[InstrumentPage])

  val routerConfig = RouterConfigDsl[SeqexecPages].buildConfig { dsl =>
    import dsl._

    (emptyRule
    | staticRoute(root, Root) ~> renderR(r => SequenceArea(RouterProps(InstrumentPage("Flamingos2", None), r.narrow)))
    | dynamicRoute(("/" ~ string("[a-zA-Z0-9-]+") ~ "/" ~ string("[a-zA-Z0-9-]+").option).caseClass[InstrumentPage]) {
        case x @ InstrumentPage(i, _) if InstrumentNames.instruments.list.toList.contains(i) => x
      } ~> dynRenderR((p, r) => SequenceArea(RouterProps(p, r.narrow[InstrumentPage])))
    )
      .notFound(redirectToPage(Root)(Redirect.Push))
      // Runtime verification that all pages are routed
      .verify(Root, InstrumentNames.instruments.list.toList.map(i => InstrumentPage(i, None)): _*)
      .renderWith(layout)
      .logToConsole
  }

  val router = Router(BaseUrl.fromWindowOrigin, routerConfig)

  def layout(c: RouterCtl[SeqexecPages], r: Resolution[SeqexecPages]) =
    <.div(
      NavBar(),
      wsConsoleConnect(u => WebSocketsConsole(u()._1, u()._2)),
      QueueArea(c.narrow[InstrumentPage]),
      r.render(),
      lbConnect(LoginBox.apply)
    )

}
