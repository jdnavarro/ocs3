package edu.gemini.seqexec.web.client.components

import diode.react.ModelProxy
import edu.gemini.seqexec.web.client.model.{ClientStatus, Logout, OpenLoginBox}
import edu.gemini.seqexec.web.client.semanticui.Size
import edu.gemini.seqexec.web.client.semanticui.elements.button.Button
import edu.gemini.seqexec.web.client.semanticui.elements.icon.Icon.IconSignOut
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._

import scalacss.ScalaCssReact._

/**
  * Menu at the top bar
  */
object TopMenu {

  case class Props(status: ModelProxy[ClientStatus])

  def openLogin[A](proxy: ModelProxy[A]) = japgolly.scalajs.react.Callback.log("Login")>>proxy.dispatchCB(OpenLoginBox)
  def logout[A](proxy: ModelProxy[A]) = proxy.dispatchCB(Logout)

  def loginButton[A](proxy: ModelProxy[A], enabled: Boolean) =
    Button(Button.Props(size = Size.Medium, onClick = openLogin(proxy), disabled = !enabled), "Login")

  def logoutButton[A](proxy: ModelProxy[A], text: String, enabled: Boolean) =
    Button(Button.Props(size = Size.Medium, onClick = logout(proxy), icon = Some(IconSignOut), disabled = !enabled), text)

  private val component = ScalaComponent.builder[Props]("SeqexecTopMenu")
    .stateless
    .render_P { p =>
      val status = p.status()
      <.div(
        ^.cls := "ui secondary right menu",
        SeqexecStyles.notInMobile,
        status.u.fold(
          <.div(
            ^.cls := "ui item",
            loginButton(p.status, status.isConnected)
          )
        )(u =>
          <.div(
            ^.cls := "ui secondary right menu",
            <.div(
              ^.cls := "ui header item",
              SeqexecStyles.notInMobile,
              u.displayName
            ),
            <.div(
              ^.cls := "ui header item",
              SeqexecStyles.onlyMobile,
              // Ideally we'd do this with css text-overflow but it is not
              // working properly inside a header item, let's abbreviate in code
              u.displayName.split("\\s").headOption.map(_.substring(0, 10) + "...").getOrElse[String]("")
            ),
            <.div(
              ^.cls := "ui item",
              SeqexecStyles.notInMobile,
              logoutButton(p.status, "Logout", status.isConnected)
            ),
            <.div(
              ^.cls := "ui item",
              SeqexecStyles.onlyMobile,
              logoutButton(p.status, "", status.isConnected)
            )
          )
        )
      )
    }
    .build

  def apply(u: ModelProxy[ClientStatus]): Unmounted[Props, Unit, Unit] = component(Props(u))
}
