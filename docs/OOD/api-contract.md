# Nortada — API Contract (OOD)

REST Level 3 (HATEOAS) API contract for the Phase 1 backend. All responses use the
`application/hal+json` media type and every resource carries a `_links` section
(hypermedia). This contract realises **US011** (beach list) and **US012** (beach detail);
the design classes behind it are in `docs/OOD/nortada-OOD.puml` and the request flows in
`docs/OOD/sequences/beach-request-flows.puml`.

- Base path: `/api`
- Media type: `application/hal+json`
- Nortada status enum: `NONE`, `LIGHT`, `MODERATE`, `STRONG`, `SEVERE` (see US010)

---

## 1. `GET /api/beaches` — Beach list

Returns all beaches with their current Nortada status, paginated.

| | |
|---|---|
| Method | `GET` |
| Path | `/api/beaches` |
| Produces | `application/hal+json` |

### Query parameters

| Name | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | Zero-based page index. |
| `size` | int | `20` | Page size (max 100). |
| `sort` | string | `name,asc` | Sort field and direction (optional). |

### Status codes

| Code | When |
|---|---|
| `200 OK` | Beaches returned (possibly an empty page). |
| `400 Bad Request` | Invalid pagination parameters. |
| `500 Internal Server Error` | Unexpected server failure. |

### Response body (200) — `BeachListResponse`

Collection resource in HAL: beaches under `_embedded.beaches`, page metadata under
`page`, and pagination hypermedia under `_links` (`self`, `first`, `prev`, `next`,
`last`). Each embedded beach carries its own `_links` (`self`, `collection`).

```json
{
  "_embedded": {
    "beaches": [
      {
        "id": "6f1c2e9a-4b2d-4f3a-9c1e-2a7b8c9d0e11",
        "name": "Praia de Matosinhos",
        "region": "Norte",
        "nortadaStatus": "MODERATE",
        "_links": {
          "self": { "href": "https://api.nortada.pt/api/beaches/6f1c2e9a-4b2d-4f3a-9c1e-2a7b8c9d0e11" },
          "collection": { "href": "https://api.nortada.pt/api/beaches" }
        }
      },
      {
        "id": "8a2d3f0b-5c3e-4a4b-8d2f-3b8c9d0e1f22",
        "name": "Praia da Costa Nova",
        "region": "Centro",
        "nortadaStatus": "STRONG",
        "_links": {
          "self": { "href": "https://api.nortada.pt/api/beaches/8a2d3f0b-5c3e-4a4b-8d2f-3b8c9d0e1f22" },
          "collection": { "href": "https://api.nortada.pt/api/beaches" }
        }
      }
    ]
  },
  "page": {
    "size": 20,
    "totalElements": 42,
    "totalPages": 3,
    "number": 1
  },
  "_links": {
    "self":  { "href": "https://api.nortada.pt/api/beaches?page=1&size=20" },
    "first": { "href": "https://api.nortada.pt/api/beaches?page=0&size=20" },
    "prev":  { "href": "https://api.nortada.pt/api/beaches?page=0&size=20" },
    "next":  { "href": "https://api.nortada.pt/api/beaches?page=2&size=20" },
    "last":  { "href": "https://api.nortada.pt/api/beaches?page=2&size=20" }
  }
}
```

Notes:
- `prev` is omitted on the first page; `next` is omitted on the last page.
- Empty collections still return `200 OK` with an empty `_embedded.beaches` array and
  `self`/`first`/`last` links.

---

## 2. `GET /api/beaches/{id}` — Beach detail

Returns a single beach with its current Nortada status.

| | |
|---|---|
| Method | `GET` |
| Path | `/api/beaches/{id}` |
| Produces | `application/hal+json` |

### Path parameters

| Name | Type | Description |
|---|---|---|
| `id` | UUID | Beach identifier. |

### Status codes

| Code | When |
|---|---|
| `200 OK` | Beach found and returned. |
| `404 Not Found` | No beach with the given `id`. |
| `500 Internal Server Error` | Unexpected server failure. |

### Response body (200) — `BeachResponse`

Item resource with a `_links` section carrying `self` and `collection`.

```json
{
  "id": "6f1c2e9a-4b2d-4f3a-9c1e-2a7b8c9d0e11",
  "name": "Praia de Matosinhos",
  "region": "Norte",
  "nortadaStatus": "MODERATE",
  "reading": {
    "windSpeed": 32.4,
    "windDirection": 340.0,
    "temperature": 19.5,
    "fetchedAt": "2026-07-20T09:00:00Z"
  },
  "_links": {
    "self":       { "href": "https://api.nortada.pt/api/beaches/6f1c2e9a-4b2d-4f3a-9c1e-2a7b8c9d0e11" },
    "collection": { "href": "https://api.nortada.pt/api/beaches" }
  }
}
```

### Response body (404) — problem detail

```json
{
  "type": "https://api.nortada.pt/problems/beach-not-found",
  "title": "Beach not found",
  "status": 404,
  "detail": "No beach exists with id 6f1c2e9a-4b2d-4f3a-9c1e-2a7b8c9d0e11"
}
```

---

## 3. Link relations

| Relation | Present on | Meaning |
|---|---|---|
| `self` | every resource | Canonical URI of this resource. |
| `collection` | each beach item | Back-link to `/api/beaches`. |
| `first` / `prev` / `next` / `last` | list resource | Pagination navigation. |

## 4. Request/response modelling

Requests carry no body (both endpoints are `GET`). Responses are `web/dto` HAL+JSON DTOs
(`BeachResponse`, `BeachListResponse`, `WeatherReadingResponse`), produced by
`BeachDtoMapper` from an application `BeachStatusView`. DTOs are distinct from both the
`domain` objects and the JPA `@Entity` data models (three-model separation,
`docs/architecture.md` section 3) — a domain object is never serialised straight to the
wire.
