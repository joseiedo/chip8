# Variables
MVNW=./mvnw

# Default target
all: build

# Build the project
build:
	@echo "Building the project..."
	@$(MVNW) clean install

# Run the JavaFX application
run: build
	@echo "Running the JavaFX application..."
	@$(MVNW) javafx:run

# Clean the project
clean:
	@echo "Cleaning the project..."
	@$(MVNW) clean

.PHONY: all build run clean
