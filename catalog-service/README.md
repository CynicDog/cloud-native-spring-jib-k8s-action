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

To deploy the containerized database on minikube, run:
```
kubectl run catalog-db --image=postgres:latest --env=POSTGRES_USER=cynicdog --env=POSTGRES_PASSWORD=cynicdog --env=POSTGRES_DB=polardb_catalog --port=5432 --restart=Always

kubectl expose pod catalog-db --port=5432
```
> Make sure the image `postgres:latest` is loaded on minikube context in advance. 
