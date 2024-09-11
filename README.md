## Version Table 

|             |Version|
|-------------|--------------|
| Java        | 20 (Temurin) |
| Spring Boot | 3.0.0        |
| Gradle      | 8.10         |
| Jib         | 3.4.3        |
| Minikube    | 1.32.0      |

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
> You may specify the build platform of image by adding `--platform` tag with the value of `linux/amd64` or `linux/arm64`. 

## 2. Start `minikube` 
```bash
minikube start
```

## 3.1. Load the image onto Minikube context (for linux/macOS)

3.1.1. Set Docker to use Minikube’s environment:
```bash
eval $(minikube docker-env)
```

3.1.2. Load the image into Minikube:
```bash
minikube image load cynicdog/config-service:latest
```

## 3.2. Load the image onto Minikube context (for Windows)

3.2.1. Save the image as `.tar`:
```bash
docker image save -o config-service-image.tar cynicdog/config-service:latest
```

3.2.2. Load the image into Minikube:
```bash
minikube image load config-service-image.tar
```

## 4. Deployment and Service Exposure

4.1. Create a deployment:
```bash
kubectl create deployment config-service --image=cynicdog/cibfug-service:latest
```

4.2. Expose the service on port 8080:
```bash
kubectl expose deployment config-service --port=8888
```

4.3. Forward port 8080 to localhost:8000:
```bash
kubectl port-forward service/config-service 7777:8888
```

4.4. Test the service:
```bash
http http://localhost:7777/catalog-service/default   
```
You will be seeing the following response: 
```
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 11 Sep 2024 02:12:29 GMT
Keep-Alive: timeout=15
Transfer-Encoding: chunked

{
    "label": null,
    "name": "catalog-service",
    "profiles": [
        "default"
    ],
    "propertySources": [
        {
            "name": "https://github.com/CynicDog/cloud-native-spring-jib-k8s-action/catalog-service.yml",
            "source": {
                "polar.greeting": "(dev) Welcome, greeting message from the configuration server!"
            }
        }
    ],
    "state": null,
    "version": "e26f629a82424fe15b098119b1024b74263cfe68"
}
```
