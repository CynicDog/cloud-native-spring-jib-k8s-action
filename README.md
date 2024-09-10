## Version Table 

|             |Version|
|-------------|--------------|
| Java        | 20 (Temurin) |
| Spring Boot | 3.0.0        |
| Gradle      | 8.10         |
| Jib         | 3.4.3        |

# Local Minikube Deployment Test

Deploy a Spring Boot application using Minikube on macOS and Windows.

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
docker pull cynicdog/cloud-native-spring:latest
```

## 2. Start `minikube` 
```bash
minikube start
```

## 3.1. macOS Steps

3.1.1. Set Docker to use Minikube’s environment:
```bash
eval $(minikube docker-env)
```

3.1.2. Load the image into Minikube:
```bash
minikube image load cynicdog/catalog-service:latest
```

## 3.2. Windows Steps

3.2.1. Save the image as `.tar`:
```bash
docker image save -o catalog-service-image.tar cynicdog/catalog-service:latest
```

3.2.2. Load the image into Minikube:
```bash
minikube image load catalog-service-image.tar
```

## 4. Shared Steps for Deployment and Service Exposure

4.1. Create a deployment:
```bash
kubectl create deployment catalog-service --image=cynicdog/catalog-service:latest
```

4.2. Expose the service on port 8080:
```bash
kubectl expose deployment catalog-service --port=8080
```

4.3. Forward port 8080 to localhost:8000:
```bash
kubectl port-forward service/catalog-service 8000:8080
```

4.4. Test the service:
```bash
http http://localhost:8000/   
```
