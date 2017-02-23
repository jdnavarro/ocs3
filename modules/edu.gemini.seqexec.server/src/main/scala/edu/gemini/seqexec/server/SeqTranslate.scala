package edu.gemini.seqexec.server

import edu.gemini.model.p1.immutable.Site
import edu.gemini.seqexec.engine.{Action, Result, Sequence, Step}
import edu.gemini.seqexec.model.Model.{SequenceMetadata, Operator, Observer}
import edu.gemini.model.p1.immutable.Site
import edu.gemini.seqexec.server.SeqexecFailure.UnrecognizedInstrument
import edu.gemini.seqexec.server.ConfigUtilOps._
import edu.gemini.seqexec.server.SeqexecFailure.UnrecognizedInstrument
import edu.gemini.spModel.config2.{Config, ConfigSequence, ItemKey}
import edu.gemini.spModel.obscomp.InstConstants._
import edu.gemini.spModel.seqcomp.SeqConfigNames._

import scalaz.Scalaz._
import scalaz._

/**
  * Created by jluhrs on 9/14/16.
  */
class SeqTranslate(site: Site) {

  import SeqTranslate._

  implicit def toAction(x: SeqAction[Result.Response]): Action = x.run map {
    case -\/(e) => Result.Error(SeqexecFailure.explain(e))
    case \/-(r) => Result.OK(r)
  }

  private def step(systems: Systems, settings: Settings)(i: Int, config: Config): SeqexecFailure \/ Step[Action] = {

    def buildStep(inst: Instrument, instHeaders: List[Header]): Step[Action] = {
      val sys = List(Tcs(systems.tcs), inst)
      val headers = List(new StandardHeader(systems.dhs,
        ObsKeywordReaderImpl(config, site.name.replace(' ', '-')),
        if (settings.tcsKeywords) TcsKeywordsReaderImpl else DummyTcsKeywordsReader
      )) ++ instHeaders

      Step[Action](
        i,
        None,
        config.toStepConfig,
        false,
        List(
          sys.map(x => toAction(x.configure(config).map(y => Result.Configured(y.sys.name)))),
          List(toAction(inst.observe(config)((systems.dhs, headers)).map(x => Result.Observed(x.dataId))))
        )
      )
    }

    val instName = extractInstrumentName(config)

    instName match {
      case Flamingos2.name => buildStep(Flamingos2(systems.flamingos2), List(
        Flamingos2Header(systems.dhs, new Flamingos2Header.ObsKeywordsReaderImpl(config),
          if(settings.f2Keywords) Flamingos2Header.InstKeywordReaderImpl else Flamingos2Header.DummyInstKeywordReader,
          if (settings.tcsKeywords) TcsKeywordsReaderImpl else DummyTcsKeywordsReader
        ))
      ).right
      case _               => UnrecognizedInstrument(instName.toString).left[Step[Action]]
    }

  }

  private def extractInstrumentName(config: Config): String =
    // This is too weak. We may want to use the extractors used in ITC
    config.getItemValue(new ItemKey(INSTRUMENT_KEY, INSTRUMENT_NAME_PROP)).toString

  def sequence(systems: Systems, settings: Settings)(
    obsId: String,
    operator: Operator,
    observer: Observer,
    sequenceConfig: ConfigSequence): SeqexecFailure \/ Sequence[Action] = {
    val configs = sequenceConfig.getAllSteps.toList

    val steps = configs.zipWithIndex.traverseU {
      case (c, i) => step(systems, settings)(i, c)
    }

    val instName = configs.headOption.map(extractInstrumentName).getOrElse("Unknown instrument")

    steps.map(Sequence[Action](obsId, SequenceMetadata(instName, operator, observer), _))
  }

}

object SeqTranslate {
  def apply(site: Site) = new SeqTranslate(site)

  case class Systems(
                      dhs: DhsClient,
                      tcs: TcsController,
                      flamingos2: Flamingos2Controller
                    )

  case class Settings(
                       tcsKeywords: Boolean,
                       f2Keywords: Boolean
                     )

}
