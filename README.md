> _" ... No pom.xml, no sources, no compilation ..._
>
> _... Just grab jars from nexus, and run! "_

# Maven standalone runner

_! Not yet released !_

Maven is used heavily for java project management with main support for
development, unit testing and deployment automation.

But maven infrastructure is very powerful, as it has unified repositories
of artifacts (e.g. jars), and `pom.xml` files describing dependencies in
an abstract way, and provides a mechanism of transforming the abstract
coordinates into jars (download to local repository) and automatic dependency
resolution.

This maven plugin - __maven standalone runner__ is shifting it's capabilities
towards running the java packages, using the power and user simplicity of maven.

Given a maven project with main class defined in the jar manifest, you can run it as simple as:

```shell
mvn foundation.fluent.api:run:main -Dartifact=foundation.fluent.api:sample-main-artifact:1.0
```

The plugin will take care of resolving the provided artifact and all it's dependencies,
and running it's main class.

For all possibilities, see description of it's individual goals.

## Goal: `main`
This goal serves for execution of any main class either explicitly provided
via property, or defined in the manifest.

### Example
```shell
mvn foundation.fluent.api:run:main -Dartifact=foundation.fluent.api:sample-main-artifact:1.0
```

### Parameters
| Parameter | Description | Example |
|-------|---------|------|
| `artifact` | Main artifact coordinates | `-Dartifact=com.example:exampleArtifactId:version` |
| `mainClass` | Explicitly provided main class. It may not be needed, if the main class is defined in jar manifest. | `-DmainClass=com.example.Main` |
| `args` | Optional main method arguments (a.k.a command line arguments). | `"-Dargs=A B"`  |
| `argFile` | Optional file with main method arguments, one per line. | `-DargFile=args.txt` |
| `allowSnapshot` | Allow snapshot artifact to be used. By default it is not allowed. | `-DallowSnapshot=true` |

## Goal: `class-path`
This goal simply resolves (and downloads) all dependencies, and only constructs a classpath in the format for
given platform.

### Good to note
E.g. __Spring boot__ applications are often having entry point main class, so it's very simple to use this
plugin to run spring boot application.

### Example
```shell
mvn foundation.fluent.api:run:class-path -Dartifact=foundation.fluent.api:sample-main-artifact:1.0
```

### Parameters
| Parameter | Description | Example |
|-------|---------|------|
| `artifact` | Main artifact coordinates | `-Dartifact=com.example:exampleArtifactId:version` |
| `allowSnapshot` | Allow snapshot artifact to be used. By default it is not allowed. | `-DallowSnapshot=true` |

## Goal: `testng`
This goal simplifies invocation of TestNG tests directly from jar.

This may be often used in higher level (non-unit) test automation, where tests are not unit
tests of the code within the same maven module, but tests are extra delivery,
executed against external SUT (system under test), maybe installed and brought up automatically too,
or prepared in dedicated test environment.

| Parameter | Description | Example |
|-------|---------|------|
| `artifact` | Main artifact coordinates | `-Dartifact=com.example:exampleArtifactId:version` |
| `args` | Optional main method arguments (a.k.a command line arguments). | `"-Dargs=A B"`  |
| `argFile` | Optional file with main method arguments, one per line. | `-DargFile=args.txt` |
| `allowSnapshot` | Allow snapshot artifact to be used. By default it is not allowed. | `-DallowSnapshot=true` |

### Parameter handling
TestNG goal's objective is to simplify the execution.
By default, the main artifact's jar is used as `-testjar` TestNG parameter, so tests or default testNG suite (`testng.xml`)
are taken from that jar.
However via `-Dargs` parameter, one can override this, and specify different jar to be a test jar.
That argument can be also translated from artifact coordinates (abstract representation) to it's corresponding (resolved)
specific jar file.

More on TestNG command-line parameters can be found int TestNG documentation:
https://testng.org/doc/documentation-main.html#running-testng

## Plan
The plan is to provide few additional goals.
1. Support for "preparing" (a.k.a. installing) of the artifact for pure local execution. That should use the `class-path` capability, and prepare startup script.
2. Support for convenient additional common frameworks. E.g. JUnit5.
