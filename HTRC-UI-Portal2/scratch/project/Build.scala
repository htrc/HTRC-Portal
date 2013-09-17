import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "htrc-portal"
  val appVersion = "3.0.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies).settings()

}
