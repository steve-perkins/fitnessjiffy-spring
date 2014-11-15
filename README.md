[![Build Status](https://drone.io/github.com/steve-perkins/fitnessjiffy-spring/status.png)](https://drone.io/github.com/steve-perkins/fitnessjiffy-spring/latest)

FitnessJiffy (Java / Spring Framework version)
==============================================

FitnessJiffy is an application for tracking diet and exercise, and generating charts and reports for health 
information over time.  Under the surface, though, it's really a self-learning and teaching tool.  Over the past 
decade or so I've written numerous versions of this application (calling it different names), every time I 
want some deeper experience in learning a new programming language or framework.  

This version is based on [Java](http://www.oracle.com/technetwork/java/index.html) and the 
[Spring Framework](http://spring.io/), and was written as an excuse to learn 
[Spring Boot](http://projects.spring.io/spring-boot/) in particular.  I used the code and build process here as 
the basis for a lengthy blog post on Spring Boot (http://steveperkins.com/use-spring-boot-next-project), which 
was linked from the Spring website and drew a bit of attention in that community.

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

Technologies that I plan to add for the 2.0 release:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) (will replace Guava and Joda-Time)
* [Gradle](http://www.gradle.org/) (will replace Maven, maybe... I'm still undecided about this one)
* [Spock](http://docs.spockframework.org/en/latest/), or some other modern testing framework (will replace or complement JUnit)
* A JavaScript charting library (either [amCharts](http://www.amcharts.com/) or [Highcharts](http://www.highcharts.com/))
* A client-side data binding framework (probably [AngularJS](https://angularjs.org/)... but due to the weird split between Angular 1.x and 2.x, I'm also considering [Ember.js](http://emberjs.com/))
* I'd like to eventually experiment with swapping PostgreSQL for other datastores (e.g. [Cassandra](http://cassandra.apache.org/), [Redis](http://redis.io/), etc).  However, that might end up being a fork or completely different application instead.
