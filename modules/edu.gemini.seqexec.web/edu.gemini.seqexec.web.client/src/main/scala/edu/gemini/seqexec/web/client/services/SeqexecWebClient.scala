package edu.gemini.seqexec.web.client.services

import java.util.logging.LogRecord

import edu.gemini.seqexec.model.{ModelBooPicklers, UserDetails, UserLoginRequest}
import edu.gemini.seqexec.model.Model.{Conditions, ImageQuality}
import edu.gemini.seqexec.web.common._
import edu.gemini.seqexec.web.common.LogMessage._
import org.scalajs.dom.ext.{Ajax, AjaxException}
import boopickle.Default._
import edu.gemini.seqexec.model.Model.{SequenceId, SequencesQueue, SequenceView, Step}
import org.scalajs.dom.XMLHttpRequest

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}

/**
  * Encapsulates remote calls to the Seqexec Web API
  */
object SeqexecWebClient extends ModelBooPicklers {
  val baseUrl = "/api/seqexec"

  // Decodes the binary response with BooPickle, errors are not handled
  def unpickle[A](r: XMLHttpRequest)(implicit u: Pickler[A]): A = {
    val ab = TypedArrayBuffer.wrap(r.response.asInstanceOf[ArrayBuffer])
    Unpickle[A].fromBytes(ab)
  }

  def read(id: String): Future[SequencesQueue[SequenceId]] =
    Ajax.get(
      url = s"$baseUrl/sequence/$id",
      responseType = "arraybuffer"
    )
    .map(unpickle[SequencesQueue[SequenceId]])
    .recover {
      case AjaxException(xhr) if xhr.status == HttpStatusCodes.NotFound  =>
        // If not found, we'll consider it like an empty response
        SequencesQueue(Conditions.default, Nil)
    }

  /**
    * Requests the backend to execute a sequence
    */
  def run(s: SequenceView): Future[RegularCommand] = {
    Ajax.post(
      url = s"$baseUrl/commands/${s.id}/start",
      responseType = "arraybuffer"
    ).map(unpickle[RegularCommand])
  }

  /**
    * Requests the backend to set a breakpoint
    */
  def breakpoint(s: SequenceView, step: Step): Future[RegularCommand] = {
    Ajax.post(
      url = s"$baseUrl/commands/${s.id}/${step.id}/breakpoint/true",
      responseType = "arraybuffer"
    ).map(unpickle[RegularCommand])
  }

  /**
    * Requests the backend to set the operator name of a sequence
    */
  def setOperator(name: String): Future[RegularCommand] = {
    Ajax.post(
      url = s"$baseUrl/commands/operator/${name}",
      responseType = "arraybuffer"
    ).map(unpickle[RegularCommand])
  }

  /**
    * Requests the backend to set the observer name of a sequence
    */
  def setObserver(s: SequenceView, name: String): Future[RegularCommand] = {
    Ajax.post(
      url = s"$baseUrl/commands/${s.id}/observer/${name}",
      responseType = "arraybuffer"
    ).map(unpickle[RegularCommand])
  }

  /**
    * Requests the backend to set the Conditions globally
    */
  def setConditions(conditions: Conditions): Future[RegularCommand] = {
    Ajax.post(
      url = s"$baseUrl/commands/conditions",
      responseType = "arraybuffer",
      data = Pickle.intoBytes(conditions)
    ).map(unpickle[RegularCommand])
  }

  /**
    * Requests the backend to set the ImageQuality globally
    */
  def setImageQuality(iq: ImageQuality): Future[RegularCommand] = {
    Ajax.post(
      url = s"$baseUrl/commands/iq",
      responseType = "arraybuffer",
      data = Pickle.intoBytes[ImageQuality](iq)
    ).map(unpickle[RegularCommand])
  }

  /**
    * Requests the backend to send a copy of the current state
    */
  def refresh(): Future[RegularCommand] = {
    Ajax.get(
      url = s"$baseUrl/commands/refresh",
      responseType = "arraybuffer"
    ).map(unpickle[RegularCommand])
  }

  /**
    * Requests the backend to stop a sequence
    */
  def stop(s: SequenceView): Future[RegularCommand] = {
    Ajax.post(
      url = s"$baseUrl/commands/${s.id}/pause",
      responseType = "arraybuffer"
    ).map(unpickle[RegularCommand])
  }

  /**
    * Login request
    */
  def login(u: String, p: String): Future[UserDetails] =
    Ajax.post(
      url = s"$baseUrl/login",
      responseType = "arraybuffer",
      data = Pickle.intoBytes(UserLoginRequest(u, p))
    ).map(unpickle[UserDetails])

  /**
    * Logout request
    */
  def logout(): Future[String] =
    Ajax.post(
      url = s"$baseUrl/logout"
    ).map(_.responseText)

  /**
    * Log record
    */
  def log(record: LogRecord): Future[Unit] =
    Ajax.post(
      url = s"$baseUrl/log",
      responseType = "arraybuffer",
      data = Pickle.intoBytes(LogMessage.fromLogRecord(record))
    ).map(_.responseText)
}
