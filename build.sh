#!/bin/bash

# AlgoLang Native Build Script
# This script builds native executables for your platform

set -e  # Exit on error

echo "========================================="
echo "  AlgoLang Native Build Script"
echo "========================================="
echo ""

# Check if GraalVM is installed
if ! command -v native-image &> /dev/null; then
    echo "❌ Error: GraalVM Native Image not found!"
    echo ""
    echo "Please install GraalVM first:"
    echo "  curl -s 'https://get.sdkman.io' | bash"
    echo "  source \"\$HOME/.sdkman/bin/sdkman-init.sh\""
    echo "  sdk install java 21.0.2-graalce"
    echo ""
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven not found!"
    echo ""
    echo "Please install Maven:"
    echo "  sudo apt install maven  # On Ubuntu/Debian"
    echo "  brew install maven      # On macOS"
    echo ""
    exit 1
fi

echo "✓ GraalVM Native Image found"
echo "✓ Maven found"
echo ""

# Show Java version
echo "Java version:"
java -version
echo ""

# Ask what to build
echo "What would you like to build?"
echo "  1) JAR file (fast, requires Java to run)"
echo "  2) Native executable (slow build, standalone binary)"
echo "  3) Both"
echo ""
read -p "Enter choice [1-3]: " choice

case $choice in
    1)
        echo ""
        echo "Building JAR..."
        mvn clean package
        echo ""
        echo "✓ JAR built successfully!"
        echo "  Location: target/algoLang-1.0.0-jar-with-dependencies.jar"
        echo "  Run with: java -jar target/algoLang-1.0.0-jar-with-dependencies.jar test.al"
        ;;
    2)
        echo ""
        echo "Building native executable..."
        echo "This will take 2-5 minutes..."
        mvn clean package -Pnative
        echo ""
        echo "✓ Native executable built successfully!"
        echo "  Location: target/algolang"
        echo "  Run with: ./target/algolang test.al"
        ;;
    3)
        echo ""
        echo "Building JAR..."
        mvn clean package
        echo ""
        echo "Building native executable..."
        echo "This will take 2-5 minutes..."
        mvn package -Pnative
        echo ""
        echo "✓ Both built successfully!"
        echo "  JAR: target/algoLang-1.0.0-jar-with-dependencies.jar"
        echo "  Native: target/algolang"
        ;;
    *)
        echo "Invalid choice!"
        exit 1
        ;;
esac

echo ""
echo "========================================="
echo "  Build Complete!"
echo "========================================="

# Ask if user wants to test
echo ""
read -p "Would you like to run a test? [y/N]: " test_choice

if [[ $test_choice =~ ^[Yy]$ ]]; then
    # Create a test file
    cat > /tmp/test_algolang.al << 'EOF'
Algorithme: test;
Variables:
    nom : chaine_charactere;
    age : entier;
Debut:
    nom <- "AlgoLang";
    age <- 1;
    ecrire("Nom: " + nom);
    ecrire("Age: " + age);
    ecrire("Build successful!");
Fin
EOF

    echo ""
    echo "Running test program..."
    echo ""

    if [ -f target/algolang ]; then
        ./target/algolang /tmp/test_algolang.al
    elif [ -f target/algoLang-1.0.0-jar-with-dependencies.jar ]; then
        java -jar target/algoLang-1.0.0-jar-with-dependencies.jar /tmp/test_algolang.al
    fi

    rm /tmp/test_algolang.al
fi

echo ""
echo "Done! 🎉"