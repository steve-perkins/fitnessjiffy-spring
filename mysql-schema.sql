-- MySQL dump 10.13  Distrib 5.6.23, for Win64 (x86_64)
--
-- Host: localhost    Database: fitnessjiffy
-- ------------------------------------------------------
-- Server version	5.6.25-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `exercise`
--

DROP TABLE IF EXISTS `exercise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exercise` (
  `id` binary(16) NOT NULL,
  `category` varchar(25) COLLATE utf8_bin NOT NULL,
  `code` varchar(5) COLLATE utf8_bin NOT NULL,
  `description` varchar(250) COLLATE utf8_bin NOT NULL,
  `metabolic_equivalent` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exercise`
--

LOCK TABLES `exercise` WRITE;
/*!40000 ALTER TABLE `exercise` DISABLE KEYS */;
/*!40000 ALTER TABLE `exercise` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exercise_performed`
--

DROP TABLE IF EXISTS `exercise_performed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exercise_performed` (
  `id` binary(16) NOT NULL,
  `date` date NOT NULL,
  `minutes` int(11) NOT NULL,
  `exercise_id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_oc1fognywyv0fn3dcogp2nn8e` (`user_id`,`exercise_id`,`date`),
  KEY `FK_52nub55r5musrfyjsvpth76bh` (`exercise_id`),
  CONSTRAINT `FK_52nub55r5musrfyjsvpth76bh` FOREIGN KEY (`exercise_id`) REFERENCES `exercise` (`id`),
  CONSTRAINT `FK_o3b6rrwboc2sshggrq8hjw3xu` FOREIGN KEY (`user_id`) REFERENCES `fitnessjiffy_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exercise_performed`
--

LOCK TABLES `exercise_performed` WRITE;
/*!40000 ALTER TABLE `exercise_performed` DISABLE KEYS */;
/*!40000 ALTER TABLE `exercise_performed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fitnessjiffy_user`
--

DROP TABLE IF EXISTS `fitnessjiffy_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fitnessjiffy_user` (
  `id` binary(16) NOT NULL,
  `activity_level` double NOT NULL,
  `birthdate` date NOT NULL,
  `created_time` datetime NOT NULL,
  `email` varchar(100) COLLATE utf8_bin NOT NULL,
  `first_name` varchar(20) COLLATE utf8_bin NOT NULL,
  `gender` varchar(6) COLLATE utf8_bin NOT NULL,
  `height_in_inches` double NOT NULL,
  `last_name` varchar(20) COLLATE utf8_bin NOT NULL,
  `last_updated_time` datetime NOT NULL,
  `password_hash` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `timezone` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fitnessjiffy_user`
--

LOCK TABLES `fitnessjiffy_user` WRITE;
/*!40000 ALTER TABLE `fitnessjiffy_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `fitnessjiffy_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `food`
--

DROP TABLE IF EXISTS `food`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `food` (
  `id` binary(16) NOT NULL,
  `calories` int(11) NOT NULL,
  `carbs` double NOT NULL,
  `created_time` datetime NOT NULL,
  `default_serving_type` varchar(10) COLLATE utf8_bin NOT NULL,
  `fat` double NOT NULL,
  `fiber` double NOT NULL,
  `last_updated_time` datetime NOT NULL,
  `name` varchar(50) COLLATE utf8_bin NOT NULL,
  `protein` double NOT NULL,
  `saturated_fat` double NOT NULL,
  `serving_type_qty` double NOT NULL,
  `sodium` double NOT NULL,
  `sugar` double NOT NULL,
  `owner_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_of9wdgtxdh2mgh2cfh3spllvi` (`id`,`owner_id`),
  KEY `FK_k8ugf925yeo9p3f8vwdo8ctsu` (`owner_id`),
  CONSTRAINT `FK_k8ugf925yeo9p3f8vwdo8ctsu` FOREIGN KEY (`owner_id`) REFERENCES `fitnessjiffy_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `food`
--

LOCK TABLES `food` WRITE;
/*!40000 ALTER TABLE `food` DISABLE KEYS */;
/*!40000 ALTER TABLE `food` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `food_eaten`
--

DROP TABLE IF EXISTS `food_eaten`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `food_eaten` (
  `id` binary(16) NOT NULL,
  `date` date NOT NULL,
  `serving_qty` double NOT NULL,
  `serving_type` varchar(10) COLLATE utf8_bin NOT NULL,
  `food_id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_o17xkhthgnqe2icjgamjbun93` (`user_id`,`food_id`,`date`),
  KEY `FK_a6t0pikjip5a2k9jntw8s0755` (`food_id`),
  CONSTRAINT `FK_a6t0pikjip5a2k9jntw8s0755` FOREIGN KEY (`food_id`) REFERENCES `food` (`id`),
  CONSTRAINT `FK_fqyglhvonkjbp4kd7htfy02cb` FOREIGN KEY (`user_id`) REFERENCES `fitnessjiffy_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `food_eaten`
--

LOCK TABLES `food_eaten` WRITE;
/*!40000 ALTER TABLE `food_eaten` DISABLE KEYS */;
/*!40000 ALTER TABLE `food_eaten` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_data`
--

DROP TABLE IF EXISTS `report_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `report_data` (
  `id` binary(16) NOT NULL,
  `date` date NOT NULL,
  `net_calories` int(11) NOT NULL,
  `net_points` double NOT NULL,
  `pounds` double NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_5bacnypi0a0a5vcxaqovytq93` (`user_id`,`date`),
  CONSTRAINT `FK_mm7j7rv35awetxl921usmtdm4` FOREIGN KEY (`user_id`) REFERENCES `fitnessjiffy_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_data`
--

LOCK TABLES `report_data` WRITE;
/*!40000 ALTER TABLE `report_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weight`
--

DROP TABLE IF EXISTS `weight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `weight` (
  `id` binary(16) NOT NULL,
  `date` date NOT NULL,
  `pounds` double NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r4ky9e01cp3060j1hgmmqo220` (`user_id`,`date`),
  CONSTRAINT `FK_rus9mpsdmijsl6fujhhud5pgu` FOREIGN KEY (`user_id`) REFERENCES `fitnessjiffy_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weight`
--

LOCK TABLES `weight` WRITE;
/*!40000 ALTER TABLE `weight` DISABLE KEYS */;
/*!40000 ALTER TABLE `weight` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-06-20 19:28:07
