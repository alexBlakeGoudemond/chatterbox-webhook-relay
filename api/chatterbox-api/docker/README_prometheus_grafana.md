# README Prometheus and Grafana

Prometheus can be used as a Web Scraping tool

Grafana can be used to display the data scraped from Prometheus visually

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