# Apache MyFaces Core CI

## Apache Jenkins Build Service

MyFaces Core is using the Multi-Branch Pipeline Plugin for the ci builds.

The Plugin is checking all branches for a `Jenkinsfile` containing the build instructions.

[Jenkinsfile](Jenkinsfile)

[MyFaces Core Pipeline][1]

[ASF Jenkins Build Service][2]

[Multibranch Pipeline recipes][3]

## Github Actions

For a quick build Github Actions are configured.

[.github/workflow/myfaces-ci.yml](.github/workflows/myfaces-ci.yml)

ASF Infra has some restrictions and objections about Github Actions.

[GitHub Actions and Secrets][4]

[GitHub Actions status][5]



[1]: https://ci-builds.apache.org/job/MyFaces/job/MyFaces%20Pipeline/

[2]: https://cwiki.apache.org/confluence/display/INFRA/Jenkins

[3]: https://cwiki.apache.org/confluence/display/INFRA/Multibranch+Pipeline+recipes

[4]: https://infra.apache.org/github-actions-secrets.html

[5]: https://cwiki.apache.org/confluence/display/BUILDS/GitHub+Actions+status

[6]: https://travis-ci.com/github/apache/myfaces
