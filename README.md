[![Build Status](https://drone.io/github.com/steve-perkins/fitnessjiffy-spring/status.png)](https://drone.io/github.com/steve-perkins/fitnessjiffy-spring/latest)

# FitnessJiffy (Java / Spring Framework version)

* [Intro and Background](#intro-and-background)
* [Technologies Used](#technologies-used)
* [Previous Technologies Used Along the Way](#previous-technologies-used-along-the-way)
* [Application Features and Screenshots](#application-features-and-screenshots)

## Intro and Background

FitnessJiffy is an application for tracking diet and exercise, and generating charts
and reports for health information over time.  Under the surface though, it's really
a self-learning and teaching tool.  I've written numerous versions of this application
over the years, every time I want some deeper experience in learning a new programming
language or framework.

This version is based on [Java](http://www.oracle.com/technetwork/java/index.html) and
the [Spring Framework](http://spring.io/).  Although these have long been the primary
tools in my professional career, I wrote this version of the app as an excuse to
explore [Spring Boot](http://projects.spring.io/spring-boot/).  I used the code and
build process here as the basis for a lengthy blog post on Spring Boot
(http://steveperkins.com/use-spring-boot-next-project), which was linked from the
Spring website and drew a bit of attention in that community.

***If you're looking for a robust sample application to get started with Spring Boot, then
you've come to right place.***

## Technologies Used 

In addition to Spring Boot, this application makes use of technologies including:

* [Java 8](http://www.oracle.com/technetwork/java/index.html)
* [Spring Boot](http://projects.spring.io/spring-boot/)
* [Spring Data JPA](http://projects.spring.io/spring-data-jpa/)
* [Spring Security](http://projects.spring.io/spring-security/)
* [MySQL](http://dev.mysql.com/) (with [H2](http://www.h2database.com/html/main.html) for unit testing)
* [Flyway](https://flywaydb.org/)
* [JSR-305 annotations](http://findbugs.sourceforge.net/)
* [Thymeleaf templates](http://www.thymeleaf.org/)
* [Twitter Bootstrap](http://getbootstrap.com/)
* [jQuery](http://jquery.com/)
* [amCharts](http://amcharts.com) (A JavaScript library for generating charts and reports)
* [Gradle](http://gradle.org/)
* [JUnit](http://junit.org/)

## Previous Technologies Used Along the Way

If you go splunking through the commit history, you'll find the following:

* [Google Guava](https://code.google.com/p/guava-libraries/)
  * No longer necessary after the move from Java 7 to Java 8
* [Joda-Time](http://www.joda.org/joda-time/)
  * Likewise replaced by the Java 8 standard library
* [PostgreSQL](http://www.postgresql.org/)
  * I know PostgreSQL is more feature-rich than MySQL, and is trendy among developers
  right now.  However, I needed to improve my hands-on familiarity with MySQL due to
  some professional work (dev features or not, MySQL ***blows PostgreSQL away*** when
  it comes to ops support for replication and scalability).  Since this is a small
  open source application, the wide availability of cheap MySQL hosting doesn't hurt
  either.
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

