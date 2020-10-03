organization := "todo.yaroot"
scalaVersion := "2.13.2"

fork in run := true
addCompilerPlugin("org.typelevel"    % "kind-projector"     % "0.11.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"      %% "better-monadic-for" % "0.3.1" cross CrossVersion.binary)
addCompilerPlugin("com.github.cb372" % "scala-typed-holes"  % "0.1.5" cross CrossVersion.full)

testFrameworks += new TestFramework("munit.Framework")
scalafmtOnCompile := true
cancelable in Global := true

libraryDependencies ++= Seq(
  "io.agroal"      % "agroal-pool" % "1.8",
  "org.tpolecat"  %% "doobie-core" % "0.9.2",
  "org.scalameta" %% "munit"       % "0.7.12" % Test
)
