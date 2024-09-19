## Version Table

|             | Version       |
|-------------|---------------|
| JDK         | 20 (Temurin)  |
| Spring Boot | 3.0.0         |
| Gradle      | 8.10          |
| Jib         | 3.4.3         |
| Minikube    | 1.32.0        |

# Local Minikube Deployment Test

Deploy a Spring Boot application using Minikube on macOS and Windows with Ingress and manifest files.

---

## Prerequisites

- Docker
- Minikube
- kubectl
- HTTPie or curl

---

# Steps (macOS & Windows)

## 1. Pull the Docker Image
```bash
docker pull ghcr.io/cynicdog/cloud-native-spring-jib-k8s-action/catalog-service:latest
```
> 1. Make sure to clear the previous credentials on `ghcr.io` in your local Docker context by running `docker logout ghcr.io`.
> 2. You may specify the build platform of the image by adding `--platform` tag with the value `linux/amd64` or `linux/arm64`.

## 2. Start Minikube
```bash
minikube start --cpus 2 --memory 4g --driver docker
```
> Assign compute resources at your need. 

## 3. Load the Image onto Minikube (Optional) 
> You may skip this step if services are to be deployed declaratively using manifest files.

### 3.1. For Linux/macOS:
3.1.1. Set Docker to use Minikube’s environment:
```bash
eval $(minikube docker-env)
```

3.1.2. Load the image into Minikube:
```bash
minikube image load cynicdog/catalog-service:latest
```

### 3.2. For Windows:
3.2.1. Save the image as `.tar`:
```bash
docker image save -o catalog-service-image.tar cynicdog/catalog-service:latest
```

3.2.2. Load the image into Minikube:
```bash
minikube image load catalog-service-image.tar
```

## 4. Apply the Manifest Files

4.1. Enable Ingress on Minikube (if not done already):
```bash
minikube addons enable ingress
```

4.2. Apply the Kubernetes deployment and service manifest:
```bash
kubectl apply -f ./manifest
```

## 5. Access the Application via Ingress

Once the Ingress is applied, you can retrieve the Minikube IP:
```bash
minikube ip
```

Then, expose the cluster to the local environment:
```bash
minikube tunnel
```

You'll be seeing the following result: 
```
http http://127.0.0.1/books

HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Thu, 19 Sep 2024 03:30:02 GMT
Transfer-Encoding: chunked
X-RateLimit-Burst-Capacity: 20
X-RateLimit-Remaining: 19
X-RateLimit-Replenish-Rate: 10
X-RateLimit-Requested-Tokens: 1

[
    {
        "author": "Lyra Silverstar",
        "createdDate": "2024-09-19T03:24:10.731070Z",
        "id": 1,
        "isbn": "1234567891",
        "lastModifiedDate": "2024-09-19T03:24:10.731070Z",
        "price": 9.9,
        "publisher": "Polarsophia",
        "title": "Northern Lights",
        "version": 1
    },
    ... 
]
```