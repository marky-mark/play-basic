# Database Migration using Flyway on AWS RDS 

This directory contains:

* `flyway.conf` - database connection parameters
* `flyway.sh` - bash script to connect to Flyway and run commands
* `migration/postgres/` - directory containing versioned SQL migrations (each migration is applied exactly once)


##Migrating Database Schema 

###Install Prerequisites
* Postgres: `brew install postgresql` (make sure postgres 9.4+)
* Flyway CLI: `brew install flyway`

###RDS Database
To migrate the PostgreSQL database on RDS:

######Run script

```
flyway/flyway.sh <environment: dev|staging|live|local> <flyway_command> <tunnel_port: default 1234>

```
* Bastion is used to connect to the database via localhost, a tunnel will be opened on the default port, unless another is specified

* The following Flyway commands can be executed
	* migrate
	* clean
	* validate
	* baseline
	* repair 	

### Local Database
To run locally you need to first create the user and database with which Flyway can connect to migrate schema. 

######Start Postgres server

```
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
```

######Create a new user and database with permissions

```
psql postgres
CREATE USER playbasic WITH PASSWORD 'playbasic';
CREATE DATABASE play_basic;
GRANT all privileges on DATABASE play_basic TO playbasic;
```

######Run script

```
cd flyway
./flyway.sh local migrate
```

##Connecting to RDS 
You can login to the PostgreSQL server on AWS from localhost by executing the following commands:

######Open Tunnel
```
ssh -L 1234:play-basic.c1nii26fhuxs.eu-central-1.rds.amazonaws.com:5432 BASTION BOX -N
```
######Connect to Server
```
psql -h localhost -p 1234 -U setanta -d play_basic
```
