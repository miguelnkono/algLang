# AlgoLang - Bilingual Algorithmic Language Interpreter

A modern interpreter for algorithmic pseudocode that supports both French and English syntax.

## 🚀 Quick Start

### Download

Choose the binary for your platform:

- **Linux (x64)**: `algolang-linux-x64.tar.gz`
- **Windows (x64)**: `algolang-windows-x64.zip`
- **macOS (x64)**: `algolang-macos-x64.tar.gz`

### Installation

#### Linux / macOS
```bash
# Extract
tar -xzf algolang-*.tar.gz

# Make executable
chmod +x algolang

# Run
./algolang your-program.al
```

#### Windows
```cmd
# Extract the zip file (right-click > Extract All)

# Run
algolang.exe your-program.al
```

## 📖 Usage

### French Mode (Default)
```bash
algolang program.al
```

Example program:
```
Algorithme: exemple;
Variables:
    x : entier;
    nom : chaine_charactere;
Debut:
    x <- 42;
    nom <- "Bonjour";
    ecrire("x = " + x);
    ecrire("nom = " + nom);
Fin
```

### English Mode
```bash
algolang program.al --language=1
```

Example program:
```
Algorithm: example;
Variables:
    x : integer;
    name : string;
Begin:
    x <- 42;
    name <- "Hello";
    write("x = " + x);
    write("name = " + name);
End
```

## 🌍 Supported Languages

### Keywords

| French | English | Purpose |
|--------|---------|---------|
| Algorithme | Algorithm | Program header |
| Variables | Variables | Variable declarations |
| Debut | Begin | Block start |
| Fin | End | Block end |
| entier | integer | Integer type |
| reel | real | Float type |
| chaine_charactere | string | String type |
| caractere | char | Character type |
| booleen | boolean | Boolean type |
| vrai | true | Boolean true |
| faux | false | Boolean false |
| ecrire | write | Print statement |

### Number Format

- **French**: Use comma for decimals (e.g., `1,75`)
- **English**: Use dot for decimals (e.g., `1.75`)

## 🔧 Features

- ✅ Variable declarations and assignments
- ✅ Integer, real, string, character, and boolean types
- ✅ Arithmetic operations (+, -, *, /)
- ✅ Comparison operators (>, <, >=, <=, ==, !=)
- ✅ Logical operators (!)
- ✅ String concatenation
- ✅ Type checking
- ✅ Bilingual error messages

## 📝 Examples

### Variable Assignment (French)
```
Algorithme: variables;
Variables:
    age : entier;
    taille : reel;
    nom : chaine_charactere;
Debut:
    age <- 25;
    taille <- 1,75;
    nom <- "Alice";
    ecrire("Nom: " + nom);
    ecrire("Age: " + age);
    ecrire("Taille: " + taille);
Fin
```

### Arithmetic (English)
```
Algorithm: math;
Variables:
    x : integer;
    y : integer;
    result : integer;
Begin:
    x <- 10;
    y <- 5;
    result <- x + y;
    write("Sum: " + result);
    result <- x * y;
    write("Product: " + result);
End
```

## 🐛 Error Messages

Errors are displayed in the selected language:

**French:**
```
[ligne 5] Erreur : à 'x' : Variable 'x' non déclarée.
```

**English:**
```
[line 5] Error : at 'x' : Variable 'x' not declared.
```

## 🏗️ Building from Source

See [NATIVE_BUILD_GUIDE.md](NATIVE_BUILD_GUIDE.md) for instructions on building native executables.

Quick build:
```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/algolang.git
cd algolang

# Build (requires GraalVM and Maven)
./build.sh  # Linux/macOS
build.bat   # Windows
```

## 📄 License

See LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## 📧 Support

For bug reports and feature requests, please use GitHub Issues.
