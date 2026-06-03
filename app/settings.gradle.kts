pluginManagement {
    repositories {
        google()
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

// اسم المشروع الافتراضي
rootProject.name = "FoxyMacScanner"

// إخبار محرك البناء بوجود مجلد التطبيق الرئيسي
include(":app")
