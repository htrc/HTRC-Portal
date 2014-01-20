import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "htrc-portal"
  val appVersion = "3.0.0-SNAPSHOT"

/* Add Dependencies in following format
*  "group_id" % "artifact_id" % "version"*/

    val appDependencies = Seq(
    "org.apache.amber" % "oauth2-client" % "0.22.1358727-wso2v2",
    "org.apache.amber" % "oauth2-common" % "0.22.1358727-wso2v2",
    "edu.indiana.d2i.htrc.oauth2" % "client-api" % "1.0.1",
    "org.apache.amber" % "oauth2-resourceserver" % "0.22.1358727-wso2v2",
    "com.googlecode.json-simple" % "json-simple" % "1.1",
      "mysql" % "mysql-connector-java" % "5.1.27",
    "org.wso2.carbon" % "org.wso2.carbon.identity.oauth.stub" % "4.0.3",
    "edu.illinois.i3.htrc" % "UserManager" % "0.3.1"
      exclude("org.apache.tomcat.ha.wso2","tomcat-ha")
      exclude("org.wso2.securevault","org.wso2.securevault")
      exclude("org.wso2.carbon","org.wso2.carbon.registry.extensions")
      exclude("org.apache.maven.scm.wso2","maven-scm")
      exclude("org.wso2.carbon","org.wso2.carbon.ui")
      exclude("ch.qos.logback","logback-classic")
      exclude("commons-fileupload.wso2","commons-fileupload")
      exclude("commons-codec.wso2","commons-codec")
      exclude("commons-collections.wso2","commons-collections")
      exclude("commons-pool.wso2","commons-pool")
      exclude("commons-beanutils.wso2","commons-beanutils")
      exclude("org.jboss.marshalling.wso2","marshalling")
      exclude("org.jboss.logging.wso2","jboss-logging")
      exclude("org.wso2.carbon","org.wso2.carbon.caching.core")
      exclude("org.wso2.carbon","org.wso2.carbon.ntask.core")
      exclude("org.slf4j","jcl-over-slf4j"),

    javaCore,
    javaJdbc,
    javaEbean
  )

/* Add repositories in following format
* "repository_name" at "repository path"*/

   val main = play.Project(appName, appVersion, appDependencies).settings(
//    resolvers += "WSO2 internal Repository" at "http://maven.wso2.org/nexus/content/groups/wso2-public/",
     resolvers += ("Local Maven Repository" at "file://"+ Path.userHome.absolutePath + "/.m2/repository"),
    resolvers += "Internal Release Repository" at "http://htrc.illinois.edu:8080/archiva/repository/internal/",
    resolvers += "Internal Snapshot Repository" at "http://htrc.illinois.edu:8080/archiva/repository/snapshots/"
  )

}
