[![Build Status](https://drone.io/github.com/steve-perkins/fitnessjiffy-spring/status.png)](https://drone.io/github.com/steve-perkins/fitnessjiffy-spring/latest)

# FitnessJiffy (Java / Spring Framework version)

* [Intro and Background](#intro-and-background)
* [Technologies Used](#technologies-used)
* [Future Technical Roadmap](#future-technical-roadmap)
* [Application Features and Screenshots](#application-features-and-screenshots)

## Intro and Background

FitnessJiffy is an application for tracking diet and exercise, and generating charts and reports for health 
information over time.  Under the surface, though, it's really a self-learning and teaching tool.  Over the past 
decade or so I've written numerous versions of this application (calling it different names), every time I 
want some deeper experience in learning a new programming language or framework.  

This version is based on [Java](http://www.oracle.com/technetwork/java/index.html) and the 
[Spring Framework](http://spring.io/), and was written as an excuse to learn 
[Spring Boot](http://projects.spring.io/spring-boot/) in particular.  I used the code and build process here as 
the basis for a lengthy blog post on Spring Boot (http://steveperkins.com/use-spring-boot-next-project), which 
was linked from the Spring website and drew a bit of attention in that community.

[FitnessJiffy ETL](https://github.com/steve-perkins/fitnessjiffy-etl) is a companion project... a command-line tool 
for migrating data from previous incarnations of this application, or for exporting the data from any version into 
JSON format.
    
## Technologies Used 

In addition to Spring Boot, this application makes use of technologies including:

* [PostgreSQL](http://www.postgresql.org/) (with [H2](http://www.h2database.com/html/main.html) for unit testing)
* [Spring Data JPA](http://projects.spring.io/spring-data-jpa/)
* [Spring Security](http://projects.spring.io/spring-security/)
* [Google Guava](https://code.google.com/p/guava-libraries/)
* [Joda-Time](http://www.joda.org/joda-time/)
* [JSR-305 annotations](http://findbugs.sourceforge.net/)
* [Thymeleaf templates](http://www.thymeleaf.org/)
* [Twitter Bootstrap](http://getbootstrap.com/)
* [jQuery](http://jquery.com/)
* [Apache Maven](http://maven.apache.org/)
* [JUnit](http://junit.org/)

## Future Technical Roadmap 

Technologies that I plan to add for the 2.0 release:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) (will replace Guava and Joda-Time)
* [Gradle](http://www.gradle.org/) (will replace Maven, maybe... I'm still undecided about this one)
* [Spock](http://docs.spockframework.org/en/latest/), or some other modern testing framework (will replace or complement JUnit)
* A JavaScript charting library (either [amCharts](http://www.amcharts.com/) or [Highcharts](http://www.highcharts.com/))
* A client-side data binding framework (probably [AngularJS](https://angularjs.org/)... but due to the weird split between Angular 1.x and 2.x, I'm also considering [Ember.js](http://emberjs.com/))
* I'd like to eventually experiment with swapping PostgreSQL for other datastores (e.g. [Cassandra](http://cassandra.apache.org/), [Redis](http://redis.io/), etc).  However, that might end up being a fork or completely different application instead.

## Application Features and Screenshots

(click on a screenshot thumbnail to see its full size)

Login and logout is built around Spring Security, with some custom event handling hooks.  In the future, I may add 
OpenID or OAuth support, to let users authenticate through an existing provider account (e.g. Google, Yahoo, etc).  More 
importantly, there is not yet a "Create User" function in the web application.  Users must be created in the 
database manually.  This is not so much due to the complexity of adding a "Create User" page, but rather due to not 
yet being ready to open it up a hosted version of the application for public use.

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/login.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/login-thumbnail.png"/></a>

Users can track their weight on a daily basis, and their profile will show their current body-mass index (BMI) and the 
estimated number of daily calories needed to maintain their current weight:

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/profile.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/profile-thumbnail.png"/></a>

Tracking and editing of foods eaten each day:

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-eaten.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-eaten-thumbnail.png"/></a>

Recently-eaten foods (i.e. within the previous two weeks) appear in a convenient pull-down selector.  Users can also 
search for foods by name, full or partial:

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-search.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-search-thumbnail.png"/></a>

The database includes a built-in set of "global" foods, which are visible to all users but cannot be modified.  When a user 
modifies a "global" food, or simply creates a new food from scratch, then a food is created in that user's "private" 
set of foods.  This is all transparent to the user.  

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-create.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/food-create-thumbnail.png"/></a>

The database includes comprehensive data on over 800 exercises, taken from the 
[2011 Compendium of Physical Activities](https://sites.google.com/site/compendiumofphysicalactivities/).  By using the 
user's weight on the date when an exercise was performed, FitnessJiffy can calculate how many calories were burned 
by that particular user.

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-performed.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-performed-thumbnail.png"/></a>

Users have  quick access to recently-performed exercises (i.e. within the previous two weeks), can search for exercises 
by full or partial name, and can browse exercises by category.

<a href="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-search.png"><img src="https://github.com/steve-perkins/fitnessjiffy-spring/raw/screenshots/screenshots/exercise-search-thumbnail.png"/></a>

FitnessJiffy stores for each day a summary of each user's stats (e.g. weight, calories burned, etc), for quick retrieval as JSON 
so that charts can be rendered and report data summarized on the client-side.  Whenever any data pertaining to a user 
changes, FitnessJiffy schedules a background thread to update that user's report data for the affected data range.  This 
thread is scheduled to run after a five-minute delay, to avoid unnecessary duplication when the user makes multiple changes 
within a short period of time.

CHARTS AND REPORTS SCREENSHOT PENDING

(this work is currently underway in the `reports` branch)
