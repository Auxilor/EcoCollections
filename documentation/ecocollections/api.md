---
title: "API"
sidebar_position: 9
---

This page is for developers who want to hook into EcoCollections from their own plugin. EcoCollections is open-source, so you can read the code, depend on it, and build on top of it.

## Source code

The source code is on GitHub [here](https://github.com/Auxilor/EcoCollections).

## Adding the dependency

1. Add the Auxilor repository to your `build.gradle.kts`:
2. Add EcoCollections as a `compileOnly` dependency:

```kotlin
repositories {
    maven("https://repo.auxilor.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.exanthiax:EcoCollections:<version>")
}
```

The latest version available on the repo can be found [here](https://github.com/Auxilor/EcoCollections/tags).

<hr/>

## Where to go next

- **Shared APIs:** the [eco framework](https://github.com/Auxilor/eco) is where the shared eco APIs live.
- **Config side:** [How to Make a Collection](how-to-make-a-collection) covers the config that backs the API.