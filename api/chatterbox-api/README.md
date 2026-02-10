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
curl -X POST https://<publicFacingURL>/chatterbox/github -H "Content-Type: application/json" -H "X-GitHub-Event: push" -H "X-GitHub-Delivery: test123" -H "X-Hub-Signature-256: sha256=efd6f5b2172766c993fe6c7c0743faef8262eb38ab0e31c17b1cb910c10817cd" -d '{"repository":{"full_name":"psyAlexBlakeGoudemond/chatterbox"}}'
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
      |      | ( "55618" : "5432" )           |                                              |                                                      |    
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

## Developer Debates and Discussions

As software developers, we make the decisions (the alternative being to blindly repeat what we see mentioned elsewhere)
With this awareness in mind, here are some decisions made in this repo:

- Publicly stated opinions that the authors of this repo challenge:
    - Some 3rd party libraries should be allowed in the Domain layer, as long as risk is accepted
    - Spring annotations are allowed in the Application layer
        - The 'pure' model of Hexagonal Architecture is used like this: must not have any Spring or Jakarta references
          in application / domain packages
        - However, by allowing Spring in the application layer, we can then place (@Service, @Transactional) there
- The Architectural-rules maven dependency is particularly useful here
    - It encodes the expectations of the project
    - A fun exercise is to remove support for a type in the application package and then see how easy it is to address
      the issues mentioned in the tests
- Other Hexagonal Architectural notes
    - the convention is: If a class is in adapter and ends with 'Adapter' - then it COULD implement a port. Else, it
      would implement a contract defined elsewhere, like application.common (think logging)
- Another pattern explored in this repo is the decision that all logging statements are defined in one place
    - Logs are grouped together into separate classes, but they are wired into 1 master Logging class
    - An outcome of this is that logging is consistent, duplication is reduced, and only 1 type need be imported
    - Also decouples from frameworks in the event of moving to PURE hexagonal architecture down the line
    - Tests that need logs are bloated though, as several logging types must be imported. The
      `@ImportSlf4jWebhookLogger` is a lovely solution to this problem
- AI Tools greatly assisted in making this repository. Originally using ChatGPT in the browser and then swapping to
  Junie inside the IDE; the project has grown to this point with their help
    - Other resources that helped a lot are `Collaboration custom scripts` repo
- And the biggest value here is, of course, Webhooks!
- Also, one of the dependencies imported that was never ultimately used was `github-api` (converting to JsonNode makes
  additional HTTP calls - which was not desirable in this case)
- WebFilter and TaskDecorator in regard to Logging and MDC keys

## Acknowledgements
Shoutouts to the following for their time and input:
- `Johan Van Zyl`; offering advice on Hexagonal Architecture and things to consider in general
- `Yoshailen Michael`; offering insights into Hexagonal Architecture on his client project
- All the people who allowed me to talk their ears off while I was sharing my thoughts!
- `Junie` and `ChatGPT` for setting up and working with the project
