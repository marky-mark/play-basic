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

Start by...

```bash
docker-compose up
```

Alternatively use

```bash
sbt dockerComposeUp
```

### Starting Manually

#### Prerequisites



