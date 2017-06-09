[![Build Status](https://travis-ci.org/marky-mark/play-basic.svg?branch=master)](https://ravis-ci.org/marky-mark/play-basic)

Basic Scala Api Application
============================

## Starting Locally

### Docker Compose

#### Prerequisites
* Docker 9+
* Docker compose
* For Mac, Docker Machine and create a "default" machine

```bash
export DOCKER_IP=$(docker-machine ip default)
```

Can start by using docker compose, but the issue here is that the schema might not be applied. This is due to the limitations of docker compose not waiting for the db to be up applying flyway.

```bash
docker-compose up
```

Alternatively use

```bash
sbt dockerComposeUp
```

### Starting Manually

#### Prerequisites

* Postgres 9.4+
* Follow flyway instructions 


```bash
sbt run
```

### Tests

```bash
sbt test dockerComposeTest
```


