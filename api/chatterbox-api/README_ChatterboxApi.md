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

# Other Details

# Actuator Endpoints

Some Actuator endpoints are available, including Custom Metrics

## Custom Metrics

Some simple metrics are tracked as part of a Filter, before any Endpoint 
is processed. These metrics can be accessed on localhost.

To identify the available metrics:

```bash
curl https://chatterbox.loca.lt/actuator/metrics
```

Then, from those keys, you can drill down into a specific metric:

```bash
curl https://chatterbox.loca.lt/actuator/metrics/webhook.payload.successes
```

### How do the Metrics work?

In Spring Boot, metrics are handled by Micrometer, 
which is the facade for various monitoring backends 
(Prometheus, Datadog, etc.). 

The central piece is usually a MeterRegistry, 
which acts as a container for all the metrics. 
Think of it as a dynamic registry of meters:
- A meter = a single metric, e.g., a counter, gauge, timer, or histogram
- Each meter has a unique name + optional tags
- The registry keeps track of all meters for the lifetime of the application

Internally, `MeterRegistry` implementations 
(like `SimpleMeterRegistry` or `PrometheusMeterRegistry`) 
use a concurrent map to store meters.

In terms of lifecycles:
- The registry lives for the lifetime of the Spring ApplicationContext / JAR
- Meters are singleton within the registry: you always get the same Counter instance
- When the application shuts down, the registry is gone, along with all metrics
