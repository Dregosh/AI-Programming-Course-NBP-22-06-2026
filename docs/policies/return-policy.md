# Return Policy (Example Starter Document)

> **Status:** Example seed document for the Hardware Service Decision Copilot MVP.
> Encodes the rules the decision agent must apply for **RETURN** requests.
> Replace with the real company policy before any production use.

---

## 1. Scope

This policy governs **voluntary returns** of electronics purchased from the company,
where the customer wishes to send the item back for a refund **without claiming a fault**.
Fault-based claims are handled by the separate **Complaint Policy**.

## 2. Eligibility Window

- Returns are accepted within **30 calendar days** of the purchase date.
- Requests submitted after day 30 are **not eligible** and must be REJECTED.
- If the purchase date is missing or in the future, the case must be ESCALATED.

## 3. Condition Requirements (Resalable Standard)

A returned item qualifies for refund **only if it can be resold as new**. The item must show:

- **No signs of use**: no scratches, scuffs, wear marks, fingerprints, dust ingress, or residue.
- **No physical damage**: no cracks, dents, bent parts, broken ports, or cosmetic defects.
- **No modification**: no third-party stickers, engravings, or altered components.
- Original condition consistent with an unopened or as-new product.

If the image shows **any** sign of use or damage, the return does **not** meet the resalable
standard and must be REJECTED (the customer may instead file a complaint if a fault exists).

## 4. Category Notes

- **Smartphone / Tablet / Laptop / Smartwatch**: screens and bodies must be free of scratches;
  no activation locks or personal accounts left signed in.
- **Headphones**: ear tips / cushions must be unused for hygiene reasons.
- Hygiene-sensitive accessories that show use are **not** returnable.

## 5. Decision Mapping

| Outcome | Condition |
|---|---|
| **APPROVE** | Within 30-day window **and** image shows no signs of use or damage (resalable). |
| **REJECT** | Outside window, **or** image shows any use/damage, **or** category-specific hygiene rule fails. |
| **ESCALATE** | Image inconclusive/blurry/wrong item, purchase date missing or invalid, or evidence is contradictory. |

## 6. Required Customer Disclosures

- An **APPROVE** outcome confirms the return is accepted and a refund will be processed.
- A **REJECT** or **ESCALATE** outcome is **preliminary** and is reviewed by a human specialist
  before becoming final.
