# Introduction

Spoofax 3 is a _modular_ and _incremental_ textual language workbench running on the JVM: a collection of tools and Java libraries that enable the development of textual languages, embeddable into batch compilers, code editors and IDEs, or custom applications.
It is a reimplementation of [Spoofax 2](http://spoofax.org), with the goal of being more modular, flexible, and correctly incremental.

Currently, Spoofax 3 is experimental and still a work-in-progress.
Therefore, it does not have a stable API, lacks documentation and test coverage, and has not yet been applied to real-world use cases.
If you are looking for a more mature alternative, see [Spoofax 2](http://spoofax.org), which Spoofax 3 is based on.


## Motivation

We first discuss the motivations for developing Spoofax 3.

### Architecture

The main motivation for developing Spoofax 3 is the monolithic, inflexible, and non-incremental architecture of Spoofax 2:

* It has an *inflexible fixed-function pipeline*, where every file of your language is parsed, analyzed, and transformed.
  This works fine, and can even be incremental when the files of your language can be separately compiled.
  However, this is often not the case.
  Languages should be able to define their own incremental pipelines with minimal effort.
  Those pipelines should be modular and flexible, enabling usage in a wide range applications such as command-line interfaces, build systems, code editors, and IDEs.

* It is monolithic for *language users* (i.e., the users of your programming language that you have developed with Spoofax), as every language developed with Spoofax 2 depends on Spoofax Core, which in turn depends on all meta-components: JSGLR1 and 2, NaBL+TS index and task engine, NaBL2 & Statix solver, dynsem interpreter, Stratego runtime, config parsing libraries, etc.
  A language should only require the meta-components that it uses.

* It is monolithic for *meta-component developers* (e.g., the developers of the language workbench, or researchers experimenting with new meta-tools or meta-languages).
  New meta-components need to be tightly integrated into Spoofax Core, requiring time-consuming changes and introducing increased coupling.
  We should develop meta-components in separation, and loosely couple/integrate them (as much as possible).

* The *build of Spoofax 2* itself is monolithic and non-incremental, as all its components are compiled in one huge non-incremental build, massively increasing iteration time during development.
  The build must be incremental, and components should be separated where possible to reduce coupling, decreasing iteration times.

### Language loading

Furthermore, Spoofax 2 *only support dynamic loading of languages*, where a language can be (re)loaded into the running environment.
This is very useful during language development, as it enables fast prototyping.
However, when we want to statically load the language, we still need to perform the dynamic loading ritual: somehow include the language archive in your application, and then load it at runtime.
This hurts startup time, is not supported on some platforms (e.g., Graal native image), and is tedious.
We should support both static and dynamic loading (where possible).

### Error tracing

Some errors are not being traced back to their source.
For example, many errors in configuration only show up during build time (in the console, which may be hidden) and are not traced back to the configuration file.
This confuses users, and may get stuck on simple things, which then require help from us.
Errors, warnings, and informational messages should be traced back to their source, and shown inline at the source in IDE environments, or shown as a list of messages with origin information on the command-line.
When there are errors, execution should continue in certain instances (e.g., parse error should recover and try to do analysis), but should not in others (e.g., error from static analysis should prevent execution since it could crash).

### Configuration

Another issue is the scattered configuration in language specifications, which is spread over many different places:

* `metaborg.yaml`
* `editor/*.esv`
* `dynsem.properties`
* In meta-languages files. For example, template options in SDF3.
* `pom.xml`
* `.mvn/extensions.xml`

Finding the right configuration option in these files, and keeping them in sync, is tedious.
Furthermore, while most configuration is documented on our website, looking that up still causes a cognitive gap.
We should consolidate configuration that belongs together, and not have any duplicate configuration that needs to be kept in sync.
Configuration should be supported with editor services such as inline errors and code completion, if possible.

Moreover, some parts of a language specification are configured by convention, and these conventions cannot be changed.
For example, the main SDF3 file is always assumed to be `syntax/<language-name>.sdf3`.
When the language name is changed, but we forget to change the name of this main file, no parse table is built.
Configuration conventions should be changeable, and defaults should be persisted to ensure that renamings do not break things.

### Summary of Problems

To summarize, Spoofax 2 suffers from the following problems that form the motivation for Spoofax 3:

* Monolithic, inflexible, and non-incremental architecture causing:
    * Inflexible and slow language processing due to non-incremental fixed-function pipeline
    * Coupling in Spoofax Core: every language depends on Spoofax Core, and Spoofax Core depends on all meta-components
    * Slow iteration times when developing Spoofax 2 due to its monolithic and non-incremental build
    * Tedious to use languages due to dynamic language loading
* Confusing (end-)user experience due to:
    * Bad error traceability
    * Scattered configuration
    * Non-incremental configuration (restarts required to update configuration)


## Key ideas

To solve these problems, we intend to employ the following key ideas in Spoofax 3.

To reduce coupling, Spoofax 3's "Spoofax Core" does not depend on any meta-components.
Instead, a language implementation depends directly on the meta-components that it requires.
For example, the Tiger language implementation depends directly on the JSGLR2 parser, the NaBL2 constraint solver, and the Stratego runtime.

To make language pipelines flexible, modular, and incremental, we use an incremental, modular, and expressive build system as the basis for creating pipelines: [PIE](https://github.com/metaborg/pie).
Language processing steps such as parsing, styling text, analyzing, checking (to provide inline error messages), running (parts of) a compiler, etc. become PIE task definitions.
Tasks, which are instances of these task definitions, can depend on each other, and depend on resources such as files.
The PIE runtime efficiently and incrementally executes tasks.
Furthermore, task definitions can be shared and used by other language implementations, making language implementations modular.

To reduce the tedium of dynamic language loading, we instead choose to do static language loading as the default.
A language implementation is just a JAR file that can be put on the classpath and used as a regular Java library.
For example, to use the JSGLR2 parser of the Tiger language, we just depend on the Tiger language implementation as we would depend on a regular Java library, create an instance of the `TigerParser` class, and then use that to parse a string into an AST.

We still want to automatically provide integrations with the command-line, build systems such as Gradle, and IDEs such as Eclipse and IntelliJ.
Therefore, every language implementation must implement the `LanguageInstance` interface.
Spoofax 3 then provides libraries which take a `LanguageInstance` object, and integrate it with a platform.
For example, `spoofax.cli` takes a `LanguageInstance` object and provides a command-line application, and `spoofax.eclipse` does the same for an Eclipse plugin.

Because language implementations are just regular Java libraries, they now require some Java boilerplate.
However, we do not want language developers to write this Java boilerplate for standard cases.
Therefore, we employ a Spoofax 3 compiler that generates this Java boilerplate.
If the language developer is not happy with the implementation, or wants to customize parts, they can manually implement or extend Java classes where needed.
It is also possible to not use the Spoofax 3 compiler at all, and manually implement all parts.

To enable quick language prototyping, we still support dynamic language loading in environments that support them (e.g., Eclipse and IntelliJ), by dynamically loading the language implementation JAR when changed.
For example, when prototyping the Tiger language in Eclipse, if the syntax definition is changed we run the Spoofax 3 compiler to (incrementally) create a new parse table and Java classes, and dynamically (re)load the JAR.

To improve the user experience, we use a configuration DSL to configure language specifications and implementations.
Thereby configuration is centralized, has domain-specific checking, and editor services such as inline errors and code completion.
We also allow changing of defaults (conventions), and persist them to enable renaming.

To improve error traceability, errors are reported inline where possible.
Errors are traced through PIE pipelines and support origin tracking to easily support error traceability and inline errors for all language implementations.

better builders:
non-Stratego commands
incremental commands
separate commands from how they are executed
support command parameters/arguments
continuous execution

modular and incremental development of Spoofax 3 itself with Gradle

## Current Status

We have stated our key ideas, but since Spoofax 3 is still under heavy development, they have not all been implemented yet.
We now discuss the current status of Spoofax 3 by summarizing the key ideas and whether they has been implemented, along with any comments.

* [x] **Decoupling**: Spoofax Core not depend on any meta-components. Language implementations instead depend on the meta-components they require.
* [x] **Flexible, modular and incremental pipelines**: Use [PIE](https://github.com/metaborg/pie).
* [x] **Static loading**: Use static loading by default, making language implementation plain JAR files, which are easy to use in the Java ecosystem.
* [x] **`LanguageInstance` interface**: Language implementations must implement the `LanguageInstance` interface, which a platform library uses to integrate a language with the platform.
    * An initial version of the `LanguageInstance` interface exists, but this interface is not yet stable and will receive many new features.
    * Currently, this interface contains features pertaining both command-line platforms and IDE/code editor platforms. These may be split up in the future.
* [x] **Generate Java boilerplate**: Generate the Java boilerplate that Spoofax 3 now requires due to the `LanguageInstance` interface and language implementations being plain JAR files.
    * An initial version of the Spoofax 3 compiler exists that generates the Java boilerplate based on a Spoofax 2 language.
    * Configuration for the Spoofax 3 compiler is provided through a Gradle build script, which is verbose.
* [ ] **Quick language prototyping**: Support dynamic language loading in environments that support this, to enable quick language prototyping.
* [ ] **Configuration DSL**: Use a configuration DSL to improve the developer/user experience.
* [ ] **Error origin tracking**: Perform origin tracking and propagation on errors to improve the developer/user experience.
* [x] **Commands**: More flexible and incremental version of "builders" from Spoofax 2.
    * [x] **Non-Stratego commands**: Commands execute PIE tasks, which execute Java code.
    * [x] **Incremental commands**: Commands are incremental because they execute PIE tasks.
    * [x] **Separate commands from how they are executed**: Commands can be bound to IDE/editor menus, command-line commands, or to resource changes.
    * [x] **Command parameters/arguments**: Commands can specify parameters, which must be provided as arguments when executed.
* [x] **Modular and incremental development**: Use Gradle (instead of Maven) to build Spoofax 3, which increases modularity and provides incremental builds for faster iteration times.

Furthermore, we now discuss the status of features that were not new key ideas.

* [ ] Language builds
    * Spoofax 3 currently does not support *building languages* yet. It is only possible to convert a Spoofax 2 language into a Spoofax 3 language through the Spoofax 3 compiler.
* [ ] Meta-language bootstrapping
    * Because Spoofax 3 does not yet support building languages, bootstrapping is not yet possible.
* Meta-tools
    * Parsing with
        * [x] JSGLR1
        * [ ] JSGLR2
            * [ ] Incremental parsing
    * Semantic analysis with
        * [x] NaBL2
        * [x] Statix
        * [ ] FlowSpec
        * [ ] Stratego
    * [x] Transformation (compilation) with Stratego
* Editor services
    * [x] Syntax-based styling
    * [x] Inline error/warning/note messages
    * [ ] Code completion
    * [ ] Outline
    * [ ]
* Platforms
    * [x] Command-line
    * [ ] Eclipse
        * An Eclipse plugin for your language is provided, but it not yet mature
        * Concurrency/parallelism is completely ignored. Therefore, things may run concurrently that are not suppose to which cause data races and crashes.
    * [ ] IntelliJ
        * A very minimal IntelliJ plugin for your language is provided, currently only supporting syntax highlighting and inline parse errors.
    * [ ] Gradle
    * [ ] Maven
    * [ ] REPL

The following features will not be supported

* Analysis with NaBL/TS

## Architecture

