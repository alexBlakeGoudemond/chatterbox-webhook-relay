# Chatterbox API README

![Chattering Teeth Gif](chattering_teeth.gif)

# What is Included

- Dockerfile to copy the JAR into a Docker Image
- Docker Compose that gets the chatterbox Image working alongside nginx
- NginX configuration that defines location `/chatterbox/github` and resolves it to the fully qualifed URL that the
  Backend expects
- Able to verify the combination works using LocalTunnel: `lt --port 80 --subdomain chatterbox`

## Architecture Diagram

```java
        GitHub Webhook
                │
                ▼
    https://chatterbox.loca.lt
        (LocalTunnel)
                │
                ▼
        ┌───────────────┐
        │   Nginx       │
        │  (Docker)     │
        │ Reverse Proxy │
        └───────────────┘
                │
                ▼
        ┌───────────────┐
        │  Spring Boot  │
        │  Chatterbox   │
        │  (Docker)     │
        │ /api/webhook  │
        └───────────────┘

```

# How to Confirm things Working?

## Create and test the Dockerfile

Build the Image:

```bash
docker build -t chatterbox:latest .
```

Confirm you see the Image created in Docker Desktop.

Then, run the Image:

```bash
docker run -d --name chatterbox -p 8080:8080 chatterbox:latest
```

Confirm working with Logs:

```bash
docker logs -f chatterbox
```

From any terminal outside of docker, confirm the endpoint is working:

```bash
curl -X POST http://localhost:8080/api/webhook/github \
     -H "Content-Type: application/json" \
     -d '{"ping":"hello"}'
```

You should see a valid response!

Now, stop that container:

```bash
docker stop chatterbox
```

and remove the container (keeps the image)

```bash
docker rm chatterbox
```

## Create and test the Docker-Compose

Docker Compose orchestrates multiple containers together.
We are combining Spring Boot + NginX

Start by following the steps outlined in the Dockerfile section above.
Once confirmed working, ensure that no other containers use the Image

Then, build the Container:

```bash
docker-compose up --build -d
```

Enter the container:

```bash
docker exec -it chatterbox-nginx bash
```

and confirm that the endpoint is exposed:

```bash
curl -X POST http://chatterbox:8080/api/webhook/github \
     -H "Content-Type: application/json" \
     -d '{"ping":"hello"}'
```

You should see a valid response!

## Create and test with LocalTunnel

LocalTunnel is creating a public facing URL with SSH for us to use

Start by following the steps outline in Docker-Compose

In a dedicated terminal run:

```bash
lt --port 80 --subdomain chatterbox
```

(this will stay open)

In a separate terminal: 

```bash
curl -X POST https://chatterbox.loca.lt/chatterbox/github \
     -H "Content-Type: application/json" \
     -d '{"ping":"hello"}'
```

(notice the url) You should see a valid response!
