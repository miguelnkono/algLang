# AlgoLang Interactive REPL - User Guide

## Overview

The AlgoLang REPL (Read-Eval-Print-Loop) provides an interactive environment for writing and testing AlgoLang code in real-time.

## Starting the REPL

```bash
# Start interactive mode
./algolang

# Or with JAR
java -jar algoLang-1.0.0.jar
```

## Features

### ✨ Enhanced REPL Features

1. **🌈 Syntax Highlighting** - Color-coded output for better readability
2. **📝 Command History** - Keep track of your commands
3. **🔄 Multi-line Input** - Write complete programs interactively
4. **🌍 Bilingual Support** - Switch between French and English
5. **📚 Built-in Examples** - Learn from examples
6. **💡 Syntax Help** - Quick reference guides
7. **🎨 Color Themes** - Beautiful color-coded interface

### 📋 Available Commands

| Command | Description |
|---------|-------------|
| `.help` | Show all available commands |
| `.exit` or `.quit` | Exit the REPL |
| `.clear` | Clear the screen |
| `.history` | Show command history |
| `.lang` | Toggle between French/English |
| `.french` | Switch to French mode |
| `.english` | Switch to English mode |
| `.example` | Show example programs |
| `.syntax` | Show syntax reference |
| `.reset` | Reset the interpreter |

## Usage Modes

### 1. Single-Line Mode

Execute individual statements directly:

#### French
```
algolang> ecrire("Bonjour!");
Bonjour!

algolang> ecrire("Résultat: " + (10 + 5));
Résultat: 15
```

#### English
```
algolang> write("Hello!");
Hello!

algolang> write("Result: " + (10 + 5));
Result: 15
```

### 2. Multi-Line Mode

Write complete programs with variables:

#### French
```
algolang> Algorithme: test;
... Variables:
...     x : entier;
...     nom : chaine_charactere;
... Debut:
...     x <- 42;
...     nom <- "Alice";
...     ecrire("x = " + x);
...     ecrire("nom = " + nom);
... Fin

x = 42
nom = Alice
```

#### English
```
algolang> Algorithm: test;
... Variables:
...     x : integer;
...     name : string;
... Begin:
...     x <- 42;
...     name <- "Alice";
...     write("x = " + x);
...     write("name = " + name);
... End

x = 42
name = Alice
```

### 3. Quick Expressions

The REPL automatically wraps simple expressions:

```
algolang> ecrire("Quick test");
Quick test
```

Gets wrapped internally as:
```
Algorithme: repl;
Debut:
    ecrire("Quick test");
Fin
```

## Interactive Features

### Command History

Track all your commands:

```
algolang> .history

History:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[1] ecrire("Bonjour");
[2] ecrire("Test");
[3] (multi-line)
```

### Language Switching

Switch languages on the fly:

```
algolang> .french
✓ Langue changée: Français

algolang> .english
✓ Language changed: English

algolang> .lang
✓ Language changed: Français
```

### Syntax Reference

Quick syntax lookup:

```
algolang> .syntax

Syntax Reference:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Data types:
  integer, real, string, char, boolean

Arithmetic operators:
  + - * /

Comparison operators:
  > < >= <= == !=

Assignment:
  variable <- value;

Output:
  write(expression);
```

### Examples

View example programs:

```
algolang> .example

Examples:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. Simple expression:
   write("Hello world!");

2. Calculation:
   write("Result: " + (10 + 5));

3. Complete program:
   Algorithm: example;
   Variables:
       x : integer;
   Begin:
       x <- 42;
       write("x = " + x);
   End
```

## Tips & Tricks

### 1. Quick Testing

Test expressions quickly without full program structure:

```
algolang> ecrire(5 + 3);
8

algolang> ecrire("Hello " + "World");
Hello World
```

### 2. Multi-line Editing

Start with `Algorithme:` or `Algorithm:` to enter multi-line mode:

```
algolang> Algorithme: test;
... (type your program)
... Fin
(executes)
```

Empty line executes the buffered code.

### 3. Clear Screen

Keep your workspace clean:

```
algolang> .clear
(screen clears and shows welcome banner)
```

### 4. Reset When Needed

If things get messy:

```
algolang> .reset
✓ Interpreter reset
```

### 5. Use History

Review what you've typed:

```
algolang> .history
(shows all previous commands)
```

## Color Scheme

The REPL uses colors for better readability:

- 🔵 **Blue** - Prompt
- 🟢 **Green** - Success messages, welcome banner
- 🟡 **Yellow** - Commands, warnings
- 🔴 **Red** - Errors
- ⚪ **Gray** - Help text, multi-line continuation
- 🔷 **Cyan** - Section headers

### Disable Colors

If colors don't work in your terminal, use SimpleREPL:

Update `Main.java`:

```java


private static void runPrompt() throws IOException {
    SimpleREPL repl = new SimpleREPL();
    repl.start();
}
```

## Examples in Practice

### Calculator
```
algolang> ecrire("10 + 5 = " + (10 + 5));
10 + 5 = 15

algolang> ecrire("10 * 5 = " + (10 * 5));
10 * 5 = 50
```

### String Manipulation
```
algolang> ecrire("Hello " + "World" + "!");
Hello World!
```

### Complete Program
```
algolang> Algorithme: fibonacci;
... Variables:
...     a : entier;
...     b : entier;
...     c : entier;
... Debut:
...     a <- 0;
...     b <- 1;
...     ecrire("Fibonacci: " + a);
...     ecrire("Fibonacci: " + b);
...     c <- a + b;
...     ecrire("Fibonacci: " + c);
... Fin

Fibonacci: 0
Fibonacci: 1
Fibonacci: 1
```

## Keyboard Shortcuts

- **Enter** - Execute current line / add to multi-line buffer
- **Ctrl+D** - Exit (EOF)
- **Ctrl+C** - Cancel current input (terminal dependent)

## Error Handling

Errors are displayed clearly with line numbers:

```
algolang> ecrire(x);
[line 2] Error : at 'x' : Variable 'x' not declared.
```

The REPL continues running after errors - no need to restart!

## Common Workflows

### Quick Testing
```
1. Start REPL
2. Type expression
3. Press Enter
4. See immediate result
```

### Program Development
```
1. Type: Algorithme: myprogram;
2. Declare variables
3. Write logic
4. Type: Fin
5. Press Enter
6. See output
7. Refine and repeat
```

### Learning
```
1. Type: .example
2. Copy example
3. Modify it
4. Test variations
5. Type: .syntax for reference
```

## Troubleshooting

### Colors Not Showing?

Your terminal might not support ANSI colors. Switch to SimpleREPL.

### Multi-line Mode Stuck?

- Type `Fin` or `End` to execute
- Press Enter on empty line to cancel

### Want to Clear History?

```
algolang> .reset
```

### Need Help?

```
algolang> .help
```

## Best Practices

1. **Use .clear often** - Keep your workspace clean
2. **Check .history** - Review what worked
3. **Test incrementally** - Start simple, build up
4. **Use .example** - Learn from working code
5. **Switch languages** - Practice both French and English

## Advanced Usage

### Rapid Prototyping

```
algolang> ecrire("Test 1");
Test 1

algolang> ecrire("Test 2");
Test 2

algolang> .history
[1] ecrire("Test 1");
[2] ecrire("Test 2");
```

### Learning Mode

```
algolang> .syntax      # Learn syntax
algolang> .example     # See examples  
algolang> (try it)     # Practice
algolang> .lang        # Try other language
```

## Exiting the REPL

Multiple ways to exit:

```
algolang> .exit
👋 Goodbye!

algolang> .quit
👋 Goodbye!

(or press Ctrl+D)
```

## Summary

The AlgoLang REPL provides:
- ✅ Instant feedback
- ✅ No file management
- ✅ Built-in help
- ✅ Color-coded interface
- ✅ Bilingual support
- ✅ Easy learning curve

Perfect for:
- 🎓 Learning AlgoLang
- 🧪 Testing code snippets
- 🔬 Experimenting with syntax
- 📝 Quick calculations
- 🚀 Rapid prototyping

**Start experimenting today!** 🎉
