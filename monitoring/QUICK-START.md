# Monitoring Stack Quick Start

## Access Points

- **Prometheus UI**: http://localhost:9090
- **Raw Metrics**: http://localhost:8080/api/v1/actuator/prometheus
- **API Telemetry** (SPEC endpoint): http://localhost:8080/api/v1/telemetria

## Useful Prometheus Queries

### HTTP Metrics

**Request rate per endpoint**:
```
rate(http_server_requests_seconds_count[1m])
```

**Average response time**:
```
rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])
```

**Requests by status code**:
```
sum by (status, uri) (http_server_requests_seconds_count)
```

**95th percentile response time**:
```
histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket[5m])))
```

### Database Metrics

**Active database connections**:
```
hikaricp_connections_active
```

**Connection pool usage**:
```
hikaricp_connections / hikaricp_connections_max * 100
```

**Query execution time**:
```
rate(hikaricp_connections_usage_seconds_sum[1m]) / rate(hikaricp_connections_usage_seconds_count[1m])
```

### JVM Metrics

**Heap memory usage**:
```
jvm_memory_used_bytes{area="heap"}
```

**GC pause time**:
```
rate(jvm_gc_pause_seconds_sum[1m])
```

**Thread count**:
```
jvm_threads_live_threads
```

## How Metrics Are Used

The `/telemetria` endpoint (matching THE SPEC) queries Micrometer's MeterRegistry to return service metrics in the exact format specified by the challenge:

```json
{
  "servicos": [
    {
      "nome": "simular-investimento",
      "quantidadeChamadas": 120,
      "mediaTempoRespostaMs": 250
    }
  ],
  "periodo": {
    "inicio": "2025-10-01",
    "fim": "2025-10-31"
  }
}
```

Prometheus is used for **persistent storage** of metrics - they survive API restarts.

## What Gets Monitored Automatically

✅ **HTTP endpoints** - All request/response metrics per endpoint
✅ **Database** - Connection pool, query times, active connections
✅ **JVM** - Memory, GC, threads, CPU
✅ **Application** - Startup time, uptime, health status

**Zero manual instrumentation needed!**
