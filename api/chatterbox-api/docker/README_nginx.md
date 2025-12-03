# NginX README

NginX is a reverse proxy tool that we are using to expose some parts of the API

## NginX and API Architecture Diagram

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
        │ │       ( "3002  : 80"   )                                                │  │   │ (internalPort: 1234)          │ │
        │ │       (  "443  : 443"  )                                                │  │   │                               │ │
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

## Other Info

There is also a healthcheck! If for some reason nginx tries to start and it cannot connect to the API, we can display 
an html page explaining it is warming up!. Try this in your browser when nginx container is up and api is down:
`http://localhost:3002/actuator/health` (Should also work with curl in terminal)