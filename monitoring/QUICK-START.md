# Monitoring Stack Quick Start

## Access Points

- **Prometheus UI**: http://localhost:9090
- **Grafana**: http://localhost:3000 (login: admin/admin)
- **Raw Metrics**: http://localhost:8080/api/v1/actuator/prometheus

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

## Creating Dashboards in Grafana

1. Open http://localhost:3000
2. Login with admin/admin
3. Click "+" → "Dashboard" → "Add visualization"
4. Select "Prometheus" as data source
5. Enter one of the queries above
6. Click "Apply"

## Pre-Built Dashboard (Recommended)

Import Spring Boot dashboard from Grafana.com:

1. Go to Grafana → Dashboards → Import
2. Enter dashboard ID: **4701** (Spring Boot 2.1 Statistics)
3. Or ID: **11378** (JVM Micrometer)
4. Select "Prometheus" as data source
5. Click "Import"

## What Gets Monitored Automatically

✅ **HTTP endpoints** - All request/response metrics per endpoint
✅ **Database** - Connection pool, query times, active connections
✅ **JVM** - Memory, GC, threads, CPU
✅ **Application** - Startup time, uptime, health status

**Zero manual instrumentation needed!**
