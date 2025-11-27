# README Docker

In this project there are docker images, docker containers, a docker network and a docker-compose. 
This note aims to summarise the design and the overview of how it works

Suppose we run compose and choose a container group name `chatterbox-container-grouping`.
Further suppose we use LocalTunnel with port 3002 to create a public facing URL `https://chatterbox.loca.lt`

Then, thing would look like this:

```java
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
        │ │       ( "3002 : 80"   )                                                 │  │   │                               │ │
        │ │       (  "443 : 443"  )                                                 │  │   │ GithubWebhookController       │ │
        │ │                                                                         │  │   │                               │ │
        │ │                                                                         │  │   │                               │ │
        │ │ ┌────────────┐ ┌─────────────────────────────────────────────────────┐  │  │   │                               │ │
        │ │ │conPort: 443│ │conPort: 80                                          │  │  │   │                               │ │
        │ │ │            │ │                                                     ├─────┘   │                               │ │
        │ │ │return 501  │ │/chatterbox/github                                   │  │  ▲   │                               │ │
        │ │ │            │ │proxy_pass http://chatterbox:8082/api/webhook/github │  │  │   │                               │ │
        │ │ └────────────┘ └─────────────────────────────────────────────────────┘  │  │   │                               │ │
        │ └─────────────────────────────────────────────────────────────────────────┘  │   └───────────────────────────────┘ │
        │                                                                              │                                     │
        │                                                          (Internal Docker network: chatterbox-net)                 │
        └────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘ 
```