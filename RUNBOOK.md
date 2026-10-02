# Runbook

This runbook documents real failures that were deliberately introduced into the pipeline, the symptoms observed, the diagnosis process, and the fix applied. Every entry represents something that actually broke in this environment.

Maintained for two reasons: to prove operational instincts for the portfolio, and to serve as genuine first-response documentation for anyone running this system.

---

## How to Use This Runbook

1. Identify the symptom from the section headers below
2. Follow the diagnosis steps in order — they go from cheap (log check) to expensive (restart)
3. Apply the fix
4. If the fix doesn't work, check the "also check" notes at the bottom of each entry

---

## Entries

*Populated during Session 11 (Resilience and Chaos). Each entry follows the template below.*

---

### Template

**Scenario:**  
**How it was triggered:**  
**Symptoms:**  
**Diagnosis commands:**
```bash
# commands used
```
**Root cause:**  
**Fix:**  
**How to verify the fix worked:**  
**Also check:**  

---

### Entry 1 — Consumer crash loop

*To be filled in during Session 11.*

---

### Entry 2 — Kafka broker down

*To be filled in during Session 11.*

---

### Entry 3 — MongoDB primary stepped down

*To be filled in during Session 11.*

---

### Entry 4 — Disk full

*To be filled in during Session 11.*

---

### Entry 5 — Port conflict

*To be filled in during Session 11.*
