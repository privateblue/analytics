// we don't use version numbers for Buy, but git commit sha
git.baseVersion := ""
git.useGitDescribe := false
git.gitTagToVersionNumber := { _ => None }
(version in ThisBuild) := {
  val v = (version in ThisBuild).value
  v
    .replaceAll("-SNAPSHOT", "")
    .replaceAll("-", "")
}
