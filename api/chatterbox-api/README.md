# README

![Chattering Teeth Gif](chattering_teeth.gif)

There are several README documents created to explain what is going on here. It is worth glossing over them, as some
show how you can verify things are working and others explain how its working

To find these README files:

Powershell:

```bash
Get-ChildItem -Recurse -Filter "README*" | Select-Object FullName
```

Bash:

```bash
find . -type f -iname "readme*
```

# How to Confirm things Working?

Can use Makefile commands - just review the environment variables at the top!

## Quick Commands

LocalTunnel:

```bash
lt --port 3002 --subdomain chatterbox
```

NGrok:

```bash
ngrok http 3002
```

```bash
curl -X POST https://<publicFacingURL>/chatterbox/github -H "Content-Type: application/json" -H "X-GitHub-Event: push" -H "X-GitHub-Delivery: test123" -H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778" -d '{"hello":"world"}'
```

# Architecture Diagram

```bash
                                                                                                                                                         
        +-------------------------------------+                                                                                                                                                 
        |  (Github)                           |                                                                                                                                                 
        |        +-----------------+          |                                                                                                                                                 
   +->--+--------+ Github API      |          |                                                                                                                                                 
   |    |        +-----------------+          |                                                                                                                                                 
   ↑    |                                     |                                                                                                                                                 
   |    |        +-----------------+          |                                                                                                                                                 
   |    |        | Github Webhooks |          |                                                                                                                                                 
   |    |        +--------+--------+          |                                                                                                                                                 
   |    |                 |                   |                                                                                                                                                 
   |    +-----------------+-------------------+                                                                                                                                                 
   |                      |                                                                                                                              
   ↑                      ↓                                                                                                                              
   |                      |                                                                                                                              
   |    +-----------------+-------------------+          +-------------------------------------+                 +--------------------------------+      
   |    |   Public Facing URL                 |          |   Prometheus UI                     |                 | Grafana UI                     |      
   |    |   https://<publicFacingURL>         |          |   http://localhost:9090/targets     |                 | http://localhost:3000          |      
   |    +-----------------+-------------------+          +-----------------+-------------------+                 +--------------+-----------------+      
   |                      |                                                |                                                    |                        
   |                      ↓                                                ↓                                                    ↓                        
   |                      |                                                |                                                    |                        
   |  +-------------------+------------------------------------------------+----------------------------------------------------+-------------------+    
   |  |                   |                                                |                                                    |                   |    
   |  |      +------------+-----------------------+      +-----------------+-------------------+                 +--------------+-----------------+ |    
   |  |      | Service: nginx                     |      | Service: prometheus                 |                 | Service: grafana               | |    
   ↑  |      | Container: chatterbox-nginx        |      | Container: chatterbox-prometheus    |                 | Container: chatterbox-grafana  | |    
   |  |      | Image: chatterbox-nginx:dev        |      | Image: chatterbox-prometheus:dev    |                 | Image: chatterbox-grafana:dev  | |    
   |  |      | (hostPort:conPort)                 |      | (hostPort:conPort)                  |                 | (hostPort:conPort)             | |    
   |  |      | ( "3002  : 80"   )                 |      | ( "9090  : 9090" )                  |                 | ( "3000  : 3000" )             | |    
   |  |      | (  "443  : 443"  )                 |      |                                     |                 |                                | |    
   |  |      |                                    |      | (PromQL) ---------------------------+---<----+        |                                | |    
   |  |      |                                    |      |                                     |        |        |                                | |    
   |  |      +-------------+----------------------+      +-----------------+-------------------+        |        +--------------+-----------------+ |    
   |  |                    |                                               |                            |                       |                   |    
   ↑  |                    ↓                                               ↓                            ↑                       ↓                   |    
   |  |                    |                                               | (Scrape Data using         |                       | (Grafana pulls    |    
   |  |      +-------------+----------------+                              |  interval, place           |                       |  from prometheus) |    
   |  |      | Service: chatterbox          |                              |  in Time-Series DB)        |                       |                   |    
   |  |      | Container: chatterbox-api    |                              |                            |                       |                   |    
   |  |      | Image: chatterbox-api:dev    |                              |                            +-----------------------+                   |    
   |  |      | (internalPort: 1234)         |                              |                                                                        |    
   |  |      |                              |                              |                                                                        |    
   ↑  |      |   (expose /metrics) ---------+-------<----------------<-----+                                                                        |    
   |  |      |                              |                                                                                                       |    
   +--+--<---+-- (poll Github API)          |                                                                                                       |    
      |      |                              |                                                                                                       |    
      |      |   (deliver updates) ---------+------->---------------->------------------>----+                                                      |    
      |      |                              |                                                |                                                      |    
      |      +-------------+----------------+                                                |                                                      |    
      |                    |                                                                 |                                                      |    
      |                   ↑ ↓                                                                ↓                                                      |    
      |                    |                                                                 |                                                      |    
      |      +-------------+------------------+                                              |                                                      |    
      |      | Service: postgres              |                                              |                                                      |    
      |      | Container: chatterbox-postgres |                                              |                                                      |    
      |      | Image: chatterbox-postgres:dev |                                              |                                                      |    
      |      | (hostPort : conPort)           |                                              |                                                      |    
      |      | ( "55432" : "5432" )           |                                              |                                                      |    
      |      |                                |                                              |                                                      |    
      |      +--------------------------------+                                              ↓                                                      |    
      |                                                                                      |                                                      |    
      | Container Group: chatterbox-container-grouping                                       |                                                      |    
      | (Containers communicate via internal docker network: chatterbox-net)                 |                                                      |    
      +--------------------------------------------------------------------------------------+------------------------------------------------------+    
                                                                                             |                                                                    
                                                                                             ↓                                                                    
                                                                                             |                                                                    
                                                                     +-----------------------+-----------------------+                                             
                                                                     |                                               |                                           
                                                                     ↓                                               ↓                                           
                                                                     |                                               |                                           
                                                  +------------------+------------------+        +-------------------+-----------------+                                                                                                                                                           
                                                  | (Delivery Example: MS Teams)        |        |  (Delivery Example: Discord)        |                                    
                                                  +-------------------------------------+        +-------------------------------------+                                                                                                                                                           
```