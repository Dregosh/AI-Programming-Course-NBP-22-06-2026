# Complaint Policy (Example Starter Document)

> **Status:** Example seed document for the Hardware Service Decision Copilot MVP.
> Encodes the rules the decision agent must apply for **COMPLAINT** (fault) requests.
> Replace with the real company policy before any production use.

---

## 1. Scope

This policy governs **complaints** where the customer reports that an item is **faulty,
defective, or damaged** and seeks repair, replacement, or refund under warranty.
Voluntary no-fault returns are handled by the separate **Return Policy**.

## 2. Eligibility Window

- Complaints are accepted within **24 months** of the purchase date (standard warranty period).
- Requests after day 730 are **not eligible** and must be REJECTED.
- If the purchase date is missing or in the future, the case must be ESCALATED.

## 3. Covered vs Not Covered

### Covered (defect — eligible for APPROVE)
- Manufacturing defects: dead pixels, battery failure, component failure, firmware faults.
- Damage that occurred without external impact and is consistent with a material/build fault.
- Functional failure under normal, intended use.

### Not Covered (customer-induced — eligible for REJECT)
- **Physical/impact damage**: cracked screen, dents, bent frame, broken ports from drops or pressure.
- **Liquid damage**: corrosion, water ingress marks, moisture indicators triggered.
- **Misuse / unauthorized repair**: third-party tampering, opened casing, non-approved parts.
- **Normal wear and tear**: cosmetic scuffs that do not affect function.

## 4. Evidence Assessment

The image is the primary evidence. The agent must judge:

1. **Is the item damaged, and how?** (type and location of damage)
2. **What is the most probable cause?** (manufacturing defect vs impact/liquid/misuse)
3. **Is that cause covered** under section 3?

The stated **reason for complaint** (mandatory text from the customer) must be weighed against
the visual evidence. If the stated reason contradicts the image, ESCALATE.

## 5. Decision Mapping

| Outcome | Condition |
|---|---|
| **APPROVE** | Within warranty window **and** damage/fault is consistent with a covered defect. |
| **REJECT** | Outside window, **or** damage is clearly customer-induced (impact/liquid/misuse/wear). |
| **ESCALATE** | Cause is ambiguous, image inconclusive/blurry/wrong item, stated reason contradicts the image, or date missing/invalid. |

## 6. Required Customer Disclosures

- An **APPROVE** outcome confirms the complaint is accepted and the service process begins.
- A **REJECT** or **ESCALATE** outcome is **preliminary** and is reviewed by a human specialist
  before becoming final.
