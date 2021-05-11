dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "ComposeForms"
include(":example")
include(":forms-compose")
include(":forms-errors")
include(":forms-codegen")
include(":forms-core")
