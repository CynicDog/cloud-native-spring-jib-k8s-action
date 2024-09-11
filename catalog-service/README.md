## Persistence Setup for local development 

For window, run: 
```
docker run -d `
    --name polar-postgres `
    -e POSTGRES_USER=cynicdog `
    -e POSTGRES_PASSWORD=cynicdog `
    -e POSTGRES_DB=polardb_catalog `
    -p 5432:5432 `
    postgres:latest
```

For macOS / Linux, run: 
```
docker run -d \
    --name polar-postgres \
    -e POSTGRES_USER=user \
    -e POSTGRES_PASSWORD=password \
    -e POSTGRES_DB=polardb_catalog \
    -p 5432:5432 \
    postgres:14.4
```