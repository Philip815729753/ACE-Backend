FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=1234
ENV MYSQL_DATABASE=ace

COPY createDataTables.sql /docker-entrypoint-initdb.d/