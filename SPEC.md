# Vitals Alert Pipeline — Specification

**Version:** 1.0  
**Status:** Baseline  
**Last updated:** 2025

---

## 1. Overview

An event-driven backend that accepts patient vitals readings via a REST API, streams them through Kafka, persists them to MongoDB, and raises alert events when a reading is outside safe thresholds.

All data is **synthetic only**. No real patient data is ever used, stored, or transmitted.

---

## 2. Services

| Service | Responsibility |
|---|---|
| `vitals-api` | Accepts readings via HTTP, validates them, publishes to Kafka |
| `vitals-consumer` | Consumes from Kafka, persists to MongoDB, raises alerts for abnormal readings |

---

## 3. API — vitals-api

### 3.1 Base URL

```
http://localhost:8080
```

### 3.2 Authentication

HTTP Basic Authentication is required on all endpoints.

| Credential | Value (local dev) |
|---|---|
| Username | `vitals-user` |
| Password | Set via environment variable `API_PASSWORD` — never hardcoded |

### 3.3 Endpoints

#### POST /vitals

Accepts a single vitals reading for a patient.

**Request headers:**
```
Content-Type: application/json
Authorization: Basic <base64(username:password)>
```

**Request body:**
```json
{
  "patientId": "uuid-v4",
  "heartRate": 72,
  "systolicBp": 120,
  "diastolicBp": 80,
  "oxygenSaturation": 98.5,
  "temperatureCelsius": 36.8,
  "timestamp": "2025-01-01T10:00:00Z"
}
```

**Field constraints:**

| Field | Type | Required | Valid range |
|---|---|---|---|
| `patientId` | UUID string | Yes | Valid UUID v4 |
| `heartRate` | integer | Yes | 20–300 bpm |
| `systolicBp` | integer | Yes | 50–300 mmHg |
| `diastolicBp` | integer | Yes | 30–200 mmHg |
| `oxygenSaturation` | decimal | Yes | 50.0–100.0 % |
| `temperatureCelsius` | decimal | Yes | 25.0–45.0 °C |
| `timestamp` | ISO-8601 UTC | Yes | Not in the future |

**Responses:**

| Status | Meaning |
|---|---|
| `202 Accepted` | Reading received and published to Kafka |
| `400 Bad Request` | Validation failure — body contains field-level errors |
| `401 Unauthorized` | Missing or invalid credentials |
| `500 Internal Server Error` | Kafka publish failure |

**202 response body:**
```json
{
  "readingId": "uuid-v4",
  "status": "ACCEPTED"
}
```

**400 response body:**
```json
{
  "status": "VALIDATION_ERROR",
  "errors": [
    { "field": "heartRate", "message": "must be between 20 and 300" }
  ]
}
```

#### GET /actuator/health

Spring Boot Actuator health endpoint. No authentication required.

**200 response body:**
```json
{ "status": "UP" }
```

---

## 4. Kafka Topics

| Topic | Producer | Consumer | Purpose |
|---|---|---|---|
| `vitals-raw` | `vitals-api` | `vitals-consumer` | All incoming readings |
| `vitals-alerts` | `vitals-consumer` | Future alerting service | Abnormal readings only |
| `vitals-dlt` | `vitals-consumer` (Spring retry) | Ops monitoring | Failed messages after max retries |

### 4.1 Topic Configuration

| Setting | Value | Reason |
|---|---|---|
| Partitions | 3 | Allows 3 concurrent consumer instances |
| Replication factor | 1 (dev) / 3 (prod) | Single broker locally; 3-broker cluster in HA layer |
| Retention | 7 days | Enough for replay and audit |
| Partition key | `patientId` | Preserves ordering of readings per patient |

### 4.2 Message Schema — vitals-raw

```json
{
  "readingId": "uuid-v4",
  "patientId": "uuid-v4",
  "heartRate": 72,
  "systolicBp": 120,
  "diastolicBp": 80,
  "oxygenSaturation": 98.5,
  "temperatureCelsius": 36.8,
  "timestamp": "2025-01-01T10:00:00Z",
  "submittedAt": "2025-01-01T10:00:00.123Z"
}
```

### 4.3 Message Schema — vitals-alerts

```json
{
  "alertId": "uuid-v4",
  "readingId": "uuid-v4",
  "patientId": "uuid-v4",
  "alertType": "HIGH_HEART_RATE",
  "value": 165,
  "threshold": 150,
  "severity": "WARNING",
  "triggeredAt": "2025-01-01T10:00:00.456Z"
}
```

---

## 5. Abnormality Rules

A reading is **abnormal** if any single field breaches a threshold. Multiple breaches in one reading produce multiple alert events.

| Metric | Warning threshold | Critical threshold | Alert type |
|---|---|---|---|
| Heart rate | > 100 or < 50 bpm | > 150 or < 40 bpm | `HIGH_HEART_RATE` / `LOW_HEART_RATE` |
| Systolic BP | > 140 or < 90 mmHg | > 180 or < 70 mmHg | `HIGH_SYSTOLIC_BP` / `LOW_SYSTOLIC_BP` |
| Diastolic BP | > 90 or < 60 mmHg | > 120 or < 40 mmHg | `HIGH_DIASTOLIC_BP` / `LOW_DIASTOLIC_BP` |
| Oxygen saturation | < 95 % | < 90 % | `LOW_OXYGEN_SATURATION` |
| Temperature | > 37.5 or < 36.0 °C | > 39.0 or < 35.0 °C | `HIGH_TEMPERATURE` / `LOW_TEMPERATURE` |

**Severity levels:**

| Level | Meaning |
|---|---|
| `WARNING` | Single threshold breached — monitor closely |
| `CRITICAL` | Critical threshold breached — immediate attention |

---

## 6. MongoDB Collections

### 6.1 `vitals` collection

Stores every reading that the consumer successfully processes.

```json
{
  "_id": "readingId (uuid-v4, unique index)",
  "patientId": "uuid-v4",
  "heartRate": 72,
  "systolicBp": 120,
  "diastolicBp": 80,
  "oxygenSaturation": 98.5,
  "temperatureCelsius": 36.8,
  "timestamp": "2025-01-01T10:00:00Z",
  "submittedAt": "2025-01-01T10:00:00.123Z",
  "processedAt": "2025-01-01T10:00:00.789Z",
  "abnormal": true
}
```

**Indexes:**
- `_id` (readingId) — unique, used for idempotency
- `patientId` — for future patient-level queries
- TTL on `processedAt` — documents expire after 90 days

### 6.2 `alerts` collection

Stores every alert event raised.

```json
{
  "_id": "alertId (uuid-v4)",
  "readingId": "uuid-v4",
  "patientId": "uuid-v4",
  "alertType": "HIGH_HEART_RATE",
  "value": 165,
  "threshold": 150,
  "severity": "CRITICAL",
  "triggeredAt": "2025-01-01T10:00:00.456Z"
}
```

### 6.3 `audit` collection

Append-only. Records who submitted what and when.

```json
{
  "_id": "uuid-v4",
  "readingId": "uuid-v4",
  "patientId": "uuid-v4",
  "submittedBy": "vitals-user",
  "sourceIp": "192.168.1.1",
  "submittedAt": "2025-01-01T10:00:00.123Z"
}
```

---

## 7. Consumer Behaviour

### 7.1 Retry and dead-letter policy

| Attempt | Delay | Action |
|---|---|---|
| 1 | immediate | Process normally |
| 2 | 500 ms | Retry |
| 3 | 1 000 ms | Retry |
| 4 | 2 000 ms | Retry |
| 5+ | — | Route to `vitals-dlt`, log error with readingId |

### 7.2 Idempotency

Before writing to MongoDB, the consumer checks whether a document with the same `readingId` already exists. If it does, the message is acknowledged and skipped. This makes re-delivery safe.

### 7.3 Delivery guarantee

**At-least-once delivery.** Offsets are committed only after a successful MongoDB write. A consumer crash before commit causes re-delivery, which the idempotency check handles safely.

---

## 8. Non-Functional Requirements

| Concern | Requirement |
|---|---|
| Secrets | No credentials in source code or git history. All secrets via environment variables. |
| Logging | Structured JSON logs. No patient-identifiable data (names, ID numbers) in log output. `patientId` (UUID) is acceptable. |
| Health | Both services expose `/actuator/health` |
| Data retention | Readings expire after 90 days (MongoDB TTL index) |
| Compliance | Aligns with Ghana Data Protection Act (Act 843): data minimisation, security safeguards, audit trail, and no sensitive data in logs |

---

## 9. Out of Scope (v1)

- Real patient data
- SMS or email alert delivery
- User management or role-based access
- Dashboard UI
- Multi-tenancy
