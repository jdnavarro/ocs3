package edu.gemini.seqexec.web.client.components

import diode.react.ModelProxy
import edu.gemini.seqexec.model.Model.SequenceState
import edu.gemini.seqexec.web.client.model._
import edu.gemini.seqexec.web.client.model.Pages._
import edu.gemini.seqexec.web.client.model.ModelOps._
import edu.gemini.seqexec.web.client.semanticui.elements.icon.Icon.{IconAttention, IconCheckmark, IconCircleNotched, IconSelectedRadio}
import edu.gemini.seqexec.web.client.semanticui.elements.table.TableHeader
import edu.gemini.seqexec.web.client.services.HtmlConstants.{iconEmpty, nbsp}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.RouterCtl
import org.scalajs.dom.html.TableRow

import scalacss.ScalaCssReact._
import scalaz.syntax.show._
import scalaz.syntax.equal._
import scalaz.syntax.std.option._

object QueueTableBody {
  type SequencesModel = ModelProxy[StatusAndLoadedSequencesFocus]

  case class Props(ctl: RouterCtl[SeqexecPages], sequences: SequencesModel)

  // Minimum rows to display, pad with empty rows if needed
  val minRows = 5

  def emptyRow(k: String, isLogged: Boolean): VdomTagOf[TableRow] = {
    <.tr(
      ^.key := k, // React requires unique keys
      <.td(
        ^.cls := "collapsing",
        iconEmpty),
      <.td(nbsp),
      <.td(nbsp),
      <.td(nbsp),
      <.td(
        SeqexecStyles.notInMobile,
        nbsp).when(isLogged)
    )
  }

  def showSequence(p: Props, s: SequenceInQueue): Callback =
    // Request to display the selected sequence
    p.sequences.dispatchCB(NavigateTo(InstrumentPage(s.instrument, s.id.some))) >> p.sequences.dispatchCB(SelectIdToDisplay(s.id))

  private val component = ScalaComponent.builder[Props]("QueueTableBody")
    .render_P { p =>
      val (isLogged, sequences) = (p.sequences().isLogged, p.sequences().sequences)
      <.table(
        ^.cls := "ui selectable compact celled table unstackable",
        <.thead(
          <.tr(
            SeqexecStyles.notInMobile,
            TableHeader(TableHeader.Props(collapsing = true),  iconEmpty),
            TableHeader("Obs ID"),
            TableHeader("State"),
            TableHeader("Instrument"),
            TableHeader("Obs. Name").when(isLogged)
          )
        ),
        <.tbody(
          sequences.map(Some.apply).padTo(minRows, None).zipWithIndex.collect {
            case (Some(s), i) =>
              val leftColumnIcon: TagMod =
                s.status match {
                  case SequenceState.Completed => IconCheckmark
                  case SequenceState.Running   => IconCircleNotched.copy(IconCircleNotched.p.copy(loading = true))
                  case SequenceState.Error(_)  => IconAttention
                  case _                       => if (s.active) IconSelectedRadio else iconEmpty
                }
              val stepAtText = s.status.shows + s.runningStep.map(u => s" ${u._1 + 1}/${u._2}").getOrElse("")
              val inProcess = s.status.isInProcess
              val selectableRowCls = List(
                  ^.classSet(
                    "selectable" -> !inProcess
                  ),
                  SeqexecStyles.linkeableRows.when(inProcess)
                )
              <.tr(
                ^.classSet(
                  "positive" -> (s.status === SequenceState.Completed),
                  "warning"  -> (s.status === SequenceState.Running),
                  "negative" -> s.status.isError,
                  "active"   -> (s.active && !inProcess)
                ),
                ^.key := s"item.queue.$i",
                ^.onClick --> showSequence(p, s),
                <.td(
                  ^.cls := "collapsing",
                  selectableRowCls.toTagMod,
                  p.ctl.link(InstrumentPage(s.instrument, s.id.some))(leftColumnIcon).unless(inProcess),
                  leftColumnIcon.when(inProcess)
                ),
                <.td(
                  ^.cls := "collapsing",
                  selectableRowCls.toTagMod,
                  p.ctl.link(InstrumentPage(s.instrument, s.id.some))(s.id).unless(inProcess),
                  s.id.when(inProcess)
                ),
                <.td(
                  ^.cls := "collapsing",
                  selectableRowCls.toTagMod,
                  p.ctl.link(InstrumentPage(s.instrument, s.id.some))(stepAtText).unless(inProcess),
                  stepAtText.when(inProcess)
                ),
                <.td(
                  selectableRowCls.toTagMod,
                  p.ctl.link(InstrumentPage(s.instrument, s.id.some))(s.instrument.shows).unless(inProcess),
                  s.instrument.shows.when(inProcess)
                ),
                <.td(
                  selectableRowCls.toTagMod,
                  SeqexecStyles.notInMobile,
                  p.ctl.link(InstrumentPage(s.instrument, s.id.some))(s.name).unless(inProcess),
                  s.name.when(inProcess)
                ).when(isLogged)
              )
            case (_, i) =>
              emptyRow(s"item.queue.$i", isLogged)
          }.toTagMod
        )
      )
    }
    .build

  def apply(ctl: RouterCtl[SeqexecPages], p: SequencesModel): Unmounted[Props, Unit, Unit] = component(Props(ctl, p))

}

/**
  * Container for the queue table
  */
object QueueTableSection {
  private val sequencesConnect = SeqexecCircuit.connect(SeqexecCircuit.statusAndLoadedSequencesReader)

  private val component = ScalaComponent.builder[RouterCtl[SeqexecPages]]("QueueTableSection")
    .stateless
    .render_P(p =>
      <.div(
        ^.cls := "ui segment scroll pane",
        SeqexecStyles.queueListPane,
        sequencesConnect(c => QueueTableBody(p, c))
      )
    ).build

  def apply(ctl: RouterCtl[SeqexecPages]): Unmounted[RouterCtl[SeqexecPages], Unit, Unit] = component(ctl)

}

/**
  * Displays the elements on the queue
  */
object QueueArea {

  private val component = ScalaComponent.builder[RouterCtl[SeqexecPages]]("QueueArea")
    .stateless
    .render_P(p =>
      <.div(
        ^.cls := "ui raised segments container",
        TextMenuSegment("Night Queue", "key.queue.menu"),
        <.div(
          ^.cls := "ui attached segment",
          <.div(
            ^.cls := "ui grid",
            <.div(
              ^.cls := "stretched row",
              <.div(
                ^.cls := "sixteen wide column",
                QueueTableSection(p)
              )
            )
          )
        )
      )
    )
    .build

  def apply(ctl: RouterCtl[SeqexecPages]): Unmounted[RouterCtl[SeqexecPages], Unit, Unit] = component(ctl)

}
