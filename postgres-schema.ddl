--
-- PostgreSQL database dump
--

-- Dumped from database version 9.3.5
-- Dumped by pg_dump version 9.3.5
-- Started on 2014-09-20 12:56:46

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 177 (class 3079 OID 11750)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 1997 (class 0 OID 0)
-- Dependencies: 177
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 170 (class 1259 OID 18702)
-- Name: exercise; Type: TABLE; Schema: public; Owner: fitnessjiffy; Tablespace:
--

CREATE TABLE exercise (
    id bytea NOT NULL,
    category character varying(25) NOT NULL,
    code character varying(5) NOT NULL,
    description character varying(250) NOT NULL,
    metabolic_equivalent double precision NOT NULL
);


ALTER TABLE public.exercise OWNER TO fitnessjiffy;

--
-- TOC entry 171 (class 1259 OID 18710)
-- Name: exercise_performed; Type: TABLE; Schema: public; Owner: fitnessjiffy; Tablespace:
--

CREATE TABLE exercise_performed (
    id bytea NOT NULL,
    date date NOT NULL,
    minutes integer NOT NULL,
    exercise_id bytea NOT NULL,
    user_id bytea NOT NULL
);


ALTER TABLE public.exercise_performed OWNER TO fitnessjiffy;

--
-- TOC entry 172 (class 1259 OID 18718)
-- Name: fitnessjiffy_user; Type: TABLE; Schema: public; Owner: fitnessjiffy; Tablespace:
--

CREATE TABLE fitnessjiffy_user (
    id bytea NOT NULL,
    activity_level double precision NOT NULL,
    birthdate date NOT NULL,
    created_time timestamp without time zone NOT NULL,
    email character varying(100) NOT NULL,
    first_name character varying(20) NOT NULL,
    gender character varying(6) NOT NULL,
    height_in_inches double precision NOT NULL,
    last_name character varying(20) NOT NULL,
    last_updated_time timestamp without time zone NOT NULL,
    password_hash character varying(100),
    timezone character varying(50) NOT NULL
);


ALTER TABLE public.fitnessjiffy_user OWNER TO fitnessjiffy;

--
-- TOC entry 173 (class 1259 OID 18726)
-- Name: food; Type: TABLE; Schema: public; Owner: fitnessjiffy; Tablespace:
--

CREATE TABLE food (
    id bytea NOT NULL,
    calories integer NOT NULL,
    carbs double precision NOT NULL,
    created_time timestamp without time zone NOT NULL,
    default_serving_type character varying(10) NOT NULL,
    fat double precision NOT NULL,
    fiber double precision NOT NULL,
    last_updated_time timestamp without time zone NOT NULL,
    name character varying(50) NOT NULL,
    protein double precision NOT NULL,
    saturated_fat double precision NOT NULL,
    serving_type_qty double precision NOT NULL,
    sodium double precision NOT NULL,
    sugar double precision NOT NULL,
    owner_id bytea
);


ALTER TABLE public.food OWNER TO fitnessjiffy;

--
-- TOC entry 174 (class 1259 OID 18734)
-- Name: food_eaten; Type: TABLE; Schema: public; Owner: fitnessjiffy; Tablespace:
--

CREATE TABLE food_eaten (
    id bytea NOT NULL,
    date date NOT NULL,
    serving_qty double precision NOT NULL,
    serving_type character varying(10) NOT NULL,
    food_id bytea NOT NULL,
    user_id bytea NOT NULL
);


ALTER TABLE public.food_eaten OWNER TO fitnessjiffy;

--
-- TOC entry 175 (class 1259 OID 18742)
-- Name: report_data; Type: TABLE; Schema: public; Owner: fitnessjiffy; Tablespace:
--

CREATE TABLE report_data (
    id bytea NOT NULL,
    date date NOT NULL,
    net_calories integer NOT NULL,
    net_points double precision NOT NULL,
    pounds double precision NOT NULL,
    user_id bytea NOT NULL
);


ALTER TABLE public.report_data OWNER TO fitnessjiffy;

--
-- TOC entry 176 (class 1259 OID 18750)
-- Name: weight; Type: TABLE; Schema: public; Owner: fitnessjiffy; Tablespace:
--

CREATE TABLE weight (
    id bytea NOT NULL,
    date date NOT NULL,
    pounds double precision NOT NULL,
    user_id bytea NOT NULL
);


ALTER TABLE public.weight OWNER TO fitnessjiffy;

--
-- TOC entry 1855 (class 2606 OID 18717)
-- Name: exercise_performed_pkey; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY exercise_performed
    ADD CONSTRAINT exercise_performed_pkey PRIMARY KEY (id);


--
-- TOC entry 1853 (class 2606 OID 18709)
-- Name: exercise_pkey; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY exercise
    ADD CONSTRAINT exercise_pkey PRIMARY KEY (id);


--
-- TOC entry 1859 (class 2606 OID 18725)
-- Name: fitnessjiffy_user_pkey; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY fitnessjiffy_user
    ADD CONSTRAINT fitnessjiffy_user_pkey PRIMARY KEY (id);


--
-- TOC entry 1865 (class 2606 OID 18741)
-- Name: food_eaten_pkey; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY food_eaten
    ADD CONSTRAINT food_eaten_pkey PRIMARY KEY (id);


--
-- TOC entry 1861 (class 2606 OID 18733)
-- Name: food_pkey; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY food
    ADD CONSTRAINT food_pkey PRIMARY KEY (id);


--
-- TOC entry 1869 (class 2606 OID 18749)
-- Name: report_data_pkey; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY report_data
    ADD CONSTRAINT report_data_pkey PRIMARY KEY (id);


--
-- TOC entry 1871 (class 2606 OID 18765)
-- Name: uk_5bacnypi0a0a5vcxaqovytq93; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY report_data
    ADD CONSTRAINT uk_5bacnypi0a0a5vcxaqovytq93 UNIQUE (user_id, date);


--
-- TOC entry 1867 (class 2606 OID 18763)
-- Name: uk_o17xkhthgnqe2icjgamjbun93; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY food_eaten
    ADD CONSTRAINT uk_o17xkhthgnqe2icjgamjbun93 UNIQUE (user_id, food_id, date);


--
-- TOC entry 1857 (class 2606 OID 18759)
-- Name: uk_oc1fognywyv0fn3dcogp2nn8e; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY exercise_performed
    ADD CONSTRAINT uk_oc1fognywyv0fn3dcogp2nn8e UNIQUE (user_id, exercise_id, date);


--
-- TOC entry 1863 (class 2606 OID 18761)
-- Name: uk_of9wdgtxdh2mgh2cfh3spllvi; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY food
    ADD CONSTRAINT uk_of9wdgtxdh2mgh2cfh3spllvi UNIQUE (id, owner_id);


--
-- TOC entry 1873 (class 2606 OID 18767)
-- Name: uk_r4ky9e01cp3060j1hgmmqo220; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY weight
    ADD CONSTRAINT uk_r4ky9e01cp3060j1hgmmqo220 UNIQUE (user_id, date);


--
-- TOC entry 1875 (class 2606 OID 18757)
-- Name: weight_pkey; Type: CONSTRAINT; Schema: public; Owner: fitnessjiffy; Tablespace:
--

ALTER TABLE ONLY weight
    ADD CONSTRAINT weight_pkey PRIMARY KEY (id);


--
-- TOC entry 1876 (class 2606 OID 18768)
-- Name: fk_52nub55r5musrfyjsvpth76bh; Type: FK CONSTRAINT; Schema: public; Owner: fitnessjiffy
--

ALTER TABLE ONLY exercise_performed
    ADD CONSTRAINT fk_52nub55r5musrfyjsvpth76bh FOREIGN KEY (exercise_id) REFERENCES exercise(id);


--
-- TOC entry 1879 (class 2606 OID 18783)
-- Name: fk_a6t0pikjip5a2k9jntw8s0755; Type: FK CONSTRAINT; Schema: public; Owner: fitnessjiffy
--

ALTER TABLE ONLY food_eaten
    ADD CONSTRAINT fk_a6t0pikjip5a2k9jntw8s0755 FOREIGN KEY (food_id) REFERENCES food(id);


--
-- TOC entry 1880 (class 2606 OID 18788)
-- Name: fk_fqyglhvonkjbp4kd7htfy02cb; Type: FK CONSTRAINT; Schema: public; Owner: fitnessjiffy
--

ALTER TABLE ONLY food_eaten
    ADD CONSTRAINT fk_fqyglhvonkjbp4kd7htfy02cb FOREIGN KEY (user_id) REFERENCES fitnessjiffy_user(id);


--
-- TOC entry 1878 (class 2606 OID 18778)
-- Name: fk_k8ugf925yeo9p3f8vwdo8ctsu; Type: FK CONSTRAINT; Schema: public; Owner: fitnessjiffy
--

ALTER TABLE ONLY food
    ADD CONSTRAINT fk_k8ugf925yeo9p3f8vwdo8ctsu FOREIGN KEY (owner_id) REFERENCES fitnessjiffy_user(id);


--
-- TOC entry 1881 (class 2606 OID 18793)
-- Name: fk_mm7j7rv35awetxl921usmtdm4; Type: FK CONSTRAINT; Schema: public; Owner: fitnessjiffy
--

ALTER TABLE ONLY report_data
    ADD CONSTRAINT fk_mm7j7rv35awetxl921usmtdm4 FOREIGN KEY (user_id) REFERENCES fitnessjiffy_user(id);


--
-- TOC entry 1877 (class 2606 OID 18773)
-- Name: fk_o3b6rrwboc2sshggrq8hjw3xu; Type: FK CONSTRAINT; Schema: public; Owner: fitnessjiffy
--

ALTER TABLE ONLY exercise_performed
    ADD CONSTRAINT fk_o3b6rrwboc2sshggrq8hjw3xu FOREIGN KEY (user_id) REFERENCES fitnessjiffy_user(id);


--
-- TOC entry 1882 (class 2606 OID 18798)
-- Name: fk_rus9mpsdmijsl6fujhhud5pgu; Type: FK CONSTRAINT; Schema: public; Owner: fitnessjiffy
--

ALTER TABLE ONLY weight
    ADD CONSTRAINT fk_rus9mpsdmijsl6fujhhud5pgu FOREIGN KEY (user_id) REFERENCES fitnessjiffy_user(id);


--
-- TOC entry 1996 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2014-09-20 12:56:46

--
-- PostgreSQL database dump complete
--

