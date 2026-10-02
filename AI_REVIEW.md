# AI Review Log

This document records every place where AI-generated code or configuration was used, what it got wrong or left insecure, and what was done to fix it. It is updated continuously throughout the build.

Maintained to satisfy the requirement in the job posting: *"demonstrate that you can use AI tools responsibly and review their output critically."*

---

## How AI Was Used

| Phase | What the AI did |
|---|---|
| Spec | Suggested initial field constraints and abnormality thresholds |
| Scaffold | Generated Spring Boot module structure, pom.xml files, and initial class stubs |
| Config | Suggested application.yml configuration for Kafka and MongoDB |
| Tests | Generated unit test skeletons for abnormality rules |

---

## Issues Found and Fixed

*Entries added as the build progresses. Minimum three required.*

---

### Issue 1 — Hardcoded credentials in application.yml

**What the AI produced:**
```yaml
spring:
  security:
    user:
      name: vitals-user
      password: secret123
```

**Why it was wrong:**
Plaintext credentials committed to source control are exposed to anyone with repo access. Rotating them requires a code change and a redeploy.

**Risk if shipped:**
Credential leak via git history. Any public repo exposure would immediately compromise the API.

**Fix applied:**
Both values resolved from environment variables at runtime:
```yaml
spring:
  security:
    user:
      name: ${API_USERNAME:vitals-user}
      password: ${API_PASSWORD}
```
`API_PASSWORD` has no default — the app fails to start if the variable is not set. Fail-fast is safer than silently running with a known password.

---

### Issue 2 — Missing `@Valid` on the request body

**What the AI produced:**
```java
public ResponseEntity<VitalsReadingResponse> submitReading(
        @RequestBody VitalsReadingRequest request) {
```

**Why it was wrong:**
Without `@Valid`, all the `@NotNull`, `@Min`, `@Max`, and `@PastOrPresent` annotations on `VitalsReadingRequest` are silently ignored. Any payload passes through, including negative heart rates, future timestamps, or missing fields.

**Risk if shipped:**
Invalid readings would be published to Kafka and stored in MongoDB with no error returned to the caller. Garbage data would trigger false alerts or corrupt the dataset.

**Fix applied:**
Added `@Valid` to the parameter and a `GlobalExceptionHandler` that maps `MethodArgumentNotValidException` to a structured 400 response with per-field error messages.

---

### Issue 3 — Kafka consumer with `enable-auto-commit: true`

**What the AI produced:**
```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: true
```

**Why it was wrong:**
Auto-commit advances the offset on a background timer regardless of whether the message was actually processed. A consumer crash between the auto-commit and the MongoDB write permanently loses that message — the offset is committed but the data was never stored.

**Risk if shipped:**
Silent data loss under any consumer failure. In a healthcare context this means missing vitals readings with no indication they were ever lost.

**Fix applied:**
Set `enable-auto-commit: false` and `ack-mode: MANUAL_IMMEDIATE`. The consumer calls `ack.acknowledge()` only after a successful MongoDB write. Offsets are committed only when data is confirmed persisted.

---

## Where AI Saved Time

*To be completed at the end of the project.*

---

## Where Human Review Was Essential

*To be completed at the end of the project.*
