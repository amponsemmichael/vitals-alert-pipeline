# Vitals Alert Pipeline

> Event-driven healthcare vitals monitoring — Spring Boot · Kafka · MongoDB · Docker Compose

*Architecture diagram, deployment details, and security section added in Session 12.*

---

## Repo Structure

```
vitals-alert-pipeline/
├── vitals-api/          # Spring Boot REST API + Kafka producer
├── vitals-consumer/     # Kafka consumer + MongoDB persistence + alerting
├── docker-compose.yml   # Base local stack
├── SPEC.md              # Full system specification
├── AI_REVIEW.md         # AI usage log and corrections
├── RUNBOOK.md           # Deliberate failure scenarios and fixes
└── README.md            # This file
```

---

## Quick Start

*Populated in Session 6 once Docker Compose is working end-to-end.*

---

## Architecture

*Diagram added in Session 12.*

---

## Security

*Section added in Session 10.*

---

## Observability

*Screenshots of Grafana dashboards and CloudWatch added in Session 9.*

---

## Compliance Note

This system is designed with alignment to Ghana's Data Protection Act (Act 843) in mind. See `SPEC.md` section 8 for details.
