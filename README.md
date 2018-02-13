[![Build Status](https://travis-ci.org/marky-mark/play-basic.svg?branch=master)](https://travis-ci.org/marky-mark/play-basic)

Basic Scala Api Application
============================

## Starting Locally

### Docker Compose

#### Prerequisites
* Docker for Mac (`brew cask install docker`)
* (Docker compose should be installed already)[https://docs.docker.com/compose/install/#prerequisites]

#### Start

Can start by using docker compose, but the issue here is that the schema might not be applied. This is due to the limitations of docker compose not waiting for the db to be up applying flyway.

```bash
docker-compose up
```

Alternatively use (SBT Docker Compose Plugin)[https://github.com/Tapad/sbt-docker-compose]

```bash
sbt dockerComposeUp
```

#### Stop

```bash
docker-compose down --volumes
```

```bash
sbt dockerComposeStop
```

### Starting Manually

#### Prerequisites

* Postgres 9.4+
* Follow [flyway instructions to install schema](https://github.com/marky-mark/play-basic/tree/master/flyway) 

```bash
sbt run
```

### Tests

```bash
sbt test dockerComposeTest
```

If the integration tests are broken and you want to debug [see here](https://github.com/Tapad/sbt-docker-compose)

### Generate Swagger Json

* TODO: Create SBT Plugin

```bash
brew install swagger-codegen
swagger-codegen generate -i swagger/api.yaml -l swagger -o public/
```

### Some Useful Docker Commands

#### Clear out old docker images

```bash
docker rmi $(docker images --filter "dangling=true" -q --no-trunc) 
```

```
docker system prune
```

```
docker images prune
```

#### Clear out the exited containers

```bash
docker rm -v $(docker ps -a -q -f status=exited)
```

#### List docker images

```bash
docker images -a
```

#### List docker containers

```bash
docker ps -a
```

#### Docker Volume

```
docker volume ls
```

```
docker volume prune
```

#### Docker network

```
docker network ls
```

```
docker inspect <NETWORK_HASH>
```

### Test Coverage

Investigate if IT tests are covered as part of it.

https://github.com/scoverage/sbt-scoverage

```sbt coverage test dockerComposeTest coverageReport```

### TODO - Upgrades

* Play 2.6
* Akka 2.5
* SBT 1.X
* Scala 2.12
* codahale 4.X