import ReleaseTransformations._
import sbtrelease._

name := "sbt-test"

version := (version in ThisBuild).value

scalaVersion := "2.11.7"

def getDockerfilesFiles(f: File): Array[File] = {
  val files = f.listFiles
  files ++ files.filter(_.isDirectory).flatMap(getDockerfilesFiles)
}

val updateFromVersion = ReleaseStep(action = st => {
  val extracted = Project.extract(st)

  val currentVersion = extracted.get(version)
  val nextFunc = extracted.get(releaseNextVersion)
  val nextVersion = nextFunc(currentVersion)

  println(s"current version is ${currentVersion}")
  println(s"next version is ${nextVersion}")

  Process(s"find . -type f -name Dockerfile -exec sed -i .bak s/:${currentVersion}/:${nextVersion}/g {} +")!

  st
})

val commitDockefiles = ReleaseStep(action = st => {
  val extracted = Project.extract(st)

  val currentVersion = extracted.get(version)
  val nextFunc = extracted.get(releaseNextVersion)
  val nextVersion = nextFunc(currentVersion)

  val vcs = Git.mkVcs(new File("."))

  getDockerfilesFiles(vcs.baseDir).filter(_.getName == "Dockerfile").foreach(file=> println(file.getName))

  //getDockerfilesFiles(vcs.baseDir).filter(_.getName == "Dockerfile").foreach(file => vcs.add(file.getPath)!! st.log)

  //vcs.commit(s"Updated to ${nextVersion}") ! st.log

  st
})

val tag = ReleaseStep(action = st => {
  val extracted = Project.extract(st)

  val currentVersion = extracted.get(version)

  Process(s"""git tag ${currentVersion}""")!

  st
})

releaseProcess := Seq[ReleaseStep](
  inquireVersions,
  updateFromVersion,
  setNextVersion,
  commitDockefiles
  //commitNextVersion
  //tag,
  //pushChanges
)
