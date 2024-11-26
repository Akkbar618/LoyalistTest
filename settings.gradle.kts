pluginManagement {
    repositories {
        google()  // Упростим настройки Google репозитория
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Добавим для zxing
    }
}

rootProject.name = "LoyalistTest"
include(":app")