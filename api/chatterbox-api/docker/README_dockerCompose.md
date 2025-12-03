# DockerCompose README

In this project there are docker images, docker containers, a docker network and a docker-compose. 
This note aims to summarise the design and the overview of how it works

Suppose we run compose and choose a container group name `chatterbox-container-grouping`.
Further suppose we use LocalTunnel with port 3002 to create a public facing URL `https://chatterbox.loca.lt`

Then, thing would look like this:

```sql
                                                  ┌───────────────────────────────┐   ─┐
                                                  │  https://chatterbox.loca.lt   │    │                                      
                                                  │  reserves port xxx            │    │
                                                  └───────────────┬───────────────┘    │
                                                                  │                    │ LocalTunnel                          
                                                                  ▼                    │     ▼
                                                  ┌───────────────────────────────┐    │ host:3002
                                                  │   Host-Level Port Bindings    │    │     ▼
                                                  │    i.e. docker-compose.yml    │    │ docker port binding                  
                                                  │                               │    │     ▼
                                                  │   (hostPort:containerPort)    │    │ nginx:80
                                                  │       ( "3002 : 80"   )       │    │
                                                  │       (  "443 : 443"  )       │    │
                                                  │                               │    │
                                                  └───────────────┬───────────────┘   ─┘
                                                                  │
        ┌─────────────────────────────────────────────────────────│──────────────────────────────────────────────────────────┐
        │ Container Group: chatterbox-container-grouping          │                                                          │
        │                                                         │                                                          │
        │               ┌─────────────────────────────────────────┘                                                          │
        │               │                                                                                                    │
        │               │ hostPort: 3002, 443                                                                                │
        │               │                                                                                                    │
        │               ▼                                                                                                    │
        │ ┌─────────────────────────────────────────────────────────────────────────┐      ┌───────────────────────────────┐ │
        │ │ Service: nginx                                                          │  ┌─────Service: chatterbox           │ │
        │ │ Container: chatterbox-nginx                                             │  │   │ Container: chatterbox-api     │ │
        │ │ Image: chatterbox-nginx:dev                                             │  │   │ Image: chatterbox-api:dev     │ │
        │ │                                                                         │  │   │                               │ │
        │ │       (hostPort:conPort)                                                │  │   │ (no hostPort — internal only) │ │
        │ │       ( "3002 : 80"   )                                                 │  │   │ (internalPort: 1234)          │ │
        │ │       (  "443 : 443"  )                                                 │  │   │                               │ │
        │ │                                                                         │  │   │ GithubWebhookController       │ │
        │ │                                                                         │  │   │                               │ │
        │ │ ┌────────────┐ ┌─────────────────────────────────────────────────────┐  │  │   │                               │ │
        │ │ │conPort: 443│ │conPort: 80                                          │  │  │   │                               │ │
        │ │ │(https)     │ │(http)                                               ├─────┘   │                               │ │
        │ │ │return 501  │ │/chatterbox/github                                   │  │  ▲   │                               │ │
        │ │ │            │ │proxy_pass http://chatterbox:1234/api/webhook/github │  │  │   │                               │ │
        │ │ └────────────┘ └─────────────────────────────────────────────────────┘  │  │   │                               │ │
        │ └─────────────────────────────────────────────────────────────────────────┘  │   └───────────────────────────────┘ │
        │                                                                              │                                     │
        │                                                          (Internal Docker network: chatterbox-net)                 │
        └────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘ 
```

## Create and test the Docker-Compose

Docker Compose orchestrates multiple containers together.
We are combining Spring Boot, NginX, Prometheus and Grafana

Build the Container:

```bash
docker-compose -f docker/docker-compose.yml -p chatterbox-container-grouping up --build -d
```

Enter the container:

```bash
docker exec -it chatterbox-nginx bash
```

then confirm that the containers can talk to one another by checking Actuator

```bash
curl http://chatterbox:1234/actuator/health
```

Once that is working, confirm that the endpoint is exposed:

```bash
curl -X POST http://chatterbox:1234/api/webhook/github -H "Content-Type: application/json" -H "X-GitHub-Event: push" -H "X-GitHub-Delivery: 123" -d '{"hello":"world"}'
```

> If you look at the logs of the `chatterbox-api` container, you should see the request was received and mentioned a
> missing header
> You can add the following header if you want: `-H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778"`
> (this corresponds to the payload `{"hello":"world"}`)
