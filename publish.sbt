publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

pomExtra in Global := {
  <url>https://github.com/RomanIakovlev/scope-translator</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/RomanIakovlev/scope-translator.git</connection>
      <developerConnection>scm:git:git@github.com:RomanIakovlev/scope-translator.git</developerConnection>
      <url>github.com/RomanIakovlev/scope-translator</url>
    </scm>
    <developers>
      <developer>
        <id>Roman Iakovlev</id>
        <name>Roman Iakovlev</name>
        <url>http://github.com/RomanIakovlev</url>
      </developer>
    </developers>
}
