## Persistence Setup for local development 

To start up postgres container, run:
```
docker run -d \
    --name polar-postgres \
    -e POSTGRES_USER=cynicdog \
    -e POSTGRES_PASSWORD=cynicdog \
    -e POSTGRES_DB=polardb_catalog \
    -p 5433:5432 \
    postgres:latest
```
> Use port `5433` on your local machine to avoid conflicts with the existing PostgreSQL instance.