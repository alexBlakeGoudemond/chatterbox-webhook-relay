# Chatterbox API README

![Chattering Teeth Gif](chattering_teeth.gif)

# What is Included

- Dockerfile to copy the JAR into a Docker Image
- Docker Compose that gets the chatterbox Image working alongside nginx
- NginX configuration that defines location `/chatterbox/github` and resolves it to the fully qualifed URL that the
  Backend expects
- Able to verify the combination works using LocalTunnel: `lt --port 3002 --subdomain chatterbox`

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

## Create and test the Docker-Compose

Docker Compose orchestrates multiple containers together.
We are combining Spring Boot + NginX

Build the Container:

```bash
docker-compose -f docker/docker-compose.yml -p chatterbox-container-grouping up --build -d
```

Enter the container:

```bash
docker exec -it chatterbox-nginx bash
```

and confirm that the endpoint is exposed:

```bash
curl -X POST http://chatterbox:8082/api/webhook/github -H "Content-Type: application/json" -d '{"hello":"world"}'
```

> If you look at the logs of the `chatterbox-api` container, you should see the request was received and mentioned a
> missing header
> You can add the following header if you want: `-H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778"`
> (this corresponds to the payload `{"hello":"world"}`)

## Create and test with LocalTunnel

LocalTunnel is creating a public facing URL with SSH for us to use

Start by following the steps outline in Docker-Compose

In a dedicated terminal run:

```bash
lt --port 3002 --subdomain chatterbox
```

(this will stay open)

In a separate terminal: 

```bash
curl -X POST https://chatterbox.loca.lt/chatterbox/github -H "Content-Type: application/json" -d '{"ping":"hello"}'
```

> (notice the url) 
> 
> You should see a response, with something like "Missing Signature"

# Other Details

## Self Signed Certificate

Self-signed certs allow local environment to look like production.
The browser/Postman/Spring Boot/Nginx config all behave as if it’s real TLS

We can then test:

- SSL termination
- Redirects (HTTP→HTTPS)
- Ciphers, HSTS, etc.
- Cert/key loading
- Proxy settings

### Installing Mkcert on Windows

`scoop bucket add extras`

`scoop install mkcert`

`mkcert -install` (install the local Certificate Authority in your system's trust store, allowing your browser
and OS to trust the certificates generated)

### Generating Cert

generate the Certificate
`mkcert -key-file key.pem -cert-file cert.pem localhost 127.0.0.1 ::1`

Once generated, 2 files will be created: `cert.pem` and `key.pem` - we must add those to Nginx and adjust the config to
reflect

## Testing Cert

> If testing and you see `Missing Signature` - just add the `X-Hub-Signature-256`

this should work (https - lt using port 3002 still):

```bash
curl -X POST https://chatterbox.loca.lt/chatterbox/github      -H "Content-Type: application/json"      -H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778"      -d '{"hello":"world"}'
```

#### Running in a browser

LocalStack may ask for a password.
The tunnel password is the public IP of the computer running the localtunnel client (or the vpn's public IP if connected to one)
To find the password, consider: `https://loca.lt/mytunnelpassword`

Note that if your url is resolved to Spring, it defaults to a GET request
