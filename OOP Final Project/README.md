# Vehicle Rental System

## Course Information
- Subject: Object-Oriented Programming
- School Year: 2023-2024
- Programming Language: Java

## Author
[KARL CATIPAN]
[23-03461]
[IT-2102]

## Project Description
A Java-based vehicle rental management system demonstrating object-oriented programming principles including:
- Inheritance and Polymorphism
- Abstraction and Encapsulation
- File I/O Operations
- Error Handling

## Features

### User Management
- User registration and login system
- Persistent user data storage in users.txt
- Admin and regular user roles

### Vehicle Types
- Cars (with optional AC)
- Motorcycles (with optional helmet rental)

### Admin Features
- View all vehicles and their rental status
- Add new vehicles (cars or motorcycles)
- Remove available vehicles
- Track which user has rented each vehicle

### User Features
- View available vehicles
- Rent vehicles with cost calculation
  - Base rental rate per day
  - Additional charges for AC (cars)
  - Optional helmet rental (motorcycles)
- Return rented vehicles
- Single active rental limit per user

## How to Use

### Installation
1. Download the source files:
   - `Main.java`
   - `users.txt`

2. Compile the program:
```bash
javac Main.java
```

3. Run the program
```
java Main
```

### Default Admin Account
- Username: admin
- Password: admin123

### Regular Users
1. Register a new account
2. Login with credentials
3. View and rent available vehicles
4. Return vehicles when done

## Technical Details
- Written in Java
- Uses file I/O for persistent user data
- Object-oriented design with inheritance
- Input validation and error handling
- Cross-platform console screen clearing

## Project Structure
- Main.java: Core program implementation
- users.txt: User credential storage