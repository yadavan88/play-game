# play-game
Simple game written while exploring play framework

# Setup

## Pre-requirements
- Install docker
- Start postgres container by using `docker compose up` within `conf` dir of this repository
- This will create the required tables and also insert one record each into `Game` and `User` tables

# Usage
- Compile and start this play application (use `sbt run` for example)
- access `http://localhost:9000`. This will compile the play classes and templates
- Swagger API is available at `http://localhost:9000/assets/swagger.html`
