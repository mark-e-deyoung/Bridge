# Bridge ASM
[![Build Status](https://dev.me1312.net/jenkins/job/Bridge/badge/icon)](https://dev.me1312.net/jenkins/job/Bridge/)
[![Build Verison](https://img.shields.io/badge/dynamic/xml.svg?label=build&url=https%3A%2F%2Fdev.me1312.net%2Fmaven%2Fnet%2FME1312%2FASM%2Fbridge-plugin%2Fmaven-metadata.xml&query=%2F%2Fversioning%2Frelease&colorB=blue)](https://dev.me1312.net/jenkins/job/Bridge/)<br><br>
Bridge is a post-compile maven plugin that injects new advanced functionality into the Java language using existing semantics. Currently, we add the following features:
* [**Redirection of constructors, methods, &amp; fields**](https://github.com/ME1312/Bridge/wiki/Features#bridges) *with* `@Bridge`
* [**Unsafe native referencing of classes, constructors, methods, &amp; fields**](https://github.com/ME1312/Bridge/wiki/Features#invocations) *with* `Invocation`
* [**Unrestricted `goto` execution jumping**](https://github.com/ME1312/Bridge/wiki/Features#jumps) *with* `Label` &amp; `Jump`
* [**Automatic multi-release class forking**](https://github.com/ME1312/Bridge/wiki/Features#forks) *with* `Invocation.LANGUAGE_LEVEL`
* [**Native unchecked casting, throwing, &amp; handling**](https://github.com/ME1312/Bridge/wiki/Features#unchecked) *with* `Unchecked`
* [**Post-compile class hierarchy modification**](https://github.com/ME1312/Bridge/wiki/Features#type-adoption) *with* `@Adopt`
* [**Public implementation hiding**](https://github.com/ME1312/Bridge/wiki/Features#appending-the-synthetic-modifier) *with* `@Synthetic`
* [**Optional stripping of debug metadata**](https://github.com/ME1312/Bridge/wiki/Features#removing-debug-metadata)

*This project can always benefit from your submission of [additional automated testing](https://github.com/ME1312/Bridge/issues/1)!*
<br><br>

## Minecraft & Java 21 quickstart
- Toolchain: target Java 21 (matches current Minecraft 1.20.5+/1.21) and ASM 9.7 already supports Java 22 class files.
- Maven: set `maven.compiler.release` to `21` and align the API + plugin versions with `bridge.version`.

```xml
<properties>
    <bridge.version>00w00a</bridge.version>
    <maven.compiler.release>21</maven.compiler.release>
</properties>

<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/REPO_OWNER/Bridge</url>
    </repository>
</repositories>

<pluginRepositories>
    <pluginRepository>
        <id>github</id>
        <url>https://maven.pkg.github.com/REPO_OWNER/Bridge</url>
    </pluginRepository>
</pluginRepositories>

<dependencies>
    <dependency>
        <groupId>net.ME1312.ASM</groupId>
        <artifactId>bridge</artifactId>
        <version>${bridge.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>net.ME1312.ASM</groupId>
            <artifactId>bridge-plugin</artifactId>
            <version>${bridge.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>bridge</goal>
                    </goals>
                    <configuration>
                        <!-- Keep the server jar on the classpath so Bridge can see NMS classes -->
                        <dependencies>
                            <dependency>
                                <groupId>com.mojang</groupId>
                                <artifactId>minecraft-server</artifactId>
                                <version>${minecraft.version}</version>
                                <scope>system</scope>
                                <systemPath>${minecraft.serverJar}</systemPath>
                            </dependency>
                        </dependencies>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Gradle (Kotlin DSL) starter
Bridge ships as a Maven plugin. If you build with Gradle, the simplest path is to keep Gradle as the driver and invoke Maven’s Bridge goal after compilation:

```kotlin
plugins { java }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories {
    mavenCentral()
    maven("https://dev.me1312.net/maven")
}

dependencies {
    compileOnly("net.ME1312.ASM:bridge:00w00a")
}

val bridge by tasks.registering(Exec::class) {
    group = "bridge"
    description = "Runs Bridge's Maven plugin over compiled classes"
    commandLine(
        "mvn",
        "net.ME1312.ASM:bridge-plugin:00w00a:bridge",
        "-Dbridge.classpath=${layout.buildDirectory.dir(\"classes/java/main\").get().asFile}",
        "-Dminecraft.serverJar=${providers.environmentVariable(\"MINECRAFT_SERVER_JAR\").getOrElse(\"\")}"
    )
    dependsOn(tasks.named("classes"))
}

tasks.named("build") { dependsOn(bridge) }
```

### Remapping to runtime names
Minecraft servers use obfuscated runtime names. After the Bridge rewrite step on Mojang/Yarn/Spigot mappings, remap your output using a tool such as TinyRemapper or SpecialSource. Typical flow:
1. Compile & run Bridge on mapped names.
2. Invoke TinyRemapper (or SpecialSource) with your chosen mappings to produce the obfuscated jar you ship to servers.
3. Keep the server jar as a provided dependency for Bridge so hierarchy scanning still works.

### Optional Minecraft integration test
Activate the `minecraft-it` profile to run a lightweight sanity check against a Mojang-mapped server jar:

```
mvn -P minecraft-it -Dminecraft.serverJar=/path/to/server-<version>-mapped.jar test
```

The module lives in `bridge-mc-it` and probes `net.minecraft.SharedConstants#getGameVersion().getName()` via `Invocation`. If the signature shifts, the test is skipped with a helpful message.

## GitHub Packages (Maven/Gradle)
GitHub Actions publishes artifacts to `https://maven.pkg.github.com/<repo-owner>/Bridge`. For public repositories, packages are public, but GitHub Packages still requires an authenticated request (use `GITHUB_TOKEN` or a PAT with `read:packages`). The workflow derives `<repo-owner>` automatically from `github.repository_owner`. Versions:
- Tagged releases: tag `vX.Y.Z` publishes version `X.Y.Z`.
- Push builds: publish `0.1.0-SNAPSHOT.<run_number>` (useful for CI consumption).

### Maven consumer snippet
```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/${env.GITHUB_REPOSITORY_OWNER}/Bridge</url>
  </repository>
</repositories>
<distributionManagement>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/${env.GITHUB_REPOSITORY_OWNER}/Bridge</url>
  </repository>
</distributionManagement>
```
And for plugins:
```xml
<pluginRepositories>
  <pluginRepository>
    <id>github</id>
    <url>https://maven.pkg.github.com/${env.GITHUB_REPOSITORY_OWNER}/Bridge</url>
  </pluginRepository>
</pluginRepositories>
```
Authenticate with a token via `~/.m2/settings.xml`:
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <servers>
    <server>
      <id>github</id>
      <username>${env.GITHUB_ACTOR}</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
```
If you consume from GitHub Actions, you can omit the settings file and let `actions/setup-java` inject the credentials:
```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: 21
    server-id: github
    server-username: GITHUB_ACTOR
    server-password: GITHUB_TOKEN
```

### Gradle consumer snippet (Kotlin DSL)
```kotlin
val bridgeOwner = providers.environmentVariable("GITHUB_REPOSITORY_OWNER")
    .orElse(providers.environmentVariable("BRIDGE_OWNER"))
    .orElse("REPO_OWNER")
    .get()

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/$bridgeOwner/Bridge")
        credentials {
            username = providers.environmentVariable("GITHUB_ACTOR").orElse("token").get()
            // For public packages, a token is still required; GITHUB_TOKEN or a PAT with read:packages works.
            password = providers.environmentVariable("GITHUB_TOKEN")
                .orElse(providers.environmentVariable("GH_TOKEN"))
                .orElse("")
                .get()
        }
    }
}
dependencies {
    // For a tagged release
    compileOnly("net.ME1312.ASM:bridge:0.1.0")
    // Or for the latest CI snapshot, use the published run number
    // compileOnly("net.ME1312.ASM:bridge:0.1.0-SNAPSHOT.<run_number>")
}
```

> Tip: In GitHub Actions, `GITHUB_REPOSITORY_OWNER` is set automatically. If consuming from a different repo owner, set `BRIDGE_OWNER` to override. Use `GITHUB_TOKEN` or a PAT with `packages:read` to authenticate.

```xml
<!-- required to access the api -->
<repositories>
    <repository>
        <id>ME1312.net</id>
        <url>https://dev.me1312.net/maven</url>
    </repository>
</repositories>

<!-- required to access the maven plugin -->
<pluginRepositories>
    <pluginRepository>
        <id>ME1312.net</id>
        <url>https://dev.me1312.net/maven</url>
    </pluginRepository>
</pluginRepositories>

<!-- ensures the api and maven plugin use the same version -->
<properties> <!-- don't forget to replace this value with a real build id! -->
    <bridge.version>00w00a</bridge.version>
</properties>

<!-- provides you an api to compile against that isn't required at runtime -->
<dependencies>
    <dependency>
        <groupId>net.ME1312.ASM</groupId>
        <artifactId>bridge</artifactId>
        <version>${bridge.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<!-- runs a maven plugin to build your bridges -->
<build>
    <plugins>
        <plugin>
            <groupId>net.ME1312.ASM</groupId>
            <artifactId>bridge-plugin</artifactId>
            <version>${bridge.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>bridge</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
