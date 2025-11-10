# CargoSync API Documentation

## Table of Contents

- [Authentication](#authentication)
- [Products](#products)
- [Inventory](#inventory)
- [Sales Orders](#sales-orders)
- [Purchase Orders](#purchase-orders)
- [Suppliers](#suppliers)
- [Warehouses](#warehouses)
- [Users](#users)
- [Simple Orders](#simple-orders)
- [Backorders](#backorders)

## Introduction

This document provides detailed information about the CargoSync API endpoints. The API follows RESTful principles and uses JSON for request and response payloads.

## Base URL

```
http://localhost:8080/api
```

## Common HTTP Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Authentication

### Login

```http
POST /api/auth/login
```

**Request Body:**

```json
{
  "email": "string",
  "password": "string"
}
```

**Response:**

```json
{
  "token": "string",
  "type": "Bearer"
}
```

### Register

```http
POST /api/auth/register
```

**Request Body:**

```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "string"
}
```

## Products

### Get All Products

```http
GET /api/products
```

### Get Product by ID

```http
GET /api/products/{id}
```

### Create Product

```http
POST /api/products
```

**Request Body:**

```json
{
  "name": "string",
  "description": "string",
  "price": "number",
  "category": "string"
}
```

### Update Product

```http
PUT /api/products/{id}
```

### Delete Product

```http
DELETE /api/products/{id}
```

## Inventory

### Get Inventory Status

```http
GET /api/inventory
```

### Get Inventory by ID

```http
GET /api/inventory/{id}
```

### Update Inventory

```http
PUT /api/inventory/{id}
```

**Request Body:**

```json
{
  "quantity": "number",
  "location": "string"
}
```

## Sales Orders

### Get All Sales Orders

```http
GET /api/sales-orders
```

### Get Sales Order by ID

```http
GET /api/sales-orders/{id}
```

### Create Sales Order

```http
POST /api/sales-orders
```

**Request Body:**

```json
{
  "customerId": "string",
  "items": [
    {
      "productId": "string",
      "quantity": "number"
    }
  ]
}
```

### Update Sales Order

```http
PUT /api/sales-orders/{id}
```

### Delete Sales Order

```http
DELETE /api/sales-orders/{id}
```

## Purchase Orders

### Get All Purchase Orders

```http
GET /api/purchase-orders
```

### Get Purchase Order by ID

```http
GET /api/purchase-orders/{id}
```

### Create Purchase Order

```http
POST /api/purchase-orders
```

**Request Body:**

```json
{
  "supplierId": "string",
  "items": [
    {
      "productId": "string",
      "quantity": "number",
      "unitPrice": "number"
    }
  ]
}
```

### Update Purchase Order

```http
PUT /api/purchase-orders/{id}
```

### Delete Purchase Order

```http
DELETE /api/purchase-orders/{id}
```

## Suppliers

### Get All Suppliers

```http
GET /api/suppliers
```

### Get Supplier by ID

```http
GET /api/suppliers/{id}
```

### Create Supplier

```http
POST /api/suppliers
```

**Request Body:**

```json
{
  "name": "string",
  "email": "string",
  "phone": "string",
  "address": "string"
}
```

### Update Supplier

```http
PUT /api/suppliers/{id}
```

### Delete Supplier

```http
DELETE /api/suppliers/{id}
```

## Warehouses

### Get All Warehouses

```http
GET /api/warehouses
```

### Get Warehouse by ID

```http
GET /api/warehouses/{id}
```

### Create Warehouse

```http
POST /api/warehouses
```

**Request Body:**

```json
{
  "name": "string",
  "location": "string",
  "capacity": "number"
}
```

### Update Warehouse

```http
PUT /api/warehouses/{id}
```

### Delete Warehouse

```http
DELETE /api/warehouses/{id}
```

## Users

### Get All Users

```http
GET /api/users
```

### Get User by ID

```http
GET /api/users/{id}
```

### Create User

```http
POST /api/users
```

**Request Body:**

```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "string"
}
```

### Update User

```http
PUT /api/users/{id}
```

### Delete User

```http
DELETE /api/users/{id}
```

## Simple Orders

### Get All Simple Orders

```http
GET /api/simple-orders
```

### Get Simple Order by ID

```http
GET /api/simple-orders/{id}
```

### Create Simple Order

```http
POST /api/simple-orders
```

**Request Body:**

```json
{
  "orderId": "string",
  "items": [
    {
      "productId": "string",
      "quantity": "number"
    }
  ]
}
```

## Backorders

### Get All Backorders

```http
GET /api/backorders
```

### Get Backorder by ID

```http
GET /api/backorders/{id}
```

### Create Backorder

```http
POST /api/backorders
```

**Request Body:**

```json
{
  "orderId": "string",
  "reason": "string",
  "expectedDate": "string"
}
```

### Update Backorder

```http
PUT /api/backorders/{id}
```

### Delete Backorder

```http
DELETE /api/backorders/{id}
```

## Authentication

All endpoints except for `/api/auth/login` and `/api/auth/register` require authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your_token>
```

## Error Responses

When an error occurs, the API will return a JSON response with an error message:

```json
{
  "timestamp": "2025-11-10T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/endpoint"
}
```


## Support

For API support or questions, please contact:

- Email: contact.abdelkebir@gmail.com
- Documentation Repository: https://github.com/Black0list/CargoSync
