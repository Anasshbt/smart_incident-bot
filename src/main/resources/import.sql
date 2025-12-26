-- Sample data for development
-- Note: Tables are created by Hibernate, this script runs after table creation

-- Insert sample incidents
INSERT INTO Incident (id, type, severity, status, detectedAt, probableCause, description, serviceName, relatedMetrics)
VALUES (1, 'HIGH_ERROR_RATE', 'HIGH', 'OPEN', CURRENT_TIMESTAMP, 'Database connection saturation', 
        'High rate of HTTP 5xx errors detected in payment-service', 'payment-service', 
        '{"http_5xx_count": 45, "http_total_count": 500}');

INSERT INTO Incident (id, type, severity, status, detectedAt, probableCause, description, serviceName, relatedMetrics)
VALUES (2, 'RESOURCE_EXHAUSTION', 'CRITICAL', 'INVESTIGATING', CURRENT_TIMESTAMP, 'Memory leak detected',
        'Memory usage exceeded 90% threshold', 'order-service',
        '{"memory_usage_percent": 92.5, "memory_limit_mb": 512}');

-- Insert sample log entries
INSERT INTO Log_Entry (id, timestamp, level, message, serviceName, podName)
VALUES (1, CURRENT_TIMESTAMP, 'ERROR', 'Connection timeout to database after 30s', 'payment-service', 'payment-service-pod-1');

INSERT INTO Log_Entry (id, timestamp, level, message, serviceName, podName)
VALUES (2, CURRENT_TIMESTAMP, 'WARN', 'High memory pressure detected', 'order-service', 'order-service-pod-2');

INSERT INTO Log_Entry (id, timestamp, level, message, serviceName, podName)
VALUES (3, CURRENT_TIMESTAMP, 'ERROR', 'OutOfMemoryError: Java heap space', 'order-service', 'order-service-pod-2');

-- Insert sample metrics
INSERT INTO Metric (id, timestamp, metricName, value, serviceName, podName)
VALUES (1, CURRENT_TIMESTAMP, 'cpu_usage_percent', 45.5, 'payment-service', 'payment-service-pod-1');

INSERT INTO Metric (id, timestamp, metricName, value, serviceName, podName)
VALUES (2, CURRENT_TIMESTAMP, 'memory_usage_percent', 92.5, 'order-service', 'order-service-pod-2');

INSERT INTO Metric (id, timestamp, metricName, value, serviceName, podName)
VALUES (3, CURRENT_TIMESTAMP, 'http_5xx_count', 45.0, 'payment-service', 'payment-service-pod-1');
