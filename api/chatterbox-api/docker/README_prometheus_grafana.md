# Prometheus and Grafana README

Prometheus (Metrics + Alerts) can be used as a Web Scraping tool

Grafana (Dashboards + Visualization) can be used to display the data scraped from Prometheus with Graphs etc

Together, they can be used for monitoring this application

## Verify Prometheus working

- Navigate to Prometheus UI (`localhost:9090/targets`)
- Confirm that Job exists and is `UP`
- Go to Query, also confirm that query `UP` returns a valid response
- In the Prometheus Query, run `{job="chatterbox-api"}` to see all the metrics available
  - custom metrics should be included here as well

To verify, exec into a container and then run:
```bash
curl http://chatterbox:1234/actuator/prometheus | grep webhook
```

## Verify Grafana working

- Navigate to Grafana UI (`localhost:3000`)
- Navigate to `Connections > Data Sources` and confirm that Data Source is there (Prometheus)
- Confirm the datasource is `http://prometheus:9090` and not localhost
- Confirm dashboard is pulling the correct metrics

# Architecture Diagram

```bash
												  																							
												  +----------------------------------+ 				 +--------------------------------+ 	
												  |	Prometheus UI				     |				 | Grafana UI					  | 	
											 	  |	http://localhost:9090/targets    |				 | http://localhost:3000		  | 	
												  +-----------------+----------------+				 +--------------+-----------------+ 	
																	|												|						
												  					|												|						
   +----------------------------------------------------------------|-----------------------------------------------|--------------------+	
   | Container Group: chatterbox-container-grouping					|												|					 |	
   |	  +------------------------------+		  +-----------------+----------------+				 +--------------+-----------------+	 |	
   |	  |	Service: nginx 				 |  	  |	Service: prometheus 			 |				 | Service: grafana 			  |	 |	
   |	  |	Container: chatterbox-nginx	 |  	  |	Container: chatterbox-prometheus |				 | Container: chatterbox-grafana  |	 |	
   |	  |	Image: chatterbox-nginx:dev	 |  	  |	Image: chatterbox-prometheus:dev |				 | Image: chatterbox-grafana:dev  |	 |	
   |	  |	(hostPort:conPort)			 |  	  |	(hostPort:conPort)				 |				 | (hostPort:conPort)			  |	 |	
   |	  |	( "3002  : 80"   ) 			 |  	  |	( "9090  : 9090" )				 |				 | ( "3000  : 3000" )			  |	 |	
   |	  |	(  "443  : 443"  ) 			 |  	  |									 |				 | 								  |	 |	
   |	  |								 |  	  |	(PromQL) ------------------------+---<----+		 | 								  |	 |	
   |	  |					 			 |  	  |									 |		  |		 | 								  |	 |	
   |	  +-------------+----------------+		  +-----------------+----------------+ 		  |		 +--------------+-----------------+	 |	
   |					|											|						  |						|					 |	
   |					|											| (Scrape Data using	  |						| (Grafana pulls	 |	
   |	  +-------------+---------------+							|  interval, place		  |						|  from prometheus)  |	
   |	  |	Service: chatterbox 		|							|  in Time-Series DB)	  |						|					 |	
   |	  |	Container: chatterbox-api	|							|						  |						|					 |	
   |	  |	Image: chatterbox-api:dev	|							|						  +---------------------+					 |	
   |	  |	(internalPort: 1234)		|							|																	 |	
   |	  | 							|							|																	 |	
   |	  | (expose /metrics) ----------+-------<-------------------+																	 |	
   |	  | 							|																								 |	
   |	  +-----------------------------+																								 |	
   |																																	 |	
   | (Containers communicate via internal docker network: chatterbox-net)																 |	
   +-------------------------------------------------------------------------------------------------------------------------------------+	

```

# More Details

## Prometheus

Prometheus is a metrics collection and alerting system.
It scrapes metrics from applications, servers, containers, and infrastructure and stores them in a time-series database.

One can use it for:
- Collecting metrics like CPU, RAM, request counts, latency, queue sizes, custom business metrics, etc.
- Setting alerts (“Trigger when error rate > 5%”)
- Querying time-series data using PromQL
- Instrumenting your own applications with client libraries

### Optional but Common Add-ons

- Prometheus ---> AlertManager ---> Slack / Email / PagerDuty
- Node Exporter ---> Prometheus (for server metrics)
- Kube-State-Metrics ---> Prometheus (for Kubernetes)

### Example Metrics

#### 1️⃣ Infrastructure Metrics

Servers / Nodes (via Node Exporter)
- node_cpu_seconds_total
- node_memory_Active_bytes
- node_disk_read_bytes_total
- node_network_receive_bytes_total

Why?
Helps detect overloaded nodes, memory leaks, IO bottlenecks, and network issues.

#### 2️⃣ Application Metrics (Throughput, Latency, Errors)

HTTP Services
- http_requests_total
- http_request_duration_seconds
- http_5xx_responses_total
- http_active_requests

Why?
Shows performance, traffic load, and error spikes.

#### 3️⃣ Business Metrics (Very Powerful)

Prometheus is not only for technical metrics — you can expose domain metrics:
- Number of new signups
- Orders created per minute
- Payment failures
- Queue size (e.g., Kafka topic lag)
- Cache hit rate

Why?
Lets the business understand trends and issues early.

#### 4️⃣ Database Metrics

PostgreSQL, MySQL, Redis exporters:
- Queries per second
- Slow queries count
- Replication lag
- Cache hit ratio
- Connection pool usage

Why?
Database problems = application problems.

#### 5️⃣ Container & Kubernetes Metrics
- Pod CPU/Memory usage
- Pod restarts
- Container oom_killed
- Kube-state metrics (deployment status, replicas)
- Node capacity vs requests/limits

Why?
Critical for microservice environments.

## Grafana

Grafana is a visualization and dashboard tool.
It reads data from Prometheus (and many other sources) and turns it into graphs.

One can use it for:
- Creating dashboards for application performance
- Visualizing infrastructure health
- Visualizing business metrics (sales, orders, signups)
- Creating interactive dashboards & alerting rules

## 🙋‍♂️ What do people normally use them for?
Common use cases:
**Prometheus**
- Monitoring microservices (Kubernetes native)
- Monitoring servers & VMs
- Monitoring databases, networks, proxies (NGINX, HAProxy, Envoy)
- Tracking application metrics (request duration, throughput, error rates)
- Alerting engineers when something breaks

**Grafana**
- Dashboards for:
  - Kubernetes clusters
  - CPU / RAM / disk
  - API latency, error rates
  - Kafka / Redis / Postgres metrics
  - Business KPIs
- “Single pane of glass” views for ops teams

## ❓ Why bother?
Why Prometheus?
- You can’t fix what you can’t see – metrics reveal performance issues.
- Essential for SRE, DevOps, microservices, and cloud-native environments.
- Lightweight and fast.
- Powerful alerting (Alertmanager).
- Works beautifully with Kubernetes.

Why Grafana?
- Humans need visualization – not raw numbers.
- Helps detect trends and anomalies at a glance.
- Provides beautiful dashboards for team awareness.
- Easy to share insights across developers, ops, managers.

# Alternative tools

## 🔍 Full Observability Suites

### Datadog
- SaaS (hosted)
- Metrics + Logs + Traces in one place
- Very polished dashboards + alerting
- Expensive but zero maintenance

Good for teams that want everything managed and don't mind cost.

### New Relic
- Great application-level metrics (APM)
- Automatic instrumentation
- Easy setup
- Also SaaS

Good for app performance analysis and error tracing.

### Dynatrace
- Enterprise-focused, auto-discovery of components
- Very intelligent anomaly detection

Good for large orgs wanting “AI-assisted ops”.

## 📊 Metrics + Dashboards (Prometheus Alternatives)

### InfluxDB + Telegraf + Grafana

- Similar stack but Influx uses push architecture
- Better for IoT, high-speed metrics ingestion

### VictoriaMetrics

- Drop-in Prometheus replacement
- Faster and more efficient storage engine
- Popular for large-scale Kubernetes clusters

## 📚 Logging Solutions (Not Metrics) but Related

ELK / OpenSearch (Elasticsearch + Logstash + Kibana)
- Focuses on logs, not metrics
- Often used alongside Prometheus/Grafana

## 🧵 Tracing Systems (For Distributed Traces)

### Jaeger

- Open-source distributed tracing (microservices)
- Often paired with Prometheus/Grafana/Loki

### Grafana Tempo

- Distributed tracing from the Grafana suite
- Works with Loki (logs) and Prometheus (metrics)
