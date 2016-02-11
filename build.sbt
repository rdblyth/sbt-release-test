import ReleaseTransformations._


name := "sbt-test"

version := "1.0"

scalaVersion := "2.11.7"

val foo = ReleaseStep(action = st => {
  val extracted = Project.extract(st)

  val currentVersion = extracted.get(version)
  val nextFunc = extracted.get(releaseNextVersion)
  val nextVersion = nextFunc(currentVersion)

  println(s"current version is ${currentVersion}")
  println(s"next version is ${nextVersion}")

  Process(s"find . -type f -name Dockerfile -exec sed -i '.bak' s/:${currentVersion}/:${nextVersion}/g {} +")!

  st
})

releaseProcess := Seq[ReleaseStep](
  foo,
  setNextVersion,
  commitNextVersion,
  tagRelease,
  pushChanges

)
