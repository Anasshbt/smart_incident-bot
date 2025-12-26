# Smart Incident Bot 🤖

**Automated Cloud Incident Detection & Diagnosis System**

A cloud-native application built with Quarkus for automatically detecting incidents, analyzing their probable cause, and alerting DevOps/SRE teams.

![Quarkus](https://img.shields.io/badge/Quarkus-3.17-blue)
![Java](https://img.shields.io/badge/Java-17+-orange)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-green)
![License](https://img.shields.io/badge/License-MIT-purple)

## 🎯 Features

- **Log Ingestion**: Accept structured logs via REST API
- **Metrics Ingestion**: Collect CPU, memory, latency, and error metrics
- **Anomaly Detection**: Rule-based detection with configurable thresholds
- **Automatic Incident Creation**: Create incidents when anomalies are detected
- **Root Cause Analysis**: Correlate incident types to probable causes
- **Webhook Alerting**: Send structured alerts to external systems
- **Health & Metrics**: Built-in health checks and Prometheus metrics

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Data Ingestion                            │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │   Log API    │    │ Metrics API  │    │   Webhook    │       │
│  └──────┬───────┘    └──────┬───────┘    └──────▲───────┘       │
│         │                   │                    │               │
│         ▼                   ▼                    │               │
│  ┌──────────────────────────────────────────────┴──────────┐    │
│  │              Anomaly Detection Engine                    │    │
│  │  • Error Rate Detection    • Pod Restart Detection       │    │
│  │  • Latency Detection       • CPU/Memory Detection        │    │
│  └──────────────────────────┬──────────────────────────────┘    │
│                             │                                    │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Incident Management                          │   │
│  │  • Auto-creation         • Status tracking               │   │
│  │  • Root cause analysis   • Resolution management         │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (optional, for containerized deployment)

### Run in Development Mode

```bash
cd smart-incident-bot
./mvnw quarkus:dev
```

The application will be available at `http://localhost:8080`

### Build & Run with Docker

```bash
# Build the image
docker build -t smart-incident-bot:latest .

# Run the container
docker run -p 8080:8080 smart-incident-bot:latest
```

## 📡 API Reference

### Authentication

All API endpoints (except health checks) require the `X-API-Token` header:

```bash
-H "X-API-Token: demo-token-change-in-production"
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/logs` | Ingest a single log entry |
| `POST` | `/api/logs/batch` | Ingest multiple log entries |
| `POST` | `/api/metrics` | Ingest a single metric |
| `POST` | `/api/metrics/batch` | Ingest multiple metrics |
| `GET` | `/api/incidents` | List all incidents |
| `GET` | `/api/incidents/open` | List open incidents |
| `GET` | `/api/incidents/{id}` | Get incident by ID |
| `PATCH` | `/api/incidents/{id}/status` | Update incident status |
| `GET` | `/api/incidents/stats` | Get incident statistics |
| `GET` | `/q/health` | Health check |
| `GET` | `/q/metrics` | Prometheus metrics |

## 📖 API Examples

### Ingest a Log Entry

```bash
curl -X POST http://localhost:8080/api/logs \
  -H "Content-Type: application/json" \
  -H "X-API-Token: demo-token-change-in-production" \
  -d '{
    "level": "ERROR",
    "message": "Connection timeout to database after 30s",
    "serviceName": "payment-service",
    "podName": "payment-service-pod-1"
  }'
```

### Ingest Metrics

```bash
curl -X POST http://localhost:8080/api/metrics \
  -H "Content-Type: application/json" \
  -H "X-API-Token: demo-token-change-in-production" \
  -d '{
    "metricName": "cpu_usage_percent",
    "value": 95.5,
    "serviceName": "order-service",
    "podName": "order-service-pod-1",
    "unit": "percent"
  }'
```

### Ingest Batch Metrics

```bash
curl -X POST http://localhost:8080/api/metrics/batch \
  -H "Content-Type: application/json" \
  -H "X-API-Token: demo-token-change-in-production" \
  -d '[
    {"metricName": "http_5xx_count", "value": 45, "serviceName": "api-gateway"},
    {"metricName": "http_total_count", "value": 500, "serviceName": "api-gateway"},
    {"metricName": "memory_usage_percent", "value": 88.5, "serviceName": "api-gateway"}
  ]'
```

### List Open Incidents

```bash
curl http://localhost:8080/api/incidents/open \
  -H "X-API-Token: demo-token-change-in-production"
```

### Update Incident Status

```bash
curl -X PATCH http://localhost:8080/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -H "X-API-Token: demo-token-change-in-production" \
  -d '{
    "status": "RESOLVED",
    "resolvedBy": "john.doe@company.com",
    "resolutionNotes": "Increased connection pool size"
  }'
```

### Get Incident Statistics

```bash
curl http://localhost:8080/api/incidents/stats \
  -H "X-API-Token: demo-token-change-in-production"
```

### Health Check

```bash
curl http://localhost:8080/q/health
```

## ⚙️ Configuration

### Anomaly Detection Thresholds

| Property | Default | Description |
|----------|---------|-------------|
| `app.detection.error-rate-threshold` | 5.0 | Error rate % to trigger incident |
| `app.detection.latency-threshold-ms` | 2000 | Latency threshold in ms |
| `app.detection.cpu-threshold-percent` | 90.0 | CPU usage threshold % |
| `app.detection.memory-threshold-percent` | 85.0 | Memory usage threshold % |
| `app.detection.pod-restart-threshold` | 3 | Pod restart count in 10 min |
| `app.detection.check-interval-seconds` | 30 | Detection check interval |

### Environment Variables

```bash
# Database (Production)
POSTGRES_URL=jdbc:postgresql://host:5432/incidentdb
POSTGRES_USER=incident_user
POSTGRES_PASSWORD=secure_password

# Security
APP_SECURITY_API_TOKEN=your_secure_token

# Alerting
APP_ALERTING_WEBHOOK_URL=http://your-webhook-endpoint/alerts
APP_ALERTING_ENABLED=true
```

## 🐳 Kubernetes Deployment

### Deploy to AKS

```bash
# Create namespace
kubectl create namespace incident-bot

# Apply secrets (update with real values first!)
kubectl apply -f k8s/secret.yaml -n incident-bot

# Apply configmap
kubectl apply -f k8s/configmap.yaml -n incident-bot

# Deploy application
kubectl apply -f k8s/deployment.yaml -n incident-bot
```

### Verify Deployment

```bash
# Check pods
kubectl get pods -n incident-bot

# Check service
kubectl get svc -n incident-bot

# View logs
kubectl logs -l app=smart-incident-bot -n incident-bot
```

## 📊 Metrics

The application exposes Prometheus-compatible metrics at `/q/metrics`:

- HTTP request metrics
- JVM metrics
- Custom incident counters

## 🔒 Security

- Token-based API authentication
- Non-root container execution
- Kubernetes secrets for sensitive data
- CORS configuration support

## 🛠️ Development

### Project Structure

```
smart-incident-bot/
├── src/main/java/com/smartincident/
│   ├── model/          # Entities (Incident, LogEntry, Metric)
│   ├── dto/            # Data Transfer Objects
│   ├── repository/     # Panache Repositories
│   ├── service/        # Business Logic
│   ├── resource/       # REST Endpoints
│   ├── security/       # Authentication
│   └── scheduler/      # Scheduled Tasks
├── src/main/resources/
│   ├── application.properties
│   └── import.sql
├── k8s/                # Kubernetes manifests
├── Dockerfile
└── pom.xml
```

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw package -Pnative
```

## 📝 License

MIT License - See [LICENSE](LICENSE) for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

Built with ❤️ using [Quarkus](https://quarkus.io/)
