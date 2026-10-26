# CivicEye AI - REST API Documentation

## Base URL
- **Local Backend:** `http://localhost:8080`

---

## 1. Authentication APIs
See [AuthController.java](file:///c:/Piyush%20Yadav/LinkCode/AI%20Innovation%20Competition/CivicEye%20AI/backend/src/main/java/com/civiceye/ai/auth/AuthController.java)
- `POST /api/auth/register` - Register a new citizen account
- `POST /api/auth/login` - Authenticate user credentials and return JWT bearer token

---

## 2. Complaint Registration APIs (Phase 3)
See [ComplaintController.java](file:///c:/Piyush%20Yadav/LinkCode/AI%20Innovation%20Competition/CivicEye%20AI/backend/src/main/java/com/civiceye/ai/complaint/ComplaintController.java)

### 2.1 Register New Civic Complaint (Multipart with Photo)
- **Endpoint:** `POST /api/complaints`
- **Security:** Bearer JWT (`ROLE_CITIZEN`, `ROLE_ADMIN`)
- **Content-Type:** `multipart/form-data`
- **Form Fields:**
  - `title` *(string, required, max 150 chars)*: Issue headline
  - `category` *(string, required)*: One of `ROADS_AND_FOOTPATHS`, `GARBAGE_AND_CLEANLINESS`, `WATER_SUPPLY_AND_LEAKAGE`, `DRAINAGE_AND_SEWAGE`, `STREETLIGHTS_AND_ELECTRICITY`, `PUBLIC_HEALTH_HAZARDS`, `OTHER`
  - `description` *(string, required)*: In-depth problem details
  - `location` *(string, required, max 255 chars)*: Landmark or street address
  - `latitude` *(number, optional)*: GPS latitude
  - `longitude` *(number, optional)*: GPS longitude
  - `image` *(file, optional)*: Evidence photo (JPG, PNG, WebP; max 10MB)
- **Response:** `201 Created`
```json
{
  "success": true,
  "message": "Complaint registered successfully",
  "data": {
    "id": 1,
    "complaintNumber": "CE-2026-0001",
    "title": "Massive Pothole on Sector 4",
    "description": "Dangerous deep pothole near crossroad",
    "category": "ROADS_AND_FOOTPATHS",
    "status": "SUBMITTED",
    "priority": "MEDIUM",
    "location": "Sector 4, Main Market Road",
    "latitude": 18.5204,
    "longitude": 73.8567,
    "imageUrl": "/api/files/a9b8c7-evidence.jpg",
    "citizenId": 2,
    "citizenName": "Jane Doe",
    "citizenEmail": "citizen@civiceye.com",
    "departmentId": null,
    "departmentName": null,
    "createdAt": "2026-10-13T10:30:00"
  }
}
```

### 2.2 Register Complaint (JSON without File)
- **Endpoint:** `POST /api/complaints`
- **Security:** Bearer JWT (`ROLE_CITIZEN`, `ROLE_ADMIN`)
- **Content-Type:** `application/json`
- **Payload:**
```json
{
  "title": "Damaged streetlight near park",
  "category": "STREETLIGHTS_AND_ELECTRICITY",
  "description": "Pole fixture broken and hanging dangerously",
  "location": "Central Park Gate 2",
  "latitude": 18.5204,
  "longitude": 73.8567
}
```

### 2.3 Get Current User's Complaints
- **Endpoint:** `GET /api/complaints/my`
- **Security:** Bearer JWT (`ROLE_CITIZEN`, `ROLE_ADMIN`)
- **Response:** `200 OK` with list of complaints filed by the authenticated user.

### 2.4 Get Complaint Details by ID
- **Endpoint:** `GET /api/complaints/{id}`
- **Security:** Bearer JWT
  - Citizens can only view their own complaints.
  - Department staff can view complaints assigned to their department.
  - Admins can view any complaint.
- **Response:** `200 OK`

### 2.5 Get Complaint Details by Tracking Number
- **Endpoint:** `GET /api/complaints/number/{complaintNumber}`
- **Example:** `GET /api/complaints/number/CE-2026-0001`
- **Security:** Bearer JWT (role boundary checked)
- **Response:** `200 OK`

---

## 3. Protected File Storage APIs
See [FileStorageController.java](file:///c:/Piyush%20Yadav/LinkCode/AI%20Innovation%20Competition/CivicEye%20AI/backend/src/main/java/com/civiceye/ai/storage/FileStorageController.java)

- `GET /api/files/{filename}` - Retrieve protected evidence photo with cache header and validated content-type.

---

## 4. Complaint Lifecycle, Assignment & Timeline APIs (Phase 4)
See [ComplaintController.java](file:///c:/Piyush%20Yadav/LinkCode/AI%20Innovation%20Competition/CivicEye%20AI/backend/src/main/java/com/civiceye/ai/complaint/ComplaintController.java) and [ComplaintLifecycleService.java](file:///c:/Piyush%20Yadav/LinkCode/AI%20Innovation%20Competition/CivicEye%20AI/backend/src/main/java/com/civiceye/ai/complaint/ComplaintLifecycleService.java)

### 4.1 Update Complaint Status
- **Endpoint:** `PUT /api/complaints/{id}/status`
- **Security:** Bearer JWT (`ROLE_DEPARTMENT_USER`, `ROLE_ADMIN`)
- **Content-Type:** `application/json` or `multipart/form-data`
- **Payload:**
```json
{
  "status": "IN_PROGRESS",
  "remarks": "Maintenance crew deployed on site",
  "resolutionImageUrl": null
}
```
- **Response:** `200 OK` with updated `ComplaintResponse`.

### 4.2 Assign Complaint to Department / Officer
- **Endpoint:** `PUT /api/complaints/{id}/assign`
- **Security:** Bearer JWT (`ROLE_DEPARTMENT_USER`, `ROLE_ADMIN`)
- **Content-Type:** `application/json`
- **Payload:**
```json
{
  "departmentId": 1,
  "assignedOfficerId": 3,
  "instructions": "Inspect road crack within 24 hours"
}
```
- **Response:** `200 OK` with updated `ComplaintResponse` (status automatically set to `ASSIGNED`).

### 4.3 Get Complaint Audit Timeline
- **Endpoint:** `GET /api/complaints/{id}/timeline`
- **Security:** Bearer JWT (`ROLE_CITIZEN`, `ROLE_DEPARTMENT_USER`, `ROLE_ADMIN`)
- **Response:** `200 OK` with chronological list of status transitions, actor information, remarks, and timestamps.

### 4.4 Get Department Assigned Complaints
- **Endpoint:** `GET /api/complaints/department/{departmentId}`
- **Security:** Bearer JWT (`ROLE_DEPARTMENT_USER`, `ROLE_ADMIN`)
- **Response:** `200 OK` with list of complaints assigned to the department.

