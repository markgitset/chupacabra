# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build / Test / Run

This is a multi-module Gradle (Kotlin DSL) project. Use the Gradle wrapper.

- Build everything: `./gradlew build`
- Run all tests + Jacoco coverage: `./gradlew check`
- Run tests for one module: `./gradlew :chupacabra-core:test`
- Run a single test class: `./gradlew :chupacabra-core:test --tests "net.markdrew.chupacabra.core.RangeFunKtTest"`
- Run a single test method: `./gradlew :chupacabra-core:test --tests "net.markdrew.chupacabra.core.RangeFunKtTest.someTestMethod"`
- Publish locally / to GitHub Packages: `./gradlew publish` (requires `GITHUB_ACTOR` + `GITHUB_TOKEN`, or `gpr.user` / `gpr.key` Gradle properties, plus `GITHUB_REPOSITORY` env var to enable the repository)

The Java toolchain is JDK 23 (set in `gradle.properties` as `jvmVersion=23`); Kotlin version is in `gradle.properties` as `systemProp.kotlinVersion`. Don't hardcode versions in `build.gradle.kts` — they're read from properties.

## Architecture

Four modules with a strict dependency chain — `core` is the base; `gson`, `guava`, and `cli` extend it without depending on each other except via the chain:

```
chupacabra-core ──┬── chupacabra-gson  (adds GSON type adapters)
                  ├── chupacabra-guava (adds Guava-flavored utilities)
                  │      └── chupacabra-cli (adds CLI/jline/progressbar wrappers)
```

- **chupacabra-core** has NO module-level `build.gradle.kts` — it picks up everything from the root `subprojects { ... }` block. Its only runtime dep beyond Kotlin is `kotlin-logging-jvm` (and `kotlinx-coroutines-core`, declared in the shared block).
- **chupacabra-gson** treats Guava as an *optional* (`compileOnly`) dependency — adapters that touch Guava types must remain usable when Guava is absent at runtime. Don't promote Guava to `api` here.
- **chupacabra-cli** is the only module with heavyweight transitive deps (jline, progressbar). Keep CLI-only code out of `core`/`guava`.
- The `chupacabra-` filename prefix is recent (renamed from `kraken` — see commit `3fd9a10`). Prefer the `chupacabra` naming throughout; only the historical `tmp.sh~` / `.tmp.sh.un~` files in the repo root are leftover artifacts.

### Cross-cutting build conventions (root `build.gradle.kts`)

All four modules share a single `subprojects { ... }` block that:
- Applies `java-library`, `kotlin`, `jacoco`, `maven-publish`, and `dokka`.
- Adds JUnit 5 (`useJUnitPlatform()`) plus a logback test runtime; **production modules must not depend on a logging implementation** — only on the SLF4J facade via `kotlin-logging`. Logback is `testRuntimeOnly` deliberately.
- Wires `check` to depend on `jacocoTestReport`.
- Builds three publishable artifacts per module: main jar, sources jar, and a `tests` classifier jar (built via the registered `testJar` task) — all three are exposed in the `mavenJava` publication.

### Versioning

The version is **derived from git tags** by the `com.palantir.git-version` plugin (`gitVersion()` in the root build). Don't set `version =` manually anywhere; tag the repo with `v*` to release. The `Publish artifacts` workflow (`.github/workflows/publish-artifacts.yml`) triggers on `v*` tags and on manual `workflow_dispatch`, publishing every module to GitHub Packages and uploading all non-`*-plain` jars as a `chupacabra-jars` workflow artifact.

## Module placement guidance

When adding new utilities, decide module by *what dependencies the utility requires*:
- Pure Kotlin/JDK + logging → `chupacabra-core`
- Needs GSON → `chupacabra-gson`
- Needs Guava (and is independent of GSON) → `chupacabra-guava`
- Needs jline, or terminal/CLI plumbing → `chupacabra-cli`

The README explicitly states: "Nothing with many library dependencies belongs in here." Reject suggestions to add heavyweight deps to `core`.
