#!/bin/bash

if [ $# -lt 2 ] ; then
    echo "Usage: $0 <environment: local|dev|staging|prod> <flyway_command: info|migrate|clean|validate|baseline|repair> <tunnel_port: default 1234>"
    exit 1
fi

ENV=$1
FLYWAY_COMMAND=$2
TUNNEL_PORT=${3-"1234"}
SERVICE="play-basic"
DOMAIN="c1nii26fhuxs.eu-central-1.rds.amazonaws.com"
DB_NAME="play_basic"

if [ $ENV = 'dev' ]; then
    DB_HOST="${SERVICE}-${ENV}.${DOMAIN}"
    ENCRYPTED="AQECAHhUSWyaP+x6YsQzcmYOr65N/9DjRg/8X/oQTuyp/skhGQAAAIMwgYAGCSqGSIb3DQEHBqBzMHECAQAwbAYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAw/55RhjLwaHx+IBi4CARCAP5n7/aQdtGKsyNGDOskLfM+lKgcIfnbakwTgwB5oA1/c3eouvaycjr7WaBiMY7H8qAbKGiNFEeKlv74DnAmK5g=="
elif [ $ENV = 'staging' ]; then
    DB_HOST="${SERVICE}-${ENV}.${DOMAIN}"
    ENCRYPTED="AQECAHhUSWyaP+x6YsQzcmYOr65N/9DjRg/8X/oQTuyp/skhGQAAAIMwgYAGCSqGSIb3DQEHBqBzMHECAQAwbAYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAxwRwA2icX1aDtaEBsCARCAP2/sjhMHNvFySd44CJpBKSwtHwURRkT35fGjQzoa/arQ0yHbizxiLBrQS03bqVviQALra8+atfxnD8rQ2Z5++A=="
elif [ $ENV = 'prod' ]; then
    DB_HOST="${SERVICE}-${ENV}.${DOMAIN}"
    ENCRYPTED="AQECAHhUSWyaP+x6YsQzcmYOr65N/9DjRg/8X/oQTuyp/skhGQAAAIMwgYAGCSqGSIb3DQEHBqBzMHECAQAwbAYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAwDC8tSxMvgYQ5mBaMCARCAP6ULpMGvGuhOSWmNZoOlODnIfuUi0ch9Fmo+1SDhBG/D7dhINCV732x3/IBjz0cNYQ5fzeCVkkycxlmcn7xrUg=="
elif [ $ENV = 'local' ]; then
    DB_HOST="localhost"
else
    echo "Environment: local|dev|staging"
    exit 1
fi

echo "Environment $ENV"


if [ $ENV != 'local' ]; then
    if [[ $(lsof -i tcp:${TUNNEL_PORT} | awk 'NR!=1 {print $2}') ]]; then
        echo "Port ${TUNNEL_PORT} is already in use..."
        echo "Usage: $0 <environment: local|dev> <flyway_command: info|migrate|clean|validate|baseline|repair> <tunnel_port: default 1234>"
        exit 1
    fi

    ssh -L ${TUNNEL_PORT}:${DB_HOST}:5432 BASTION -N -f
    echo "A tunnel is established on port ${TUNNEL_PORT}"

fi

# Will need to install kmsclient via pip
#PASSWORD=$(kmsclient decrypt "$ENCRYPTED" eu-central-1 | tail -n1)


if [ $ENV != 'local' ] ; then
    CONNECTION_URL="jdbc:postgresql://localhost:${TUNNEL_PORT}/${DB_NAME}?sslmode=require&sslfactory=org.postgresql.ssl.NonValidatingFactory"
    flyway -driver="org.postgresql.Driver" -user="playbasic" -password="$PASSWORD" -url=${CONNECTION_URL} -locations="filesystem:migration/postgres" ${FLYWAY_COMMAND}
    lsof -i tcp:${TUNNEL_PORT} | awk 'NR!=1 {print $2}' | sort | uniq | xargs kill
else
    CONNECTION_URL="jdbc:postgresql://localhost:5432/${DB_NAME}"
    flyway -driver="org.postgresql.Driver" -user="playbasic" -password="playbasic" -url=${CONNECTION_URL} -locations="filesystem:migration/postgres" ${FLYWAY_COMMAND}
fi

