# API Gateway — Full API Tester

## 🎯 Purpose

An interactive HTML tool for testing all available API Gateway endpoints of the parking-system.

## 🚀 Features

### ✅ What can be tested:

#### 🔐 Authentication
- Login (obtain JWT token)
- Logout (clear token)
- Automatic token saving to localStorage

#### 👥 Client API (5 endpoints)
1. **GET** `/api/clients` — List all clients
2. **POST** `/api/clients` — Create a new client
3. **GET** `/api/clients/{id}` — Get client by ID
4. **PUT** `/api/clients/{id}` — Update client data
5. **GET** `/api/clients/search?phone=...` — Search by phone number

#### 🚗 Vehicle API (6 endpoints)
6. **GET** `/api/vehicles` — List all vehicles
7. **POST** `/api/vehicles` — Register a new vehicle
8. **GET** `/api/vehicles/{id}` — Get vehicle by ID
9. **PUT** `/api/vehicles/{id}` — Update vehicle data
10. **DELETE** `/api/vehicles/{id}` — Delete a vehicle
11. **GET** `/api/clients/{clientId}/vehicles` — Get vehicles of a client
12. **POST** `/api/clients/{clientId}/vehicles` — Add a vehicle to a client

#### ⚡ Quick Test
- Automated sequential test of all 11 endpoints
- Progress bar with current status
- Detailed statistics and results

## 🎨 Interface

### Tabs:
1. **🔐 Login** — Authentication
2. **👥 Clients** — Client operations
3. **🚗 Vehicles** — Vehicle operations
4. **⚡ Quick Test** — Automated full endpoint test

### Colour coding:
- 🟦 **Blue** — GET requests
- 🟩 **Green** — POST requests
- 🟧 **Orange** — PUT requests
- 🟥 **Red** — DELETE requests

## 📖 How to Use

### 1. Start the system
```powershell
cd C:\Users\user\Projects\parking-system\devops
.\full-rebuild.ps1
```

### 2. Open the file in a browser
```
C:\Users\user\Projects\parking-system\devops\test-login.html
```
Or simply double-click the file.

### 3. Log in
- **Tab:** 🔐 Login
- **Username:** `admin`
- **Password:** `parking123`
- Click **Login**

### 4. Test endpoints

#### Option A: Manual testing
1. Go to the required tab (Clients/Vehicles)
2. Fill in the required fields
3. Click the button for the required method (GET/POST/PUT/DELETE)
4. The result will appear at the bottom of the page

#### Option B: Automated testing
1. Go to the **⚡ Quick Test** tab
2. Click **"Run Complete API Test"**
3. Watch progress (11 tests)
4. View detailed statistics

## 📊 Usage Examples

### Create a client
```
Tab: 👥 Clients
Section: Create Client
Steps:
1. Edit the JSON if needed
2. Click [POST] Create Client
```

### Search client by phone
```
Tab: 👥 Clients
Section: Search by Phone
Steps:
1. Enter number: +380501234567
2. Click [GET] Search by Phone
```

### Add a vehicle to a client
```
Tab: 🚗 Vehicles
Section: Add Vehicle to Client
Steps:
1. Client ID: 1
2. License Plate: AA1234BB
3. Click [POST] Add Vehicle to Client
```

### Full API test
```
Tab: ⚡ Quick Test
Steps:
1. Click "Run Complete API Test"
2. Wait for completion
3. Check results
```

## 🎯 Features

### ✨ Smart capabilities:
- **Auto-save token** — no need to log in repeatedly
- **Validation** — checks that required fields are filled
- **JSON formatting** — pretty-printed response output
- **Colour coding** — easy to distinguish request types
- **Authentication status** — always visible whether you are logged in

### 🔒 Security:
- Tokens are stored only in the browser's localStorage
- Tokens are not displayed in full (only the beginning)
- Easy to log out and clear tokens

### 📱 Responsiveness:
- Works on all screen sizes
- Responsive grid for forms
- Convenient scrolling for large responses

## 🔧 Troubleshooting

### Problem: "Please login first"
**Fix:** Go to the Login tab and authenticate

### Problem: "Connection refused" or "Failed to fetch"
**Fix:**
1. Make sure the system is running: `docker ps`
2. Check Gateway availability: http://localhost:8086
3. Check logs: `docker logs api-gateway --tail 50`

### Problem: 401 Unauthorized
**Fix:**
1. Token has expired — log in again
2. Use the correct credentials

### Problem: 404 Not Found
**Fix:**
1. Check the object ID (does it exist?)
2. Create the object before retrieving/updating it

### Problem: 409 Conflict
**Fix:**
- An object with this license_plate or phone already exists
- Use unique values

## 📝 Data Formats

### Client JSON:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+380501234567",
  "email": "john.doe@example.com"
}
```

### Vehicle JSON (Create):
```json
{
  "licensePlate": "AA1234BB",
  "clientId": 1,
  "isAllowed": true
}
```

### Vehicle JSON (Update):
```json
{
  "licensePlate": "BB5678CC",
  "isAllowed": false
}
```

## 🎓 Tips

1. **Use Quick Test** before committing — make sure everything works
2. **Save IDs** of created objects for subsequent operations
3. **Validate JSON** before sending — valid format is mandatory
4. **Log out and log in** if something is not working
5. **Check the logs** in results — all error information is there

## 🔄 Changelog

### Version 2.0 (2026-01-04)
- ✅ Added all Client API endpoints
- ✅ Added all Vehicle API endpoints
- ✅ Added Quick Test for automated testing
- ✅ Improved design with tabs
- ✅ Added colour coding for request methods
- ✅ Added authentication status indicator
- ✅ Improved error handling
- ✅ Added progress bar for Quick Test

### Version 1.0 (2025-12-23)
- Basic login functionality
- Simple GET /api/clients test

## 🌐 Related Files

- **full-rebuild.ps1** — Full system rebuild
- **check-system.ps1** — Check service status

## 📊 Statistics

- **Total endpoints:** 12 (+ 1 login)
- **Client API:** 5 endpoints
- **Vehicle API:** 7 endpoints
- **Quick Test:** 11 automated tests

---

**Author:** Parking System Team  
**Version:** 2.0  
**Date:** 2026-01-04
