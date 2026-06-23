# Backend — Running the Application

## Prerequisites

| Tool | Required Version | Notes |
|------|-----------------|-------|
| Java (JDK) | 24 or 25 | The project targets Java 24. Java 25 (currently installed) works fine. |
| Maven | Not required locally | The Maven Wrapper (`mvnw`) is included and downloads Maven 3.9.12 automatically. |

You do **not** need to install Maven globally. The `./mvnw` script handles everything.

---

## Application Entry Point

```
src/main/kotlin/org/soldatenwohnen/SoldatenWohnenApplication.kt
```

This is a standard Spring Boot + Kotlin bootstrap:

```kotlin
@SpringBootApplication
class SoldatenWohnenApplication

fun main(args: Array<String>) {
    runApplication<SoldatenWohnenApplication>(*args)
}
```

Spring Boot's component scan starts from the `org.soldatenwohnen` package and picks up all annotated classes beneath it.

---

## How to Build and Run

All commands must be run from the project root directory.

### Run in development mode (recommended)

```bash
./mvnw spring-boot:run
```

This compiles and starts the application in one step. Spring Boot DevTools is included, so the app automatically restarts when source files change.

### Build a JAR, then run it

```bash
# 1. Compile and package
./mvnw package

# 2. Run the fat JAR
java -jar target/soldaten-wohnen-0.0.1-SNAPSHOT.jar
```

### Build without running tests

```bash
./mvnw package -DskipTests
```

### Run tests only

```bash
./mvnw test
```

The app starts on **port 8080** by default (`http://localhost:8080`).

---

## Common Error: ClassNotFoundException for SoldatenWohnenApplication

```
Error: Could not find or load main class org.soldatenwohnen.SoldatenWohnenApplication
Caused by: java.lang.ClassNotFoundException: org.soldatenwohnen.SoldatenWohnenApplication
```

**Why this happens:** In Kotlin, a top-level `fun main` compiles to a class named `SoldatenWohnenApplicationKt`, not `SoldatenWohnenApplication`. Running the `.class` file directly with `java -cp ...` will fail because the JVM looks for the wrong class name.

**Fix:** Always use the Maven wrapper to run the application — never invoke `java` directly on the compiled classes:

```bash
# Correct
./mvnw spring-boot:run

# Also correct (fat JAR packages the correct manifest)
java -jar target/soldaten-wohnen-0.0.1-SNAPSHOT.jar

# Wrong — do NOT do this
java -cp target/classes org.soldatenwohnen.SoldatenWohnenApplication
```

---

## Project Overview

| Property | Value |
|----------|-------|
| Spring Boot | 4.0.3 |
| Kotlin | 2.2.21 |
| Java target | 24 |
| Default port | 8080 |
| Architecture | Hexagonal (Ports & Adapters) |
| Bounded context | `katalog/` |

### Package structure

```
src/main/kotlin/org/soldatenwohnen/
├── SoldatenWohnenApplication.kt       ← Entry point
└── katalog/                           ← Bounded context: catalog
    ├── adapter/
    │   ├── in/                        ← Driving adapters (e.g. REST controllers)
    │   └── out/                       ← Driven adapters (e.g. persistence)
    ├── application/
    │   ├── port/
    │   │   ├── in/                    ← Input port interfaces (use cases)
    │   │   └── out/                   ← Output port interfaces (repositories)
    │   └── service/                   ← Application services
    └── domain/
        ├── model/                     ← Domain aggregates and entities
        └── service/                   ← Domain services
```

---

## Reference Documentation

- [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.3/maven-plugin)
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.0.3/reference/using/devtools.html)
- [Spring Web](https://docs.spring.io/spring-boot/4.0.3/reference/web/servlet.html)
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
