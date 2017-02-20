package edu.gemini.seqexec.web.client.components.sequence

import diode.react.ModelProxy
import edu.gemini.seqexec.web.client.model._
import edu.gemini.seqexec.web.client.components.DropdownMenu
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.ext.KeyCode

/**
  * Display to show headers per sequence
  */
object HeadersSideBar {

  case class Props(model: ModelProxy[ClientStatus])

  case class State(observer: String)

  class Backend($: BackendScope[Props, State]) {

    def onEnter(e: ReactKeyboardEventI): Callback = CallbackOption.keyCodeSwitch(e) {
      case KeyCode.Enter => setObserver
    }

    def setObserver: Callback = $.props.zip($.state) >>= {
      case (p, s) => p.model.dispatchCB(SetObserver(s.observer))
    }

    def render(p: Props, s: State)  =
      <.div(
        ^.cls := "ui raised secondary segment",
        <.h4("Headers"),
        <.div(
          ^.cls := "ui form",
          <.div(

            <.label("Observer"),
            <.input(
              ^.`type` :="text",
              ^.autoComplete :="off",
              ^.value := s.observer
            )
          ),
          <.div(
            ^.cls := "required field",
            <.label("Operator"),
            <.input(
              ^.`type` :="text",
              ^.autoComplete :="off"
            )
          ),
          DropdownMenu(DropdownMenu.Props("Image Quality", "Select", List("IQ20", "IQ70", "IQ85", "Any"))),
          DropdownMenu(DropdownMenu.Props("Cloud Cover", "Select", List("CC20", "CC50", "CC70", "CC80", "CC90", "Any"))),
          DropdownMenu(DropdownMenu.Props("Water Vapor", "Select", List("WV20", "WV50", "WV80", "Any"))),
          DropdownMenu(DropdownMenu.Props("Sky Background", "Select", List("SB20", "SB50", "SB80", "Any")))
        )
      )
  }

  val component = ReactComponentB[Props]("HeadersSideBar")
    .initialState(State(""))
    .renderBackend[Backend]
    .build


  def apply(u: ModelProxy[ClientStatus]) = component(Props(u))
}
