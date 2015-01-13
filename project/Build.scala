import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "htrc-portal"
  val appVersion = "3.0.0-SNAPSHOT"

  val appDependencies = Seq(
    javaCore,
    javaJdbc,
    javaEbean,
    cache,
    "org.pac4j" % "play-pac4j_java" % "1.2.1.htrcv2"
   exclude("com.typesafe.play", "play-jdbc")
   exclude("com.typesafe.play", "play-cache"),
    "org.pac4j" % "pac4j-saml" % "1.5.1.htrcv2",
    "edu.indiana.d2i.htrc.security" % "useradmin-ext-stub" % "1.0.0-SNAPSHOT",
    "org.apache.amber.wso2" % "amber" % "0.22.1358727.wso2v3",
    "edu.indiana.d2i.htrc.oauth2" % "client-api" % "1.0.1",
    "org.apache.amber" % "oauth2-resourceserver" % "0.22.1358727-wso2v3",
    "com.googlecode.json-simple" % "json-simple" % "1.1",
    "mysql" % "mysql-connector-java" % "5.1.27",
    "org.wso2.carbon" % "org.wso2.carbon.identity.oauth.stub" % "4.2.2",
    "org.wso2.carbon" % "org.wso2.carbon.user.mgt.stub" % "4.2.0",
    "edu.vt.middleware" % "vt-password" % "3.1.2",
    "net.sourceforge.javacsv" % "javacsv" % "2.0",
    "org.wso2.carbon" % "org.wso2.carbon.registry.ws.client" % "4.2.0"
	exclude("org.eclipse.core","org.eclipse.core.runtime")
	exclude("uddi","uddi4j")
	exclude("org.apache.maven.scm", "maven-scm-api"),
    "org.wso2.carbon" % "org.wso2.carbon.registry.core" % "4.2.0" 
	exclude("org.eclipse.core","org.eclipse.core.runtime")
	exclude("uddi","uddi4j")
	exclude("org.apache.maven.scm", "maven-scm-api"),
    "org.wso2.carbon" % "org.wso2.carbon.registry.extensions.stub" % "4.2.0",
    "org.wso2.carbon" % "org.wso2.carbon.identity.authenticator.token.stub" % "4.2.0"
)

  /* Add repositories in following format
  * "repository_name" at "repository path"*/

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += ("WSO2 Maven Repository" at "http://maven.wso2.org/nexus/content/groups/wso2-public/"),
    resolvers += ("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"),
    resolvers += ("HTRC Nexus Repository" at "http://htrc.illinois.edu/nexus/content/groups/public/" ),
    resolvers += ("typesafe" at "http://repo.typesafe.com/typesafe/releases/"),
    resolvers += Resolver.mavenLocal
  )

}
