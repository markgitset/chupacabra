# Project Rules

## Gradle Kotlin DSL Properties
When writing or updating Gradle build configuration files (`build.gradle.kts`):
- Do not use deprecated Kotlin DSL property delegates like `val propName: String by project` or `val propName: String by project.properties` which are deprecated in Gradle 9.6+.
- Instead, use the canonical and warning-free method `project.property("propName") as String` to retrieve project properties.
