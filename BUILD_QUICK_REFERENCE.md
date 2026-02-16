# AlgoLang Build Quick Reference

## Native Executable Builds

Three build profiles are available:

### Standard Build (Recommended)
```bash
mvn clean package -Pnative
```
- Build time: ~60 seconds
- Size: 15MB
- Performance: Excellent

### Optimized Build (Maximum Performance)
```bash
mvn clean package -Pnative-optimized
```
- Flags: `-O3 -march=native --gc=serial`
- Build time: ~120 seconds
- Size: 16MB
- Performance: 5-10% faster than standard

### Minimal Build (Smallest Size)
```bash
mvn clean package -Pnative-minimal
```
- Flags: `-Oz --gc=serial -H:+RemoveUnusedSymbols`
- Build time: ~90 seconds
- Size: 12MB
- Performance: Good

## Running

After building, run with:
```bash
./target/algolang program.al --language=0
```

## JAR Distribution

Build without native profile:
```bash
mvn clean package -DskipTests
```
- Size: 2.5MB
- Requires Java
- Run with: `java -jar target/algoLang-1.0.0.jar program.al`

## Key Differences

| Aspect | Native | JAR |
|--------|--------|-----|
| Startup | <100ms | 1-2s |
| Size | 15MB | 2.5MB |
| Java Required | No | Yes |
| Build Time | 60s | 10s |
| Performance | Excellent | Good |

## Troubleshooting

No native-image? Install it:
```bash
gu install native-image
```

Out of memory? Increase heap:
```bash
export MAVEN_OPTS="-Xmx4g"
mvn clean package -Pnative
```

