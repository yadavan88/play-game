CREATE TABLE IF NOT EXISTS public."Game"
(
    "gameId" serial PRIMARY KEY,
    name character varying NOT NULL
);
CREATE TABLE IF NOT EXISTS public."GameEggMapping"
(
    "gameEggMappingId" serial PRIMARY KEY,
    "gameId" integer NOT NULL,
    "eggPosition" integer NOT NULL,
    "userId" integer,
    message character varying,
    upvotes integer NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS public."User"
(
    "userId" serial PRIMARY KEY,
    username character varying,
    password character varying,
    active boolean NOT NULL
);

INSERT INTO public."Game"(
    "gameId", name)
VALUES (1,'Find the Egg');

INSERT INTO public."User"(
    "userId", username, password, active)
VALUES (1, 'admin', 'password123', true);