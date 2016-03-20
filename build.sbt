import play.Project._

playJavaSettings

showCurrentGitBranch

git.useGitDescribe := true

lazy val commonSettings = Seq(
  organization := "edu.indiana.d2i.htrc",
  organizationName := "HathiTrust Research Center",
  organizationHomepage := Some(url("https://www.hathitrust.org/htrc")),
  resolvers ++= Seq(
    "I3 Repository" at "http://nexus.htrc.illinois.edu/content/groups/public",
    Resolver.mavenLocal
  ),
  buildInfoOptions ++= Seq(BuildInfoOption.BuildTime),
  buildInfoKeys ++= Seq[BuildInfoKey](
    "gitSha" -> git.gitHeadCommit.value.getOrElse("N/A"),
    "gitBranch" -> git.gitCurrentBranch.value,
    "gitVersion" -> git.gitDescribedVersion.value.getOrElse("N/A"),
    "gitDirty" -> git.gitUncommittedChanges.value
  ),
  packageOptions in(Compile, packageBin) += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  )
)

lazy val `htrc-portal` = (project in file(".")).
  enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt).
  settings(commonSettings: _*).
  settings(
    name := "htrc-portal",
    description := "The HTRC Portal is the main gateway for users to explore the HathiTrust collection algorithmically",
    homepage := Some(url("https://sharc.hathitrust.org")),
    startYear := Some(2011),
    licenses += "Apache2" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
    libraryDependencies ++= Seq(
      javaCore,
      javaJdbc,
      javaEbean,
      cache,
      "org.pac4j" % "play-pac4j_java" % "1.2.1.htrcv5"
        exclude("com.typesafe.play", "play-jdbc")
        exclude("com.typesafe.play", "play-cache"),
      "org.pac4j" % "pac4j-saml" % "1.5.1.htrcv2",
      "edu.indiana.d2i.htrc" % "useradmin-ext-stub" % "1.0.1",
      "org.apache.amber.wso2" % "amber" % "0.22.1358727.wso2v3",
      "edu.indiana.d2i.htrc.oauth2" % "client-api" % "1.0.1",
      "org.apache.amber" % "oauth2-resourceserver" % "0.22.1358727-wso2v3",
      "com.googlecode.json-simple" % "json-simple" % "1.1",
      "mysql" % "mysql-connector-java" % "5.1.27",
      "org.wso2.carbon" % "org.wso2.carbon.identity.oauth.stub" % "4.2.2",
      "org.wso2.carbon" % "org.wso2.carbon.user.mgt.stub" % "4.2.0",
      "org.wso2.carbon" % "org.wso2.carbon.identity.application.mgt.stub" % "4.2.0",
      "org.wso2.carbon" % "org.wso2.carbon.identity.sso.saml.stub" % "4.2.2",
      "org.wso2.carbon" % "org.wso2.carbon.authenticator.stub" % "4.0.0",
      "org.wso2.carbon" % "org.wso2.carbon.utils" % "4.2.0",
      "edu.vt.middleware" % "vt-password" % "3.1.2",
      "net.sourceforge.javacsv" % "javacsv" % "2.0",
      "org.markdownj" % "markdownj-core" % "0.4",
      "org.wso2.carbon" % "org.wso2.carbon.registry.ws.client" % "4.2.0"
        exclude("org.eclipse.core", "org.eclipse.core.runtime")
        exclude("uddi", "uddi4j")
        exclude("org.wso2.carbon", "org.wso2.carbon.registry.uddi")
        exclude("org.apache.juddi.wso2", "juddi")
        exclude("org.apache.maven.scm", "maven-scm-api")
        exclude("org.wso2.carbon", "org.wso2.carbon.feature.mgt.core")
        exclude("org.wso2.carbon", "org.wso2.carbon.feature.mgt.services")
        exclude("org.wso2.carbon", "org.wso2.carbon.ndatasource.common")
        exclude("org.wso2.carbon", "org.wso2.carbon.ndatasource.rdbms")
        exclude("org.wso2.carbon", "org.wso2.carbon.ntask.common")
        exclude("org.wso2.carbon", "org.wso2.carbon.ntask.core"),
      "org.wso2.carbon" % "org.wso2.carbon.registry.core" % "4.2.0"
        exclude("org.eclipse.core", "org.eclipse.core.runtime")
        exclude("uddi", "uddi4j")
        exclude("org.apache.maven.scm", "maven-scm-api")
        exclude("org.wso2.carbon", "org.wso2.carbon.registry.uddi")
        exclude("org.wso2.carbon", "org.wso2.carbon.feature.mgt.core")
        exclude("org.wso2.carbon", "org.wso2.carbon.feature.mgt.services")
        exclude("org.wso2.carbon", "org.wso2.carbon.ndatasource.common")
        exclude("org.wso2.carbon", "org.wso2.carbon.ndatasource.rdbms")
        exclude("org.wso2.carbon", "org.wso2.carbon.ntask.common")
        exclude("org.wso2.carbon", "org.wso2.carbon.ntask.core"),
      "org.wso2.carbon" % "org.wso2.carbon.registry.extensions.stub" % "4.2.0",
      "org.wso2.carbon" % "org.wso2.carbon.identity.authenticator.token.stub" % "4.2.0",
      "org.apache.axis2.wso2" % "axis2-client" % "1.6.1.wso2v10",
      "org.apache.woden.wso2" % "woden" % "1.0.0.M8-wso2v1"
    ),
    playCopyAssets <<= playCopyAssetsTask,
    resourceGenerators in Compile += Def.task {
      val portalDistName = name.value + "-" + version.value + ".zip"
      val portalDist = target(_ / "universal").value / portalDistName
      val file = (resourceManaged in Compile).value / "docker" / "Dockerfile"
      val contents = "FROM registry.docker.htrc.indiana.edu/htrc/oracle-java8:alpine" + "\n\n" +
        "RUN wget https://github.com/jwilder/dockerize/releases/download/v0.2.0/dockerize-linux-amd64-v0.2.0.tar.gz" + "\n" +
        "RUN tar -C /usr/local/bin -xzvf dockerize-linux-amd64-v0.2.0.tar.gz" + "\n\n" +
        "WORKDIR /opt" + "\n\n" +
        "ADD %s /opt/".format(portalDist) + "\n" +
        "RUN unzip %s-%s.zip".format(name.value, version.value) + "\n\n" +
        "ADD portal.sh /opt/" + "\n" +
        "RUN chmod +x /opt/portal.sh" + "\n\n" +
        "EXPOSE 9000" + "\n\n" +
        "CMD [\"dockerize\", \"-timeout\", \"120s\", \"-wait\", \"tcp://idp:9443\", \"-wait\", \"tcp://mysql:3306\", \"/opt/portal.sh\"]"
      IO.write(file, contents)
      Seq(file)
    }.taskValue,
    resourceGenerators in Compile += Def.task {
      val file = (resourceManaged in Compile).value / "docker" / "portal.sh"
      val contents = "#!/bin/sh" + "\n\n" +
        "/opt/%s-%s/bin/htrc-portal -Dconfig.file=/htrc/conf/portal/application.conf".format(name.value, version.value) + "\n"
      IO.write(file, contents)
      Seq(file)
    }.taskValue
  )


