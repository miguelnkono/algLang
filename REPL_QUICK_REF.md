# REPL Quick Reference Card

## 🚀 Starting
```bash
./algolang              # Start interactive mode
```

## 📋 Essential Commands

| Command | Action |
|---------|--------|
| `.help` | Show help |
| `.exit` | Exit REPL |
| `.lang` | Switch language |
| `.example` | Show examples |
| `.syntax` | Syntax reference |

## 💡 Quick Usage

### Single Line (French)
```
algolang> ecrire("Bonjour!");
Bonjour!
```

### Single Line (English)
```
algolang> write("Hello!");
Hello!
```

### Multi-Line
```
algolang> Algorithme: test;
... Variables:
...     x : entier;
... Debut:
...     x <- 42;
...     ecrire(x);
... Fin
42
```

## 🎯 Tips

- **No semicolon needed** - REPL adds it automatically for single lines
- **Empty line** - Executes multi-line buffer
- **Ctrl+D** - Quick exit
- **Type `.clear`** - Clean screen
- **Type `.history`** - See what you did

## 🌍 Language Switch

```
.french    → Français
.english   → English
.lang      → Toggle
```

## ⚡ Quick Tests

```
algolang> ecrire(10 + 5);
15

algolang> ecrire("Hello " + "World");
Hello World

algolang> ecrire(10 > 5);
true
```

## 🎨 Features

✅ Color-coded output  
✅ Command history  
✅ Multi-line input  
✅ Built-in examples  
✅ Syntax help  
✅ Auto-wrap expressions  

## 🆘 Help

```
.help      → Full command list
.example   → See examples
.syntax    → Syntax reference
```

**That's all you need to get started!** 🎉
