import java.util.*;
import java.io.*;

public class Main {
    // Abstract base class for all vehicles in the rental system
    static abstract class Vehicle {
        private String model;
        private String brand;
        private double rentalRate;
        private boolean isAvailable;
        private User renter;

        public Vehicle(String model, String brand, double rentalRate) {
            this.model = model;
            this.brand = brand;
            this.rentalRate = rentalRate;
            this.isAvailable = true;
            this.renter = null;
        }

        public abstract double calculateRentalCost(int days);

        public String getModel() { return model; }
        public String getBrand() { return brand; }
        public double getRentalRate() { return rentalRate; }
        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
        public User getRenter() { return renter; }
        public void setRenter(User renter) { this.renter = renter; }

        @Override
        public String toString() {
            return brand + " " + model + " (Rate: $" + rentalRate + "/day)";
        }
    }

    // Car implementation with AC feature
    static class Car extends Vehicle {
        private boolean hasAC;

        public Car(String model, String brand, double rentalRate, boolean hasAC) {
            super(model, brand, rentalRate);
            this.hasAC = hasAC;
        }

        @Override
        public double calculateRentalCost(int days) {
            double cost = getRentalRate() * days;
            if (hasAC) cost += 5 * days;
            return cost;
        }
    }

    // Motorcycle implementation with optional helmet rental
    static class Motorcycle extends Vehicle {
        private boolean hasHelmet;

        public Motorcycle(String model, String brand, double rentalRate, boolean hasHelmet) {
            super(model, brand, rentalRate);
            this.hasHelmet = hasHelmet;
        }

        @Override
        public double calculateRentalCost(int days) {
            return getRentalRate() * days;
        }

        public double getHelmetCost(int days) {
            return hasHelmet ? 2 * days : 0;
        }

        public boolean hasHelmet() {
            return hasHelmet;
        }
    }

    // User class managing authentication and rental status
    static class User {
        private String username;
        String password;
        private boolean hasActiveRental;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
            this.hasActiveRental = false;
        }

        public boolean hasActiveRental() { return hasActiveRental; }
        public void setActiveRental(boolean hasRental) { this.hasActiveRental = hasRental; }
        
        public boolean authenticate(String password) {
            return this.password.equals(password);
        }

        public String getUsername() { return username; }
    }

    // Core rental system managing vehicles, users, and rental operations
    static class RentalSystem {
        private List<Vehicle> vehicles;
        private Map<String, User> users;
        private User currentUser;
        private static final String USER_FILE = "users.txt";

        public RentalSystem() {
            vehicles = new ArrayList<>();
            users = new HashMap<>();
            loadUsers();
            vehicles.add(new Car("Civic", "Honda", 50.0, true));
            vehicles.add(new Car("Corolla", "Toyota", 45.0, true));
            vehicles.add(new Motorcycle("Ninja", "Kawasaki", 35.0, true));
            users.put("admin", new User("admin", "admin123"));
        }

        // Persistent storage operations
        private void loadUsers() {
            try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        users.put(parts[0], new User(parts[0], parts[1]));
                    }
                }
            } catch (IOException e) {
                System.out.println("Warning: Could not load users file.");
            }
        }

        private void saveUsers() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
                for (User user : users.values()) {
                    writer.write(user.getUsername() + ":" + user.password + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error: Could not save users to file.");
            }
        }

        // User management methods
        public void registerUser(String username, String password) {
            if (!users.containsKey(username)) {
                users.put(username, new User(username, password));
                saveUsers();
            }
        }

        public boolean isUserExists(String username) {
            return users.containsKey(username);
        }

        public boolean login(String username, String password) {
            User user = users.get(username);
            if (user != null && user.authenticate(password)) {
                currentUser = user;
                return true;
            }
            return false;
        }

        public boolean canRentVehicle() {
            return currentUser != null && !currentUser.hasActiveRental();
        }

        public List<Vehicle> getAvailableVehicles() {
            List<Vehicle> availableVehicles = new ArrayList<>();
            for (Vehicle v : vehicles) {
                if (v.isAvailable()) {
                    availableVehicles.add(v);
                }
            }
            return availableVehicles;
        }

        // Vehicle rental operations
        public boolean rentVehicle(int index, int days) {
            List<Vehicle> availableVehicles = getAvailableVehicles();
            if (index >= 0 && index < availableVehicles.size() && canRentVehicle()) {
                Vehicle vehicle = availableVehicles.get(index);
                vehicle.setAvailable(false);
                vehicle.setRenter(currentUser);
                currentUser.setActiveRental(true);
                return true;
            }
            return false;
        }

        public Vehicle getUserRentedVehicle() {
            if (currentUser == null) return null;
            for (Vehicle v : vehicles) {
                if (!v.isAvailable() && v.getRenter() == currentUser) {
                    return v;
                }
            }
            return null;
        }

        public void returnVehicle() {
            Vehicle rentedVehicle = getUserRentedVehicle();
            if (rentedVehicle != null) {
                rentedVehicle.setAvailable(true);
                rentedVehicle.setRenter(null);
                currentUser.setActiveRental(false);
            }
        }

        public User getCurrentUser() { return currentUser; }

        public boolean isAdmin() {
            return currentUser != null && currentUser.getUsername().equals("admin");
        }

        // Admin operations
        public void addVehicle(Vehicle vehicle) {
            vehicles.add(vehicle);
        }

        public boolean removeVehicle(int index) {
            List<Vehicle> availableVehicles = getAvailableVehicles();
            if (index >= 0 && index < availableVehicles.size()) {
                vehicles.remove(availableVehicles.get(index));
                return true;
            }
            return false;
        }
    }

    // Utility method for clearing console screen across different platforms
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    // Input validation utility with range checking
    private static int getValidIntInput(Scanner scanner, int min, int max) {
        while (true) {
            try {
                String input = scanner.nextLine();
                int choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RentalSystem rentalSystem = new RentalSystem();

        while (true) {
            // Authentication menu
            clearScreen();
            if (rentalSystem.getCurrentUser() == null) {
                System.out.println("Vehicle Rental System");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.println();
                System.out.print("Choose an option: ");
                
                int choice = getValidIntInput(scanner, 1, 3);

                switch (choice) {
                    case 1:
                        clearScreen();
                        System.out.println("=== Login ===");
                        System.out.print("Username: ");
                        String username = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();

                        if (!rentalSystem.login(username, password)) {
                            System.out.println("Invalid credentials!");
                            System.out.println("Press Enter to continue...");
                            scanner.nextLine();
                        }
                        break;

                    case 2:
                        clearScreen();
                        System.out.println("=== Register ===");
                        System.out.print("Enter new username: ");
                        String newUsername = scanner.nextLine();
                        
                        if (rentalSystem.isUserExists(newUsername)) {
                            System.out.println("Username already exists!");
                        } else {
                            System.out.print("Enter password: ");
                            String newPassword = scanner.nextLine();
                            rentalSystem.registerUser(newUsername, newPassword);
                            System.out.println("Registration successful!");
                        }
                        System.out.println("Press Enter to continue...");
                        scanner.nextLine();
                        break;

                    case 3:
                        System.out.println("Goodbye!");
                        scanner.close();
                        return;
                }
                continue;
            }

            // Admin interface
            clearScreen();
            if (rentalSystem.isAdmin()) {
                System.out.println("Welcome, Administrator");
                System.out.println("1. View all vehicles");
                System.out.println("2. Add new vehicle");
                System.out.println("3. Remove vehicle");
                System.out.println("4. Logout");
                System.out.println();
                System.out.print("Choose an option: ");

                int choice = getValidIntInput(scanner, 1, 4);

                switch (choice) {
                    case 1:
                        clearScreen();
                        List<Vehicle> allVehicles = rentalSystem.vehicles;
                        System.out.println("All Vehicles:");
                        for (int i = 0; i < allVehicles.size(); i++) {
                            Vehicle v = allVehicles.get(i);
                            System.out.println((i + 1) + ": " + v + 
                                            (v.isAvailable() ? " (Available)" : 
                                            " (Rented by: " + v.getRenter().getUsername() + ")"));
                        }
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine();
                        break;

                    case 2:
                        clearScreen();
                        System.out.println("=== Add New Vehicle ===");
                        System.out.println("1. Add Car");
                        System.out.println("2. Add Motorcycle");
                        System.out.println("3. Back to Menu");
                        System.out.println();
                        System.out.print("Choose vehicle type: ");
                        
                        int type = getValidIntInput(scanner, 1, 3);
                        if (type == 3) break;

                        System.out.print("Enter brand: ");
                        String brand = scanner.nextLine();
                        System.out.print("Enter model: ");
                        String model = scanner.nextLine();
                        System.out.print("Enter daily rate: $");
                        double rate = Double.parseDouble(scanner.nextLine());

                        if (type == 1) {
                            System.out.print("Has AC? (y/n): ");
                            boolean hasAC = scanner.nextLine().toLowerCase().startsWith("y");
                            rentalSystem.addVehicle(new Car(model, brand, rate, hasAC));
                        } else {
                            System.out.print("Include helmet? (y/n): ");
                            boolean hasHelmet = scanner.nextLine().toLowerCase().startsWith("y");
                            rentalSystem.addVehicle(new Motorcycle(model, brand, rate, hasHelmet));
                        }
                        
                        System.out.println("Vehicle added successfully!");
                        System.out.println("Press Enter to continue...");
                        scanner.nextLine();
                        break;

                    case 3:
                        clearScreen();
                        List<Vehicle> availableVehicles = rentalSystem.getAvailableVehicles();
                        if (availableVehicles.isEmpty()) {
                            System.out.println("No available vehicles to remove!");
                        } else {
                            System.out.println("Available Vehicles:");
                            for (int i = 0; i < availableVehicles.size(); i++) {
                                System.out.println((i + 1) + ": " + availableVehicles.get(i));
                            }
                            System.out.println((availableVehicles.size() + 1) + ": Back to Menu");
                            
                            System.out.print("\nEnter vehicle number to remove: ");
                            int selection = getValidIntInput(scanner, 1, availableVehicles.size() + 1);
                            
                            if (selection <= availableVehicles.size()) {
                                if (rentalSystem.removeVehicle(selection - 1)) {
                                    System.out.println("Vehicle removed successfully!");
                                } else {
                                    System.out.println("Failed to remove vehicle!");
                                }
                            }
                        }
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine();
                        break;

                    case 4:
                        rentalSystem.currentUser = null;
                        break;
                }
            } else {
                // Regular user interface with rental operations
                System.out.println("Welcome, " + rentalSystem.getCurrentUser().getUsername());
                System.out.println("1. View available vehicles");
                System.out.println("2. " + (rentalSystem.getCurrentUser().hasActiveRental() ? "Return vehicle" : "Rent a vehicle"));
                System.out.println("3. Logout");
                System.out.println();
                System.out.print("Choose an option: ");

                int choice = getValidIntInput(scanner, 1, 3);

                switch (choice) {
                    case 1:
                        clearScreen();
                        List<Vehicle> vehicles = rentalSystem.getAvailableVehicles();
                        System.out.println("Available Vehicles:");
                        for (int i = 0; i < vehicles.size(); i++) {
                            System.out.println((i + 1) + ": " + vehicles.get(i));
                        }
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine();
                        break;

                    case 2:
                        clearScreen();
                        if (rentalSystem.getCurrentUser().hasActiveRental()) {
                            Vehicle rentedVehicle = rentalSystem.getUserRentedVehicle();
                            System.out.println("Currently rented vehicle: " + rentedVehicle);
                            System.out.print("Are you sure you want to return this vehicle? (y/n): ");
                            if (scanner.nextLine().toLowerCase().startsWith("y")) {
                                rentalSystem.returnVehicle();
                                System.out.println("Vehicle returned successfully!");
                            }
                        } else {
                            if (!rentalSystem.canRentVehicle()) {
                                System.out.println("You already have an active rental!");
                                System.out.println("Please return your current rental before renting another vehicle.");
                                System.out.println("\nPress Enter to continue...");
                                scanner.nextLine();
                                break;
                            }
                            
                            vehicles = rentalSystem.getAvailableVehicles();
                            if (vehicles.isEmpty()) {
                                System.out.println("No vehicles available for rent!");
                                System.out.println("Press Enter to continue...");
                                scanner.nextLine();
                                break;
                            }
                            
                            System.out.println("Available Vehicles:");
                            for (int i = 0; i < vehicles.size(); i++) {
                                System.out.println((i + 1) + ": " + vehicles.get(i));
                            }
                            System.out.println((vehicles.size() + 1) + ": Back to Menu");
                            
                            System.out.println();
                            System.out.print("Enter your choice: ");
                            int selection = getValidIntInput(scanner, 1, vehicles.size() + 1);
                            
                            if (selection == vehicles.size() + 1) {
                                break;
                            }
                            
                            int index = selection - 1;
                            
                            System.out.print("Enter number of days (1-30): ");
                            int days = getValidIntInput(scanner, 1, 30);
                            
                            if (rentalSystem.rentVehicle(index, days)) {
                                clearScreen();
                                Vehicle rentedVehicle = vehicles.get(index);
                                double baseCost = rentedVehicle.calculateRentalCost(days);
                                double additionalCost = 0;
                                String additionalInfo = "";
                                
                                if (rentedVehicle instanceof Car) {
                                    Car car = (Car) rentedVehicle;
                                    additionalCost = car.hasAC ? 5 * days : 0;
                                    if (additionalCost > 0) additionalInfo = "AC charge: $" + additionalCost;
                                } else if (rentedVehicle instanceof Motorcycle) {
                                    Motorcycle motorcycle = (Motorcycle) rentedVehicle;
                                    additionalCost = motorcycle.getHelmetCost(days);
                                    if (additionalCost > 0) additionalInfo = "Helmet charge: $" + additionalCost;
                                }
                                
                                System.out.println("=== Rental Receipt ===");
                                System.out.println("Vehicle: " + rentedVehicle.getBrand() + " " + rentedVehicle.getModel());
                                System.out.println("Days rented: " + days);
                                System.out.println("Base rate: $" + rentedVehicle.getRentalRate() + "/day");
                                System.out.println("Base cost: $" + baseCost);
                                if (!additionalInfo.isEmpty()) {
                                    System.out.println(additionalInfo);
                                }
                                System.out.println("Total cost: $" + (baseCost + additionalCost));
                                System.out.println("\nThank you for your rental!");
                            } else {
                                System.out.println("Rental failed!");
                            }
                        }
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine();
                        break;

                    case 3:
                        rentalSystem.currentUser = null;
                        break;
                }
            }
        }
    }
}
