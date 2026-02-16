# Native Build Setup Guide

This guide will help you compile AlgoLang into native executables for Linux, Windows, and macOS using GraalVM Native Image.

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Local Build Instructions](#local-build-instructions)
- [GitHub Actions Automated Releases](#github-actions-automated-releases)
- [Editor Integration](#editor-integration)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### 1. Install GraalVM

#### On Linux (Mint/Ubuntu/Debian)
```bash
# Download and install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install GraalVM
sdk install java 21.0.2-graalce

# Set as default
sdk default java 21.0.2-graalce

# Verify installation
java -version
# Should show: "GraalVM CE"
```

#### On Windows
1. Download GraalVM from: https://github.com/graalvm/graalvm-ce-builds/releases
2. Extract to `C:\Program Files\GraalVM\`
3. Set `JAVA_HOME` environment variable
4. Add to PATH: `C:\Program Files\GraalVM\bin`
5. Install Visual Studio Build Tools 2022 (required for native-image on Windows):
   - Download from: https://visualstudio.microsoft.com/downloads/
   - Install "Desktop development with C++"

#### On macOS
```bash
# Using Homebrew
brew install --cask graalvm/tap/graalvm-ce-java21

# Set JAVA_HOME
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-java21-21.0.2/Contents/Home

# Verify
java -version
```

### 2. Install Maven (if not already installed)

#### Linux
```bash
sudo apt update
sudo apt install maven
```

#### Windows
Download from: https://maven.apache.org/download.cgi

#### macOS
```bash
brew install maven
```

## Local Build Instructions

### Building the JAR (Standard Java)
```bash
# Build regular JAR
mvn clean package

# Run the JAR
java -jar target/algoLang-1.0.0.jar test.al
```

### Building Native Executable (Linux/macOS)

```bash
# Build native executable
mvn clean package -Pnative

# The executable will be at: target/algolang

# Make it executable (if not already)
chmod +x target/algolang

# Test it
./target/algolang test.al

# Test English mode
./target/algolang test.al --language=1
```

**Expected output:**
- Build time: 2-5 minutes (first time)
- Executable size: ~20-40 MB
- Startup time: instant (vs ~1-2s for JAR)

### Building Native Executable (Windows)

```cmd
REM Open "x64 Native Tools Command Prompt for VS 2022"

REM Build native executable
mvn clean package -Pnative

REM The executable will be at: target\algolang.exe

REM Test it
target\algolang.exe test.al

REM Test English mode
target\algolang.exe test.al --language=1
```

### Creating Distribution Archives

#### Linux/macOS
```bash
# Create tarball
cd target
tar -czf algolang-linux-x64.tar.gz algolang
```

#### Windows
```powershell
# Create zip
cd target
Compress-Archive -Path algolang.exe -DestinationPath algolang-windows-x64.zip
```

## GitHub Actions Automated Releases

The project includes GitHub Actions workflow that automatically builds native executables for all platforms when you push a version tag.

### Setup Steps:

1. **Push your code to GitHub:**
```bash
git init
git add .
git commit -m "Initial commit with native build support"
git remote add origin https://github.com/YOUR_USERNAME/algolang.git
git push -u origin main
```

2. **Create the workflow directory structure:**
```bash
mkdir -p .github/workflows
cp release.yml .github/workflows/
git add .github/workflows/release.yml
git commit -m "Add GitHub Actions release workflow"
git push
```

3. **Create and push a release tag:**
```bash
# Tag your release
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

4. **Monitor the build:**
   - Go to your GitHub repository
   - Click "Actions" tab
   - Watch the build progress (takes ~10-15 minutes)

5. **Download releases:**
   - Go to "Releases" tab
   - Download the binaries for your platform

### What Gets Built Automatically:

- ✅ `algolang-linux-x64.tar.gz` - Linux native executable
- ✅ `algolang-windows-x64.zip` - Windows native executable  
- ✅ `algolang-macos-x64.tar.gz` - macOS native executable

## Editor Integration

### Method 1: Using Native Executable (Recommended)

In your editor, simply call the native executable:

```javascript
// Node.js example
const { exec } = require('child_process');

function runAlgoLang(filePath, language = 'french') {
  const langFlag = language === 'english' ? '--language=1' : '--language=0';
  
  return new Promise((resolve, reject) => {
    exec(`./algolang ${filePath} ${langFlag}`, (error, stdout, stderr) => {
      if (error) {
        reject({ error, stderr });
      } else {
        resolve(stdout);
      }
    });
  });
}

// Usage
runAlgoLang('program.al', 'french')
  .then(output => console.log(output))
  .catch(err => console.error(err));
```

### Method 2: Using JAR File

```javascript
function runAlgoLangJar(filePath, language = 'french') {
  const langFlag = language === 'english' ? '--language=1' : '--language=0';
  
  return new Promise((resolve, reject) => {
    exec(`java -jar algolang.jar ${filePath} ${langFlag}`, 
      (error, stdout, stderr) => {
        if (error) {
          reject({ error, stderr });
        } else {
          resolve(stdout);
        }
    });
  });
}
```

### Method 3: Process Communication (Advanced)

For better performance, use stdin/stdout communication:

```javascript
const { spawn } = require('child_process');

class AlgoLangInterpreter {
  constructor(executablePath) {
    this.executablePath = executablePath;
  }
  
  async execute(code, language = 'french') {
    const langFlag = language === 'english' ? '--language=1' : '--language=0';
    
    // Write code to temp file
    const tempFile = `/tmp/program-${Date.now()}.al`;
    fs.writeFileSync(tempFile, code);
    
    return new Promise((resolve, reject) => {
      const process = spawn(this.executablePath, [tempFile, langFlag]);
      
      let stdout = '';
      let stderr = '';
      
      process.stdout.on('data', (data) => {
        stdout += data.toString();
      });
      
      process.stderr.on('data', (data) => {
        stderr += data.toString();
      });
      
      process.on('close', (code) => {
        // Clean up temp file
        fs.unlinkSync(tempFile);
        
        if (code === 0) {
          resolve({ output: stdout, exitCode: code });
        } else {
          reject({ error: stderr, exitCode: code });
        }
      });
    });
  }
}

// Usage
const interpreter = new AlgoLangInterpreter('./algolang');

interpreter.execute(`
Algorithme: test;
Variables:
    x : entier;
Debut:
    x <- 42;
    ecrire("Result: " + x);
Fin
`, 'french')
  .then(result => console.log(result.output))
  .catch(err => console.error(err.error));
```

## Troubleshooting

### Linux: "algolang: command not found"
```bash
# Make sure it's executable
chmod +x algolang

# Run with ./
./algolang test.al

# Or add to PATH
sudo cp algolang /usr/local/bin/
```

### Windows: "algolang.exe is not recognized"
```cmd
REM Use full path
C:\path\to\algolang.exe test.al

REM Or add to PATH (System Properties > Environment Variables)
```

### Build Error: "native-image: command not found"
```bash
# GraalVM Native Image might not be installed
# On GraalVM 21+, it's included by default
# For older versions:
gu install native-image
```

### Build Error: "Visual Studio not found" (Windows)
- Install Visual Studio Build Tools 2022
- Make sure "Desktop development with C++" is selected
- Use "x64 Native Tools Command Prompt for VS 2022" to build

### Native Image Build Fails on Reflection Issues
The project includes `reflect-config.json` which should handle all reflection.
If you add new classes with reflection, update:
`src/main/resources/META-INF/native-image/reflect-config.json`

### Large Executable Size
This is normal for native images. Optimizations:
```bash
# Add to pom.xml buildArgs:
<buildArg>-O3</buildArg>  <!-- Maximum optimization -->
<buildArg>--gc=G1</buildArg>  <!-- Better GC -->
```

## Performance Comparison

| Metric | JAR | Native Executable |
|--------|-----|-------------------|
| Startup Time | ~1-2s | ~0.01s |
| Memory Usage | ~100MB | ~20MB |
| File Size | ~10KB + JRE | ~25MB standalone |
| Distribution | Requires Java | Standalone binary |

## Next Steps

1. ✅ Build native executable locally
2. ✅ Test with your programs
3. ✅ Set up GitHub Actions
4. ✅ Create first release
5. ✅ Integrate into your editor
6. 🚀 Distribute to users!

## Support

For issues:
- Check GitHub Issues
- Review GraalVM docs: https://www.graalvm.org/native-image/
- Native Image compatibility: https://www.graalvm.org/native-image/compatibility/