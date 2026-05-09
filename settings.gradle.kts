pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ElementaEduApp"
include(
    ":app",

    ":common",
    ":common:ui",
    ":common:network",
    ":common:utils",

    ":core",
    ":core:database",
    ":core:datastore",

    ":features",

    ":features:auth",
    ":features:auth:auth-api",
    ":features:auth:auth-impl",

    ":features:catalog",
    ":features:catalog:catalog-api",
    ":features:catalog:catalog-impl",

    ":features:reader",
    ":features:reader:reader-api",
    ":features:reader:reader-impl",

    ":features:user-profile",
    ":features:user-profile:user-profile-api",
    ":features:user-profile:user-profile-impl",

    ":features:tests",
    ":features:tests:tests-api",
    ":features:tests:tests-impl",

    ":features:book",
    ":features:book:book-api",
    ":features:book:book-impl",

    ":features:user-library",
    ":features:user-library:user-library-api",
    ":features:user-library:user-library-impl",
    ":features:collections",
    ":features:collections:collections-impl",
    ":features:collections:collections-api",
)
