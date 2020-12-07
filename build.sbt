organization := "com.github.quasi-category"
name := "doobie-agroal"
scalaVersion := "2.13.3"
crossScalaVersions := Seq("2.12.12", "2.13.3")

fork in run := true
addCompilerPlugin("org.typelevel"    % "kind-projector"     % "0.11.2" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"      %% "better-monadic-for" % "0.3.1" cross CrossVersion.binary)
addCompilerPlugin("com.github.cb372" % "scala-typed-holes"  % "0.1.6" cross CrossVersion.full)

testFrameworks += new TestFramework("munit.Framework")
scalafmtOnCompile := true
cancelable in Global := true

libraryDependencies ++= Seq(
  "io.agroal"      % "agroal-pool"       % "1.9",
  "org.tpolecat"  %% "doobie-core"       % "0.9.4",
  "org.postgresql" % "postgresql"        % "42.2.18" % Test,
  "org.scalameta" %% "munit"             % "0.7.19"  % Test,
  "org.typelevel" %% "munit-cats-effect" % "0.3.0"   % Test
)

version ~= (_.replace('+', '-'))
dynver ~= (_.replace('+', '-'))
