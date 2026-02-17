# Chupacabra

Chupacabra is a collection of Kotlin libraries containing general-purpose utilities.
Nothing with many library dependencies belongs in here.

## Chupacabra Core (markdrew.net:chupacabra-core)
Small library of general purpose utilities.  Other than Kotlin itself, this 
library only depends on io.github.microutils:kotlin-logging.

## Chupacabra GSON (markdrew.net:chupacabra-gson)
An extension of Chupacabra Core (depends on chupacabra-core) that adds GSON-related utilities

## Chupacabra Guava (markdrew.net:chupacabra-guava)
An extension of Chupacabra Core (depends on chupacabra-core) that adds Guava-related utilities

## Chupacabra CLI (markdrew.net:chupacabra-cli)
An extension of Chupacabra Core (depends on chupacabra-core) that adds utilities for building
user-friendly command-line applications.


## Publishing artifacts
Artifacts are automatically published for downstream projects using the `Publish artifacts` GitHub Actions workflow (`.github/workflows/publish-artifacts.yml`).

- On every pushed tag matching `v*`, all modules are published to GitHub Packages for this repository.
- You can also trigger the workflow manually with `workflow_dispatch`.
- The workflow additionally uploads all built JARs as a workflow artifact named `chupacabra-jars`.
