# ANIS Backend API Reference — Child App

Base URL: `https://api.anis.solutions/api/v1/`
Source: Apidog docs (project ID `762422`)

## Status Legend

| Icon | Meaning |
|------|---------|
| ✅ | Implemented in child app |
| ❌ | Not yet implemented |
| ➖ | Not in Apidog docs (added manually) |

## Auth

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/auth/register` | ❌ | register |
| POST | `/auth/login` | ❌ | login |
| POST | `/auth/logout` | ❌ | logout |
| POST | `/auth/generate-otp` | ❌ | generate-otp |
| POST | `/auth/verify-otp` | ❌ | verify-otp |
| POST | `/auth/refresh-token` | ❌ | refresh-token |
| POST | `/auth/password/forgot` | ❌ | forget-password |
| PATCH | `/auth/password/reset/{token}` | ❌ | reset-password |

## Parent

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| PATCH | `/parents/me` | ❌ | update-profile |
| PATCH | `/parents/me/password` | ❌ | change-password |
| GET | `/parents/reactivate` | ❌ | reactivate request |
| GET | `/parents/me` | ❌ | me |
| DELETE | `/parents/deactivate` | ❌ | deactivate |
| POST | `/parents/reactivate/{token}` | ❌ | reactivate |

## Locations

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/locations/{childId}` | ❌ | Add |
| PATCH | `/locations/{childId}/{locId}` | ❌ | Update |
| GET | `/locations/{childId}/{locId}` | ❌ | Get |
| GET | `/locations/{childId}` | ❌ | Get All |
| DELETE | `/locations/{childId}/{locId}` | ❌ | Delete |
| POST | `/locations/telemetry/{childId}` | ✅ | Track Location |
| GET | `/locations/stream/{childId}` | ❌ | Track Location |

## Apps

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| | *Readme* | | |
| POST | `/apps/` | ❌ | Add App |
| DELETE | `/apps/{packageId}/children/{childId}` | ❌ | Delete App |
| POST | `/apps/add-bulk` | ✅ | add bulk |
| PATCH | `/apps/{packageId}/children/{childId}/block` | ❌ | Block App |
| PATCH | `/apps/{packageId}/children/{childId}/limit` | ❌ | Limit App |
| GET | `/apps/children/{childId}` | ❌ | Get Child Apps |
| GET | `/apps/{packageId}/children/{childId}` | ❌ | Get Child App |
| POST | `/apps/usage/{packageId}` | ❌ | update usage |

## Child

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/children/` | ❌ | addChild |
| GET | `/children/` | ❌ | getMyChildern |
| GET | `/children/{childId}` | ❌ | getMyChild |
| PATCH | `/children/{childId}` | ❌ | update child |
| POST | `/children/pair` | ✅ | pairing |
| GET | `/children/me` | ✅ | me |

## Quests

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/children/{childId}/quests/` | ❌ | Add Quest |
| PATCH | `/children/{childId}/quests/{questId}` | ✅ | Update Quest |
| PATCH | `/children/{childId}/quests/{questId}/start` | ❌ | Start Quest |
| PATCH | `/children/{childId}/quests/{questId}/cancel` | ❌ | Cancel Quest |
| PATCH | `/children/{childId}/quests/{questId}/complete` | ❌ | Complete Quest |
| PATCH | `/children/{childId}/quests/{questId}/stop` | ❌ | Stop Quest |
| GET | `/children/{childId}/quests/{questId}` | ❌ | Get Quest |
| GET | `/children/{childId}/quests` | ✅ | Get All Quest |

## Rewards

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/children/{childId}/rewards` | ❌ | Add Reward |
| PATCH | `/children/{childId}/rewards/{rewardId}` | ✅ | Update Reward |
| DELETE | `/children/{childId}/rewards/{rewardId}` | ❌ | Get Reward |
| POST | `/children/{childId}/rewards/{rewardId}/redeem` | ❌ | Redeem Reward |
| GET | `/children/{childId}/rewards` | ✅ | Get ALL Reward |
| GET | `/children/{childId}/rewards/{rewardId}` | ❌ | Get Reward |

## screencast

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| | *🔌 parent* | | |
| | *🔌 Child* | | |

## Additional Endpoints (Not in Apidog)

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/children/fcm-token` | ✅ | Register FCM push token — implemented in `ApiService.kt` but not in Apidog docs |

---
## Detailed Endpoint Specifications

### POST /auth/register

**Name:** register
**Status:** Not implemented
**Folder:** Auth

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string",
      "format": "email"
    },
    "password": {
      "type": "string",
      "format": "password"
    },
    "confirmPassword": {
      "type": "string"
    },
    "phone": {
      "type": "string"
    },
    "firstName": {
      "type": "string"
    },
    "lastName": {
      "type": "string"
    },
    "birthDate": {
      "type": "string"
    }
  },
  "required": [
    "email",
    "password",
    "confirmPassword",
    "phone",
    "firstName",
    "lastName",
    "birthDate"
  ],
  "x-apidog-orders": [
    "email",
    "password",
    "confirmPassword",
    "phone",
    "firstName",
    "lastName",
    "birthDate"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### POST /auth/login

**Name:** login
**Status:** Not implemented
**Folder:** Auth

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string"
    },
    "password": {
      "type": "string"
    }
  },
  "required": [
    "email",
    "password"
  ],
  "x-apidog-orders": [
    "email",
    "password"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### POST /auth/logout

**Name:** logout
**Status:** Not implemented
**Folder:** Auth

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### POST /auth/generate-otp

**Name:** generate-otp
**Status:** Not implemented
**Folder:** Auth

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string",
      "x-apidog-mock": "{{$internet.email}}",
      "description": "user email"
    },
    "reason": {
      "title": "emailReason",
      "description": "email reason",
      "$ref": "#/definitions/14081884"
    }
  },
  "x-apidog-orders": [
    "email",
    "reason"
  ],
  "required": [
    "email",
    "reason"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### POST /auth/verify-otp

**Name:** verify-otp
**Status:** Not implemented
**Folder:** Auth

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string"
    },
    "otp": {
      "type": "string"
    },
    "reason": {
      "$ref": "#/definitions/14081884"
    }
  },
  "x-apidog-orders": [
    "email",
    "otp",
    "reason"
  ],
  "required": [
    "email",
    "otp",
    "reason"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### POST /auth/refresh-token

**Name:** refresh-token
**Status:** Not implemented
**Folder:** Auth

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string"
    }
  },
  "required": [
    "email"
  ],
  "x-apidog-orders": [
    "email"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### POST /auth/password/forgot

**Name:** forget-password
**Status:** Not implemented
**Folder:** Auth

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string"
    }
  },
  "required": [
    "email"
  ],
  "x-apidog-orders": [
    "email"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### PATCH /auth/password/reset/{token}

**Name:** reset-password
**Status:** Not implemented
**Folder:** Auth

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `token` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "password": {
      "type": "string"
    },
    "confirmPassword": {
      "type": "string"
    }
  },
  "required": [
    "password",
    "confirmPassword"
  ],
  "x-apidog-orders": [
    "password",
    "confirmPassword"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### PATCH /parents/me

**Name:** update-profile
**Status:** Not implemented
**Folder:** Parent

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "password": {
      "type": "string"
    },
    "confirmPassword": {
      "type": "string"
    }
  },
  "required": [
    "password",
    "confirmPassword"
  ],
  "x-apidog-orders": [
    "password",
    "confirmPassword"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### PATCH /parents/me/password

**Name:** change-password
**Status:** Not implemented
**Folder:** Parent

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {},
  "x-apidog-orders": []
}
```

---

### GET /parents/reactivate

**Name:** reactivate request
**Status:** Not implemented
**Folder:** Parent

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string"
    }
  },
  "x-apidog-orders": [
    "email"
  ],
  "required": [
    "email"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /parents/me

**Name:** me
**Status:** Not implemented
**Folder:** Parent

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### DELETE /parents/deactivate

**Name:** deactivate
**Status:** Not implemented
**Folder:** Parent

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string"
    }
  },
  "x-apidog-orders": [
    "email"
  ],
  "required": [
    "email"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /parents/reactivate/{token}

**Name:** reactivate
**Status:** Not implemented
**Folder:** Parent

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `token` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string"
    }
  },
  "x-apidog-orders": [
    "email"
  ],
  "required": [
    "email"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /locations/{childId}

**Name:** Add
**Status:** Not implemented
**Folder:** Locations

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /locations/{childId}/{locId}

**Name:** Update
**Status:** Not implemented
**Folder:** Locations

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `locId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /locations/{childId}/{locId}

**Name:** Get
**Status:** Not implemented
**Folder:** Locations

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `locId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /locations/{childId}

**Name:** Get All
**Status:** Not implemented
**Folder:** Locations

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `coord` | query | string | No |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### DELETE /locations/{childId}/{locId}

**Name:** Delete
**Status:** Not implemented
**Folder:** Locations

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `locId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /locations/telemetry/{childId}

**Name:** Track Location
**Status:** Implemented
**Folder:** Locations

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /locations/stream/{childId}

**Name:** Track Location
**Status:** Not implemented
**Folder:** Locations

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /apps/

**Name:** Add App
**Status:** Not implemented
**Folder:** Apps

**Request Body:**
```json
{
  "$ref": "#/definitions/12762260"
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### DELETE /apps/{packageId}/children/{childId}

**Name:** Delete App
**Status:** Not implemented
**Folder:** Apps

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `packageId` | path | string | Yes |  |
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /apps/add-bulk

**Name:** add bulk
**Status:** Implemented
**Folder:** Apps

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /apps/{packageId}/children/{childId}/block

**Name:** Block App
**Status:** Not implemented
**Folder:** Apps

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `packageId` | path | string | Yes |  |
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /apps/{packageId}/children/{childId}/limit

**Name:** Limit App
**Status:** Not implemented
**Folder:** Apps

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `packageId` | path | string | Yes |  |
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /apps/children/{childId}

**Name:** Get Child Apps
**Status:** Not implemented
**Folder:** Apps

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /apps/{packageId}/children/{childId}

**Name:** Get Child App
**Status:** Not implemented
**Folder:** Apps

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `packageId` | path | string | Yes |  |
| `childId` | path | string | Yes |  |

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /apps/usage/{packageId}

**Name:** update usage
**Status:** Not implemented
**Folder:** Apps

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `packageId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /children/

**Name:** addChild
**Status:** Not implemented
**Folder:** Child

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "firstName": {
      "type": "string",
      "x-apidog-mock": "ahmed"
    },
    "lastName": {
      "type": "string",
      "x-apidog-mock": "abbas"
    },
    "gender": {
      "type": "boolean",
      "x-apidog-mock": "true"
    },
    "hobbies": {
      "type": "array",
      "items": {
        "type": "string",
        "x-apidog-mock": "swim"
      }
    },
    "dob": {
      "type": "string",
      "x-apidog-mock": "11/05/2014"
    }
  },
  "x-apidog-orders": [
    "firstName",
    "lastName",
    "gender",
    "hobbies",
    "dob"
  ],
  "required": [
    "firstName",
    "lastName",
    "gender",
    "hobbies",
    "dob"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /children/

**Name:** getMyChildern
**Status:** Not implemented
**Folder:** Child

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /children/{childId}

**Name:** getMyChild
**Status:** Not implemented
**Folder:** Child

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /children/{childId}

**Name:** update child
**Status:** Not implemented
**Folder:** Child

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /children/pair

**Name:** pairing
**Status:** Implemented
**Folder:** Child

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /children/me

**Name:** me
**Status:** Implemented
**Folder:** Child

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /children/{childId}/quests/

**Name:** Add Quest
**Status:** Not implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /children/{childId}/quests/{questId}

**Name:** Update Quest
**Status:** Implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `questId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /children/{childId}/quests/{questId}/start

**Name:** Start Quest
**Status:** Not implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `questId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /children/{childId}/quests/{questId}/cancel

**Name:** Cancel Quest
**Status:** Not implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `questId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /children/{childId}/quests/{questId}/complete

**Name:** Complete Quest
**Status:** Not implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `questId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /children/{childId}/quests/{questId}/stop

**Name:** Stop Quest
**Status:** Not implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `questId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /children/{childId}/quests/{questId}

**Name:** Get Quest
**Status:** Not implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `questId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /children/{childId}/quests

**Name:** Get All Quest
**Status:** Implemented
**Folder:** Quests

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /children/{childId}/rewards

**Name:** Add Reward
**Status:** Not implemented
**Folder:** Rewards

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "description": "name of reward."
    }
  },
  "x-apidog-orders": [
    "name"
  ],
  "required": [
    "name"
  ]
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### PATCH /children/{childId}/rewards/{rewardId}

**Name:** Update Reward
**Status:** Implemented
**Folder:** Rewards

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `rewardId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### DELETE /children/{childId}/rewards/{rewardId}

**Name:** Get Reward
**Status:** Not implemented
**Folder:** Rewards

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `rewardId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### POST /children/{childId}/rewards/{rewardId}/redeem

**Name:** Redeem Reward
**Status:** Not implemented
**Folder:** Rewards

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `rewardId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /children/{childId}/rewards

**Name:** Get ALL Reward
**Status:** Implemented
**Folder:** Rewards

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

### GET /children/{childId}/rewards/{rewardId}

**Name:** Get Reward
**Status:** Not implemented
**Folder:** Rewards

**Parameters:**

| Name | In | Type | Required | Description |
|------|----|------|----------|-------------|
| `childId` | path | string | Yes |  |
| `rewardId` | path | string | Yes |  |

**Request Body:**
```json
{
  "type": "object",
  "properties": {}
}
```

**Response 200:** 
```json
{
  "type": "object",
  "properties": {}
}
```

---

