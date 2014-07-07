--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: exercise; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE exercise (
    id bytea NOT NULL,
    category character varying(25) NOT NULL,
    code character varying(5) NOT NULL,
    description character varying(250) NOT NULL,
    metabolic_equivalent double precision NOT NULL
);


ALTER TABLE public.exercise OWNER TO postgres;

--
-- Name: exercise_performed; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE exercise_performed (
    id bytea NOT NULL,
    date date NOT NULL,
    minutes integer NOT NULL,
    exercise_id bytea NOT NULL,
    user_id bytea NOT NULL
);


ALTER TABLE public.exercise_performed OWNER TO postgres;

--
-- Name: fitnessjiffy_user; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE fitnessjiffy_user (
    id bytea NOT NULL,
    activity_level double precision NOT NULL,
    birthdate date NOT NULL,
    first_name character varying(20) NOT NULL,
    gender character varying(6) NOT NULL,
    height_in_inches double precision NOT NULL,
    last_name character varying(20) NOT NULL,
    password_hash character varying(100),
    email character varying(100) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    last_updated_time timestamp without time zone NOT NULL
);


ALTER TABLE public.fitnessjiffy_user OWNER TO postgres;

--
-- Name: food; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE food (
    id bytea NOT NULL,
    calories integer NOT NULL,
    carbs double precision NOT NULL,
    default_serving_type character varying(10) NOT NULL,
    fat double precision NOT NULL,
    fiber double precision NOT NULL,
    name character varying(50) NOT NULL,
    protein double precision NOT NULL,
    saturated_fat double precision NOT NULL,
    serving_type_qty double precision NOT NULL,
    sodium double precision NOT NULL,
    sugar double precision NOT NULL,
    owner_id bytea,
    created_time timestamp without time zone NOT NULL,
    last_updated_time timestamp without time zone NOT NULL
);


ALTER TABLE public.food OWNER TO postgres;

--
-- Name: food_eaten; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE food_eaten (
    id bytea NOT NULL,
    date date NOT NULL,
    serving_qty double precision NOT NULL,
    serving_type character varying(10) NOT NULL,
    food_id bytea NOT NULL,
    user_id bytea NOT NULL
);


ALTER TABLE public.food_eaten OWNER TO postgres;

--
-- Name: weight; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE weight (
    id bytea NOT NULL,
    date date NOT NULL,
    pounds double precision NOT NULL,
    user_id bytea NOT NULL
);


ALTER TABLE public.weight OWNER TO postgres;

--
-- Name: exercise_performed_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY exercise_performed
    ADD CONSTRAINT exercise_performed_pkey PRIMARY KEY (id);


--
-- Name: exercise_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY exercise
    ADD CONSTRAINT exercise_pkey PRIMARY KEY (id);


--
-- Name: fitnessjiffy_user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY fitnessjiffy_user
    ADD CONSTRAINT fitnessjiffy_user_pkey PRIMARY KEY (id);


--
-- Name: food_eaten_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY food_eaten
    ADD CONSTRAINT food_eaten_pkey PRIMARY KEY (id);


--
-- Name: food_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY food
    ADD CONSTRAINT food_pkey PRIMARY KEY (id);


--
-- Name: weight_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY weight
    ADD CONSTRAINT weight_pkey PRIMARY KEY (id);


--
-- Name: fk_52nub55r5musrfyjsvpth76bh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY exercise_performed
    ADD CONSTRAINT fk_52nub55r5musrfyjsvpth76bh FOREIGN KEY (exercise_id) REFERENCES exercise(id);


--
-- Name: fk_a6t0pikjip5a2k9jntw8s0755; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY food_eaten
    ADD CONSTRAINT fk_a6t0pikjip5a2k9jntw8s0755 FOREIGN KEY (food_id) REFERENCES food(id);


--
-- Name: fk_fqyglhvonkjbp4kd7htfy02cb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY food_eaten
    ADD CONSTRAINT fk_fqyglhvonkjbp4kd7htfy02cb FOREIGN KEY (user_id) REFERENCES fitnessjiffy_user(id);


--
-- Name: fk_k8ugf925yeo9p3f8vwdo8ctsu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY food
    ADD CONSTRAINT fk_k8ugf925yeo9p3f8vwdo8ctsu FOREIGN KEY (owner_id) REFERENCES fitnessjiffy_user(id);


--
-- Name: fk_o3b6rrwboc2sshggrq8hjw3xu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY exercise_performed
    ADD CONSTRAINT fk_o3b6rrwboc2sshggrq8hjw3xu FOREIGN KEY (user_id) REFERENCES fitnessjiffy_user(id);


--
-- Name: fk_rus9mpsdmijsl6fujhhud5pgu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY weight
    ADD CONSTRAINT fk_rus9mpsdmijsl6fujhhud5pgu FOREIGN KEY (user_id) REFERENCES fitnessjiffy_user(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO fitnessjiffy;
