package edu.gemini.seqexec.web.client.semanticui.elements.message

import edu.gemini.seqexec.web.client.semanticui.elements.icon.Icon
import edu.gemini.seqexec.web.client.semanticui.Size
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.Unmounted

/**
  * React component for a message with a large icon
  */
object IconMessage extends Message {

  private def component = ScalaComponent.builder[IconMessage.Props]("Message")
    .stateless
    .renderPC((_, p, c) =>
      <.div(
        ^.cls := "ui icon message",
        ^.classSet(
          "warning"  -> (p.style == IconMessage.Style.Warning),
          "info"     -> (p.style == IconMessage.Style.Info),
          "positive" -> (p.style == IconMessage.Style.Positive),
          "success"  -> (p.style == IconMessage.Style.Success),
          "negative" -> (p.style == IconMessage.Style.Negative),
          "error"    -> (p.style == IconMessage.Style.Error),
          "tiny"     -> (p.size == Size.Tiny),
          "mini"     -> (p.size == Size.Mini),
          "small"    -> (p.size == Size.Small),
          "large"    -> (p.size == Size.Large),
          "big"      -> (p.size == Size.Big),
          "huge"     -> (p.size == Size.Huge),
          "massive"  -> (p.size == Size.Massive)
        ),
        p.icon,
        <.div(
          ^.cls := "content",
          p.header.map(h =>
            <.div(
              ^.cls := "header",
              h
            )
          ).whenDefined,
          c
        )
      )
    )
    .build

  case class Props(icon: Icon, header: Option[String] = None, style: Style = Style.NotDefined, size: Size = Size.NotSized)

  def apply(p: Props, children: VdomNode*): Unmounted[Props, Unit, Unit] = component(p)(children: _*)
}
