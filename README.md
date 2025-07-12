# FitnessJiffy (Java / Spring Framework version)

* [Intro and Background](#intro-and-background)
* [Technologies Used](#technologies-used)
* [Previous Technologies Used Along the Way](#previous-technologies-used-along-the-way)
* [Application Features and Screenshots](#application-features-and-screenshots)

## Intro and Background

FitnessJiffy is an application for tracking diet and exercise, and generating charts
and reports for health information over time.  It's also a reference application and
learning tool.  I have re-implemented it many times in various tech stacks, to explore
different languages and frameworks.

This version is based on [Java](http://www.oracle.com/technetwork/java/index.html) and
the [Spring Framework](http://spring.io/).  This version is quite long in the tooth
now, and could use another re-write.  Maybe to separate this monolith a properly RESTful
(or GraphQL?) backend, and a SPA frontend?  However, I originally wrote this version
as an excuse to explore [Spring Boot](http://projects.spring.io/spring-boot/), back when
that was a new thing rather than the dominant way most people use Spring now.  I used the 
code and build process here as the basis for a lengthy blog post on Spring Boot
(http://steveperkins.com/use-spring-boot-next-project), which was linked from the
Spring website and drew a bit of attention in that community at the time.

Since then, I've done only slight maintenance on this codebase.  For example, updating
from Java 8 to 21, Spring Boot 1.x to 2.x, and introducing containerization and a CI pipeline
build.  With Spring Boot 3.x mature now, and 4.0 coming just around the corner, I'm considering 
updating all dependencies and cleaning up the code to match current best practices.  However, I 
may simply move on to another implementation in a different tech stack instead.  Either way, I 
used to publicly promote this as a good reference example application for Spring Boot, but to 
be honest I think its current state is too outdated to serve that purpose well right now.

## Running the application

There is a Docker Compose file in the project's root directory, with which you can run a
database instance without needing MySQL installed locally (if you DO have MySQL installed
locally, then both it and this Dockerized version will conflict on port 3306).

```bash
# To start
docker compose up -d

# To stop
docker compose down
```

The application requires Java 21 (and optionally Docker) to be installed.

```bash
# To run
./gradlew bootRun

# To create a JAR artifact (see the `build/libs` output directory), without running
./gradlew bootJar

# To build, without creating an artifact
./gradlew build 

# To run unit tests
./gradlew check

# To build a Docker image (then tag `fitnessjiffy:latest` for your image registry, and push it there)
docker build -t fitnessjiffy:latest .
```

If you're creating a JAR artifact (or Docker image) to run in a production setting, then you should
have the runtime overwrite the three database connection properties from `src/main/resources/application.properties`
with the correct connection info for your real MySQL database:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://<your-host>:<your-port>/<your-schema>
export SPRING_DATASOURCE_USERNAME=<your-username>
export SPRING_DATASOURCE_PASSWORD=<your-password>
```

## Technologies Used 

In addition to Spring Boot, the application backend makes use of technologies including:

* [Java 21](http://www.oracle.com/technetwork/java/index.html)
* [Gradle](http://gradle.org/)
* [Spring Boot](http://projects.spring.io/spring-boot/)
* [Spring Data JPA](http://projects.spring.io/spring-data-jpa/)
* [Spring Security](http://projects.spring.io/spring-security/)
* [JUnit 5](http://junit.org/)
* [MySQL](http://dev.mysql.com/) (with [H2](http://www.h2database.com/html/main.html) for unit testing)
* [Flyway](https://flywaydb.org/)
* [JSR-305 annotations](http://findbugs.sourceforge.net/)

The frontend stack is properly antiquated at this point, but it still works on current
browsers:

* [Thymeleaf templates](http://www.thymeleaf.org/)
* [Twitter Bootstrap](http://getbootstrap.com/)
* [jQuery](http://jquery.com/)
* [amCharts](http://amcharts.com) (A JavaScript library for generating charts and reports)

## Previous Technologies Used Along the Way

If you go splunking through the commit history, you'll find the following:

* [Google Guava](https://code.google.com/p/guava-libraries/)
  * No longer necessary after the move from Java 7 to Java 8
* [Joda-Time](http://www.joda.org/joda-time/)
  * Likewise replaced by the Java 8 standard library
* [PostgreSQL](http://www.postgresql.org/)
  * At the time (pre-Docker), MySQL was a bit easier to manage from an ops standpoint, and
  it was easier to find cheap MySQL hosting for personal projects.  However, I now regret
  migrating to MySQL, and would definitely migrate back to PostgreSQL if I found the time.
* [Apache Maven](http://maven.apache.org/)

## Application Features and Screenshots

*(click on a screenshot thumbnail to see its full size)*

Login and logout is built around Spring Security, with some custom event-handling
hooks.  In the future, I might add OpenID or OAuth support, to let users authenticate
through an existing account with some provider (e.g. Google, Yahoo, etc).

More importantly, there is not yet a "Create User" function in the web application.  Users
must be created in the database manually.  This is not so much due to the complexity
of adding a "Create User" page, but rather due to not yet being ready to open it up
a hosted version of the application for public use.

However, the Flyway database scripts do create an initial test user for you, with username
`demo@demo.com` and password `password`.

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/login.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/login-thumbnail.png"/></a>

Users can track their weight on a daily basis, and their profile will show their current
body-mass index (BMI) and the estimated number of daily calories needed to maintain
their current weight:

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/profile.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/profile-thumbnail.png"/></a>

Tracking and editing of foods eaten each day:

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-eaten.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-eaten-thumbnail.png"/></a>

Recently-eaten foods (i.e. within the previous two weeks) appear in a convenient
pull-down selector.  Users can also search for foods by name, full or partial:
<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-search.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-search-thumbnail.png"/></a>

The database includes a built-in set of "global" foods, which are visible to all users
but cannot be modified.  When a user modifies a "global" food, or simply creates a new
food from scratch, then a food is created in that user's "private" set of foods.  This
is all transparent to the user.

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-create.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-create-thumbnail.png"/></a>

The database includes comprehensive data on over 800 exercises, taken from the 
[2011 Compendium of Physical Activities](https://sites.google.com/site/compendiumofphysicalactivities/).  By using the 
user's weight on the date when an exercise was performed, FitnessJiffy can calculate
how many calories were burned by that particular user.

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-performed.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-performed-thumbnail.png"/></a>

Users have  quick access to recently-performed exercises (i.e. within the previous two
weeks), can search for exercises by full or partial name, and can browse exercises by
category.

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-search.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-search-thumbnail.png"/></a>

FitnessJiffy stores for each day a summary of each user's stats (e.g. weight, calories
burned, etc), for quick retrieval as JSON so that charts can be rendered and report
data summarized on the client-side.  Whenever any data pertaining to a user changes,
FitnessJiffy schedules a background thread to update that user's report data for the
affected data range.  This thread is scheduled to run after a five-minute delay, to
avoid unnecessary duplication when the user makes multiple changes within a short period
of time.

