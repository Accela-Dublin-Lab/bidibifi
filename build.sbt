name := "bidibifi"
version := "1.0"

libraryDependencies ++= Seq(
  "io.vavr" % "vavr" % "0.10.4",
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "org.junit.jupiter" % "junit-jupiter" % "5.9.3" % Test
)

mainClass in Compile := Some("com.accela.bidibifi.proxy.FilteringProxy")

lazy val buildExecutableJar = taskKey[Unit]("Builds the executable JAR")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

buildExecutableJar := {
  (Compile / clean).value
  (Compile / compile).value
  (assembly).value
  val jarFile = (assembly / assemblyOutputPath).value
  val targetDir = (Compile / target).value
  val targetJarFile = targetDir / "bidibifi.jar"
  IO.move(jarFile, targetJarFile)
}

(Compile / run) := (Compile / run).dependsOn(buildExecutableJar).evaluated
