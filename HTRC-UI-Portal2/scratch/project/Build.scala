import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "htrc-portal"
  val appVersion = "3.0.0-SNAPSHOT"


  val appDependencies = Seq(
    ("org.apache.amber" % "oauth2-client" % "0.22.1358727-wso2v2"),
    ("org.apache.amber" % "oauth2-common" % "0.22.1358727-wso2v2"),
    ("edu.indiana.d2i.htrc.oauth2" % "client-api" % "1.0.1"),
    ("org.apache.amber" % "oauth2-resourceserver" % "0.22.1358727-wso2v2"),
    javaCore,
    javaJdbc,
    javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "WSO2 internal Repository" at "http://maven.wso2.org/nexus/content/groups/wso2-public/",
    resolvers += "Internal Release Repository" at "http://htrc.illinois.edu:8080/archiva/repository/internal/",
    resolvers += "Internal Snapshot Repository" at "http://htrc.illinois.edu:8080/archiva/repository/snapshots/"
  )

}
