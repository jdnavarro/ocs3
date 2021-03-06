package edu.gemini.seqexec.server

import java.util
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.{Timer, TimerTask}
import java.util.logging.Logger

import edu.gemini.seqexec.server.EpicsCommand._
import edu.gemini.seqexec.server.tcs.{BinaryOnOff, BinaryYesNo}

import scala.collection.JavaConverters._
import squants.Time
import edu.gemini.epics.acm._

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

/**
 * TcsEpics wraps the non-functional parts of the EPICS ACM library to interact with TCS. It has all the objects used
 * to read TCS status values and execute TCS commands.
 *
 * Created by jluhrs on 10/1/15.
 */

final class TcsEpics(epicsService: CaService, tops: Map[String, String]) {

  import TcsEpics._
  import EpicsCommand.setParameter

  val TCS_TOP = tops.getOrElse("tcs", "")

  // This is a bit ugly. Commands are triggered from the main apply record, so I just choose an arbitrary command here.
  // Triggering that command will trigger all the marked commands.
  def post: SeqAction[Unit] = m1GuideCmd.post

  object m1GuideCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("m1Guide"))
    val state = cs.map(_.getString("state"))

    def setState(v: String): SeqAction[Unit] = setParameter(state, v)
  }

  object m2GuideCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("m2Guide"))
    val state = cs.map(_.getString("state"))

    def setState(v: String): SeqAction[Unit] = setParameter[String](state, v)
  }

  object mountGuideCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("mountGuide"))

    val source = cs.map(_.getString("source"))

    def setSource(v: String): SeqAction[Unit] = setParameter(source, v)

    val p1weight = cs.map(_.getDouble("p1weight"))

    def setP1Weight(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](p1weight, v)

    val p2weight = cs.map(_.getDouble("p2weight"))

    def setP2Weight(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](p2weight, v)

    val mode = cs.map(_.getString("mode"))

    def setMode(v: String): SeqAction[Unit] = setParameter(mode, v)
  }

  object offsetACmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("offsetPoA1"))

    val x = cs.map(_.getDouble("x"))

    def setX(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](x, v)

    val y = cs.map(_.getDouble("y"))

    def setY(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](y, v)
  }

  object offsetBCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("offsetPoB1"))

    val x = cs.map(_.getDouble("x"))

    def setX(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](x, v)

    val y = cs.map(_.getDouble("y"))

    def setY(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](y, v)
  }

  object offsetCCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("offsetPoC1"))

    val x = cs.map(_.getDouble("x"))

    def setX(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](x, v)

    val y = cs.map(_.getDouble("y"))

    def setY(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](y, v)
  }

  object wavelSourceA extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("wavelSourceA"))

    val wavel = cs.map(_.getDouble("wavel"))

    def setWavel(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](wavel, v)
  }

  object wavelSourceB extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("wavelSourceB"))

    val wavel = cs.map(_.getDouble("wavel"))

    def setWavel(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](wavel, v)
  }

  object wavelSourceC extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("wavelSourceC"))

    val wavel = cs.map(_.getDouble("wavel"))

    def setWavel(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](wavel, v)
  }

  object m2Beam extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("m2Beam"))

    val beam = cs.map(_.getString("beam"))

    def setBeam(v: String): SeqAction[Unit] = setParameter(beam, v)
  }

  object pwfs1ProbeGuideCmd extends ProbeGuideCmd("pwfs1Guide", epicsService)

  object pwfs2ProbeGuideCmd extends ProbeGuideCmd("pwfs2Guide", epicsService)

  object oiwfsProbeGuideCmd extends ProbeGuideCmd("oiwfsGuide", epicsService)

  object pwfs1Park extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("pwfs1Park"))
  }

  object pwfs2Park extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("pwfs2Park"))
  }

  object oiwfsPark extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("oiwfsPark"))
  }

  object pwfs1StopObserveCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("pwfs1StopObserve"))
  }

  object pwfs2StopObserveCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("pwfs2StopObserve"))
  }

  object oiwfsStopObserveCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("oiwfsStopObserve"))
  }

  object pwfs1ObserveCmd extends WfsObserveCmd("pwfs1Observe", epicsService)

  object pwfs2ObserveCmd extends WfsObserveCmd("pwfs2Observe", epicsService)

  object oiwfsObserveCmd extends WfsObserveCmd("oiwfsObserve", epicsService)

  object hrwfsParkCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("hrwfsPark"))
  }

  object hrwfsPosCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("hrwfs"))

    val hrwfsPos = cs.map(_.getString("hrwfsPos"))

    def setHrwfsPos(v: String): SeqAction[Unit] = setParameter(hrwfsPos, v)
  }

  object scienceFoldParkCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("scienceFoldPark"))
  }

  object scienceFoldPosCmd extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender("scienceFold"))

    val scfold = cs.map(_.getString("scfold"))

    def setScfold(v: String): SeqAction[Unit] = setParameter(scfold, v)
  }

  val tcsState = epicsService.getStatusAcceptor("tcsstate")

  def absorbTipTilt: Option[Integer] = Option(tcsState.getIntegerAttribute("absorbTipTilt").value)

  def m1GuideSource: Option[String] = Option(tcsState.getStringAttribute("m1GuideSource").value)

  private val m1GuideAttr: Option[CaAttribute[BinaryOnOff]] = Option(tcsState.addEnum("m1Guide",
    TCS_TOP + "im:m1GuideOn", classOf[BinaryOnOff], "M1 guide"))

  def m1Guide: Option[BinaryOnOff] = m1GuideAttr.flatMap(v => Option(v.value))

  def m2p1Guide: Option[String] = Option(tcsState.getStringAttribute("m2p1Guide").value)

  def m2p2Guide: Option[String] = Option(tcsState.getStringAttribute("m2p2Guide").value)

  def m2oiGuide: Option[String] = Option(tcsState.getStringAttribute("m2oiGuide").value)

  def m2aoGuide: Option[String] = Option(tcsState.getStringAttribute("m2aoGuide").value)

  def comaCorrect: Option[String] = Option(tcsState.getStringAttribute("comaCorrect").value)

  private val m2GuideStateAttr: Option[CaAttribute[BinaryOnOff]] = Option(tcsState.addEnum("m2GuideState",
    TCS_TOP + "om:m2GuideState", classOf[BinaryOnOff], "M2 guiding state"))

  def m2GuideState: Option[BinaryOnOff] = m2GuideStateAttr.flatMap(v => Option(v.value))

  def xoffsetPoA1: Option[Double] = Option(tcsState.getDoubleAttribute("xoffsetPoA1").value).map(_.doubleValue)

  def yoffsetPoA1: Option[Double] = Option(tcsState.getDoubleAttribute("yoffsetPoA1").value).map(_.doubleValue)

  def xoffsetPoB1: Option[Double] = Option(tcsState.getDoubleAttribute("xoffsetPoB1").value).map(_.doubleValue)

  def yoffsetPoB1: Option[Double] = Option(tcsState.getDoubleAttribute("yoffsetPoB1").value).map(_.doubleValue)

  def xoffsetPoC1: Option[Double] = Option(tcsState.getDoubleAttribute("xoffsetPoC1").value).map(_.doubleValue)

  def yoffsetPoC1: Option[Double] = Option(tcsState.getDoubleAttribute("yoffsetPoC1").value).map(_.doubleValue)

  def sourceAWavelength: Option[Double] = Option(tcsState.getDoubleAttribute("sourceAWavelength").value).map(_.doubleValue)

  def sourceBWavelength: Option[Double] = Option(tcsState.getDoubleAttribute("sourceBWavelength").value).map(_.doubleValue)

  def sourceCWavelength: Option[Double] = Option(tcsState.getDoubleAttribute("sourceCWavelength").value).map(_.doubleValue)

  def chopBeam: Option[String] = Option(tcsState.getStringAttribute("chopBeam").value)

  def p1FollowS: Option[String] = Option(tcsState.getStringAttribute("p1FollowS").value)

  def p2FollowS: Option[String] = Option(tcsState.getStringAttribute("p2FollowS").value)

  def oiFollowS: Option[String] = Option(tcsState.getStringAttribute("oiFollowS").value)

  private val pwfs1OnAttr: Option[CaAttribute[BinaryYesNo]] = Option(tcsState.addEnum("pwfs1On",
    TCS_TOP + "drives:p1Integrating", classOf[BinaryYesNo], "P1 integrating"))

  def pwfs1On: Option[BinaryYesNo] = pwfs1OnAttr.flatMap(v => Option(v.value))

  private val pwfs2OnAttr: Option[CaAttribute[BinaryYesNo]] = Option(tcsState.addEnum("pwfs2On",
    TCS_TOP + "drives:p2Integrating", classOf[BinaryYesNo], "P2 integrating"))

  def pwfs2On: Option[BinaryYesNo] = pwfs2OnAttr.flatMap(v => Option(v.value))

  private val oiwfsOnAttr: Option[CaAttribute[BinaryYesNo]] = Option(tcsState.addEnum("oiwfsOn",
    TCS_TOP + "drives:oiIntegrating", classOf[BinaryYesNo], "P2 integrating"))

  def oiwfsOn: Option[BinaryYesNo] = oiwfsOnAttr.flatMap(v => Option(v.value))

  def sfName: Option[String] = Option(tcsState.getStringAttribute("sfName").value)

  def sfParked: Option[Integer] = Option(tcsState.getIntegerAttribute("sfParked").value)

  def agHwName: Option[String] = Option(tcsState.getStringAttribute("agHwName").value)

  def agHwParked: Option[Integer] = Option(tcsState.getIntegerAttribute("agHwParked").value)

  def instrAA: Option[Double] = Option(tcsState.getDoubleAttribute("instrAA").value).map(_.doubleValue)

  private val inPositionAttr: CaAttribute[String] = tcsState.getStringAttribute("inPosition")

  def inPosition: Option[String] = Option(inPositionAttr.value)

  private val agInPositionAttr: CaAttribute[java.lang.Double] = tcsState.getDoubleAttribute("agInPosition")
  def agInPosition: Option[Double] = Option(agInPositionAttr.value).map(_.doubleValue)

  object pwfs1ProbeGuideConfig extends ProbeGuideConfig("p1", tcsState)

  object pwfs2ProbeGuideConfig extends ProbeGuideConfig("p2", tcsState)

  object oiwfsProbeGuideConfig extends ProbeGuideConfig("oi", tcsState)

  // This functions returns a Task that, when run, will wait up to `timeout` seconds for the TCS in-position flag to
  // set to TRUE
  def waitInPosition(timeout: Time): SeqAction[Unit] =
    EpicsCommand.safe(EitherT(Task.async[TrySeq[Unit]]((f) => {
      //The task is created with Task.async. So we do whatever we need to do, adn then call `f` to signal the completion
      // of the task.

      //`resultGuard` and `lock` are used for synchronization.
      val resultGuard = new AtomicInteger(1)
      val lock = new ReentrantLock()

      //`locked` gets a piece of code and runs it protected by `lock`
      def locked(f: => Unit): Unit = {
        lock.lock()
        try {
          f
        } finally {
          lock.unlock()
        }
      }

      // First we verify that the in-position flag is not already active.
      if (!inPositionAttr.values().isEmpty && inPositionAttr.value == "TRUE") {
        f(TrySeq(()).right)
      } else {

        // If not, we set a timer for the timeout, and a listener for the EPICS channel corresponding to the TCS
        // in-position flag.
        // The timer and the listener can both complete the Task. The first one to do it cancels the other. The use of
        // `resultGuard` guarantees that only one of them will complete the Task.
        val timer = new Timer
        val statusListener = new CaAttributeListener[String] {

          override def onValueChange(newVals: util.List[String]): Unit = {
            if (!newVals.isEmpty && newVals.get(0) == "TRUE" && resultGuard.getAndDecrement() == 1) {
              locked {
                inPositionAttr.removeListener(this)
                timer.cancel()
              }

              // This `right` looks a bit confusing because is not related to the `TrySeq`, but to the result of `Task`.
              f(TrySeq(()).right)
            }
          }

          override def onValidityChange(newValidity: Boolean): Unit = {}
        }

        locked {
          if (timeout.toMilliseconds.toLong > 0) {
            timer.schedule(new TimerTask {
              override def run(): Unit = if (resultGuard.getAndDecrement() == 1) {
                locked {
                  inPositionAttr.removeListener(statusListener)
                }

                f(TrySeq.fail(SeqexecFailure.Timeout("waiting for TCS inposition flag.")).right)

              }
            }, timeout.toMilliseconds.toLong)
          }
          inPositionAttr.addListener(statusListener)
        }
      }
    })))

  // `waitAGInPosition` works like `waitInPosition`, but for the AG in-position flag.
  def waitAGInPosition(timeout: Time): SeqAction[Unit] =
    EpicsCommand.safe(EitherT(Task.async[TrySeq[Unit]]((f) => {

      val resultGuard = new AtomicInteger(1)
      val lock = new ReentrantLock()
      def locked(f: => Unit): Unit = {
        lock.lock()
        try {
          f
        } finally {
          lock.unlock()
        }
      }

      if (!agInPositionAttr.values.isEmpty && agInPositionAttr.value == 1.0) {
        f(TrySeq(()).right)
      } else {

        val timer = new Timer
        val statusListener = new CaAttributeListener[java.lang.Double] {

          override def onValueChange(newVals: util.List[java.lang.Double]): Unit = {
            if (!newVals.isEmpty && newVals.get(0) == 1.0 && resultGuard.getAndDecrement() == 1) {
              locked {
                agInPositionAttr.removeListener(this)
                timer.cancel()
              }

              f(TrySeq(()).right)
            }
          }

          override def onValidityChange(newValidity: Boolean): Unit = {}
        }

        locked {
          if (timeout.toMilliseconds.toLong > 0) {
            timer.schedule(new TimerTask {
              override def run(): Unit = if (resultGuard.getAndDecrement() == 1) {
                locked {
                  agInPositionAttr.removeListener(statusListener)
                }

                f(TrySeq.fail(SeqexecFailure.Timeout("waiting for AG inposition flag.")).right)

              }
            }, timeout.toMilliseconds.toLong)
          }
          agInPositionAttr.addListener(statusListener)
        }
      }
    })))

  def hourAngle: Option[String] = Option(tcsState.getStringAttribute("ha").value)

  def localTime: Option[String] = Option(tcsState.getStringAttribute("lt").value)

  def trackingFrame: Option[String] = Option(tcsState.getStringAttribute("trkframe").value)

  def trackingEpoch: Option[Double] = Option(tcsState.getDoubleAttribute("trkepoch").value).map(_.doubleValue)

  def equinox: Option[Double] = Option(tcsState.getDoubleAttribute("sourceAEquinox").value).map(_.doubleValue)

  def trackingEquinox: Option[String] = Option(tcsState.getStringAttribute("sourceATrackEq").value)

  def trackingDec: Option[Double] = Option(tcsState.getDoubleAttribute("dectrack").value).map(_.doubleValue)

  def trackingRA: Option[Double] = Option(tcsState.getDoubleAttribute("ratrack").value).map(_.doubleValue)

  def elevation: Option[Double] = Option(tcsState.getDoubleAttribute("elevatio").value).map(_.doubleValue)

  def azimuth: Option[Double] = Option(tcsState.getDoubleAttribute("azimuth").value).map(_.doubleValue)

  def crPositionAngle: Option[Double] = Option(tcsState.getDoubleAttribute("crpa").value).map(_.doubleValue)

  def ut: Option[String] = Option(tcsState.getStringAttribute("ut").value)

  def date: Option[String] = Option(tcsState.getStringAttribute("date").value)

  def m2Baffle: Option[String] = Option(tcsState.getStringAttribute("m2baffle").value)

  def m2CentralBaffle: Option[String] = Option(tcsState.getStringAttribute("m2cenbaff").value)

  def st: Option[String] = Option(tcsState.getStringAttribute("st").value)

  def sfRotation: Option[Double] = Option(tcsState.getDoubleAttribute("sfrt2").value).map(_.doubleValue)

  def sfTilt: Option[Double] = Option(tcsState.getDoubleAttribute("sftilt").value).map(_.doubleValue)

  def sfLinear: Option[Double] = Option(tcsState.getDoubleAttribute("sflinear").value).map(_.doubleValue)

  def instrPA: Option[Double] = Option(tcsState.getDoubleAttribute("instrPA").value).map(_.doubleValue)

  def targetA: Option[List[Double]] = Option(tcsState.getDoubleAttribute("targetA").values).map(_.asScala.toList.map(_.doubleValue))

  def aoFoldPosition: Option[String] = Option(tcsState.getStringAttribute("aoName").value)

  def airmass: Option[Double] = Option(tcsState.getDoubleAttribute("airmass").value).map(_.doubleValue)

  def airmassStart: Option[Double] = Option(tcsState.getDoubleAttribute("amstart").value).map(_.doubleValue)

  def airmassEnd: Option[Double] = Option(tcsState.getDoubleAttribute("amend").value).map(_.doubleValue)

  def carouselMode: Option[String] = Option(tcsState.getStringAttribute("cguidmod").value)

  def sourceATarget: Target = new Target {
    override def epoch = Option(tcsState.getStringAttribute("sourceAEpoch").value)

    override def equinox = Option(tcsState.getStringAttribute("sourceAEquinox").value)

    override def radialVelocity = Option(tcsState.getDoubleAttribute("radvel").value).map(_.doubleValue)

    override def frame = Option(tcsState.getStringAttribute("frame").value)

    override def centralWavelenght = sourceAWavelength

    override def ra = Option(tcsState.getDoubleAttribute("ra").value).map(_.doubleValue)

    override def objectName = Option(tcsState.getStringAttribute("sourceAObjectName").value)

    override def dec = Option(tcsState.getDoubleAttribute("dec").value).map(_.doubleValue)

    override def parallax = Option(tcsState.getDoubleAttribute("parallax").value).map(_.doubleValue)

    override def properMotionRA = Option(tcsState.getDoubleAttribute("pmra").value).map(_.doubleValue)

    override def properMotionDec = Option(tcsState.getDoubleAttribute("pmdec").value).map(_.doubleValue)
  }

  private def target(base: String): Target = new Target {
      override def epoch = Option(tcsState.getStringAttribute(base + "aepoch").value)
      override def equinox = Option(tcsState.getStringAttribute(base + "aequin").value)
      override def radialVelocity = Option(tcsState.getDoubleAttribute(base + "arv").value).map(_.doubleValue)
      override def frame  = Option(tcsState.getStringAttribute(base + "aframe").value)
      override def centralWavelenght = Option(tcsState.getDoubleAttribute(base + "awavel").value).map(_.doubleValue)
      override def ra = Option(tcsState.getDoubleAttribute(base + "ara").value).map(_.doubleValue)
      override def objectName = Option(tcsState.getStringAttribute(base + "aobjec").value)
      override def dec = Option(tcsState.getDoubleAttribute(base + "adec").value).map(_.doubleValue)
      override def parallax = Option(tcsState.getDoubleAttribute(base + "aparal").value).map(_.doubleValue)
      override def properMotionRA = Option(tcsState.getDoubleAttribute(base + "apmra").value).map(_.doubleValue)
      override def properMotionDec = Option(tcsState.getDoubleAttribute(base + "apmdec").value).map(_.doubleValue)
    }

  def pwfs1Target: Target = target("p1")

  def pwfs2Target: Target = target("p2")

  def oiwfsTarget: Target = target("oi")

  def gwfs1Target: Target = target("g1")

  def gwfs2Target: Target = target("g2")

  def gwfs3Target: Target = target("g3")

  def gwfs4Target: Target = target("g4")

  def m2UserFocusOffset = Option(tcsState.getDoubleAttribute("m2ZUserOffset").value).map(_.doubleValue)

  val pwfs1Status = epicsService.getStatusAcceptor("pwfs1state")

  def pwfs1IntegrationTime = Option(pwfs1Status.getDoubleAttribute("intTime").value).map(_.doubleValue)

  val pwfs2Status = epicsService.getStatusAcceptor("pwfs2state")

  def pwfs2IntegrationTime = Option(pwfs2Status.getDoubleAttribute("intTime").value).map(_.doubleValue)

  val oiwfsStatus = epicsService.getStatusAcceptor("oiwfsstate")

  // Attribute must be changed back to Double after EPICS channel is fixed.
  def oiwfsIntegrationTime: Option[Double]  = Option(oiwfsStatus.getDoubleAttribute("intTime").value).map(_.doubleValue)

  private def instPort(name: String): Option[Int] = Option(tcsState.getIntegerAttribute(s"${name}Port").value).map(_.intValue)
  def gsaoiPort = instPort("gsaoi")
  def gpiPort = instPort("gpi")
  def f2Port = instPort("f2")
  def niriPort = instPort("niri")
  def gnirsPort = instPort("nirs")
  def nifsPort = instPort("nifs")
  def gmosPort = instPort("gmos")
}

object TcsEpics extends EpicsSystem[TcsEpics] {

  override val className = getClass.getName
  override val Log = Logger.getLogger(className)
  override val CA_CONFIG_FILE = "/Tcs.xml"

  override def build(service: CaService, tops: Map[String, String]) = new TcsEpics(service, tops)

  sealed class ProbeGuideCmd(csName: String, epicsService: CaService) extends EpicsCommand {
    override val cs = Option(epicsService.getCommandSender(csName))

    val nodachopa = cs.map(_.getString("nodachopa"))
    def setNodachopa(v: String): SeqAction[Unit] = setParameter(nodachopa, v)

    val nodachopb = cs.map(_.getString("nodachopb"))
    def setNodachopb(v: String): SeqAction[Unit] = setParameter(nodachopb, v)

    val nodachopc = cs.map(_.getString("nodachopc"))
    def setNodachopc(v: String): SeqAction[Unit] = setParameter(nodachopc, v)

    val nodbchopa = cs.map(_.getString("nodbchopa"))
    def setNodbchopa(v: String): SeqAction[Unit] = setParameter(nodbchopa, v)

    val nodbchopb = cs.map(_.getString("nodbchopb"))
    def setNodbchopb(v: String): SeqAction[Unit] = setParameter(nodbchopb, v)

    val nodbchopc = cs.map(_.getString("nodbchopc"))
    def setNodbchopc(v: String): SeqAction[Unit] = setParameter(nodbchopc, v)

    val nodcchopa = cs.map(_.getString("nodcchopa"))
    def setNodcchopa(v: String): SeqAction[Unit] = setParameter(nodcchopa, v)

    val nodcchopb = cs.map(_.getString("nodcchopb"))
    def setNodcchopb(v: String): SeqAction[Unit] = setParameter(nodcchopb, v)

    val nodcchopc = cs.map(_.getString("nodcchopc"))
    def setNodcchopc(v: String): SeqAction[Unit] = setParameter(nodcchopc, v)
  }

  sealed class WfsObserveCmd(csName: String, epicsService: CaService) extends EpicsCommand {
    override val cs= Option(epicsService.getCommandSender(csName))

    val noexp = cs.map(_.getInteger("noexp"))
    def setNoexp(v: Integer): SeqAction[Unit] = setParameter(noexp, v)

    val int = cs.map(_.getDouble("int"))
    def setInt(v: Double): SeqAction[Unit] = setParameter[java.lang.Double](int, v)

    val outopt = cs.map(_.getString("outopt"))
    def setOutopt(v: String): SeqAction[Unit] = setParameter(outopt, v)

    val label = cs.map(_.getString("label"))
    def setLabel(v: String): SeqAction[Unit] = setParameter(label, v)

    val output = cs.map(_.getString("output"))
    def setOutput(v: String): SeqAction[Unit] = setParameter(output, v)

    val path = cs.map(_.getString("path"))
    def setPath(v: String): SeqAction[Unit] = setParameter(path, v)

    val name = cs.map(_.getString("name"))
    def setName(v: String): SeqAction[Unit] = setParameter(name, v)
  }

  class ProbeGuideConfig(protected val prefix: String, protected val tcsState: CaStatusAcceptor) {
    def nodachopa: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodachopa").value)
    def nodachopb: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodachopb").value)
    def nodachopc: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodachopc").value)
    def nodbchopa: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodbchopa").value)
    def nodbchopb: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodbchopb").value)
    def nodbchopc: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodbchopc").value)
    def nodcchopa: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodcchopa").value)
    def nodcchopb: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodcchopb").value)
    def nodcchopc: Option[String] = Option(tcsState.getStringAttribute(prefix+"nodcchopc").value)
  }

  sealed trait Target {
    def objectName: Option[String]
    def ra: Option[Double]
    def dec: Option[Double]
    def frame: Option[String]
    def equinox: Option[String]
    def epoch: Option[String]
    def properMotionRA: Option[Double]
    def properMotionDec: Option[Double]
    def centralWavelenght: Option[Double]
    def parallax: Option[Double]
    def radialVelocity: Option[Double]
  }

}
