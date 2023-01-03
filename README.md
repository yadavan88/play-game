# play-game
Simple game written while exploring play framework

# Setup

## Pre-requirements
- Install docker
- Start postgres container by using `docker compose up` within `conf` dir of this repository
- This will create the required tables and also insert one record into `Game`

# Usage
- Compile and start this play application (use `sbt run` for example)
- Make sure that docker is running with postgres container and required tables are created
- access `http://localhost:9000`. This will compile the play classes and templates. 
- Create new user
- Swagger API is available at `http://localhost:9000/assets/swagger.html`

# Running as executable
- Use `sbt stage`. This will create the executable bundle 
- Navigate to `target/universal/stage/bin`
- Execute the `play-game` executable file

# Main concepts covered in this project
- Play setup and configurations
- Routes, URL params 
- Action - Sync and Async
- Authentication and Essential Action
- Dependency Injection
- Play Slick Integration
- Twirl template Usage
- Play Caching API with Caffeine
- Swagger Documentation
