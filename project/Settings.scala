import sbt._

/**
 * Application settings and dependencies
 */
object Settings {
  object Definitions {
    /** The name of the application */
    val name = "ocs3"

    /** Project version */
    val version = "2016001.1.1"

    /** Options for the scala compiler */
    val scalacOptions = Seq(
      "-unchecked",
      "-deprecation",
      "-encoding", "UTF-8", // Keep on the same line
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-target:jvm-1.8",
      "-Xlint",
      "-Xlint:-stars-align"
    )
  }

  /** Library versions */
  object LibraryVersions {
    val scala        = "2.11.8"
    val scalaDom     = "0.9.0"
    val scalajsReact = "0.10.4"
    val scalaCSS     = "0.4.0"
    val scalaZ       = "7.1.6"
    val scalaZJS     = "7.2.1"

    // test libraries
    val scalaTest    = "3.0.0-M15"
    val scalaCheck   = "1.12.5"

    val ocsVersion   = "2016001.1.1"
  }

  /**
    * Global libraries
    */
  object Libraries {
    import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

    // Test Libraries
    val TestLibs = Def.setting(Seq(
      "org.scalatest"  %%% "scalatest"   % LibraryVersions.scalaTest  % "test",
      "org.scalacheck" %%% "scalacheck"  % LibraryVersions.scalaCheck % "test"
    ))

    // OCS Libraries, these should become modules in the future
    val SpModelCore = "edu.gemini.ocs"     %% "edu-gemini-spmodel-core" % LibraryVersions.ocsVersion
    val POT         = "edu.gemini.ocs"     %% "edu-gemini-pot"          % LibraryVersions.ocsVersion
    val EpicsACM    = "edu.gemini.ocs"     %% "edu-gemini-epics-acm"    % LibraryVersions.ocsVersion
    val TRPC        = "edu.gemini.ocs"     %% "edu-gemini-util-trpc"    % LibraryVersions.ocsVersion
  }

  /**
    * Global libraries only for JVM
    */
  object LibrariesJVM {
    // ScalaZ
    val ScalaZCore       = "org.scalaz" %% "scalaz-core"       % LibraryVersions.scalaZ
    val ScalaZConcurrent = "org.scalaz" %% "scalaz-concurrent" % LibraryVersions.scalaZ
  }

  /**
    * Global libraries only for JS
    */
  object LibrariesJS {
    import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

    // TODO Unify with the JVM version
    val ScalaZCoreJS = Def.setting("org.scalaz" %%% "scalaz-core" % "7.2.1")
  }

}
