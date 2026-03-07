
// en français;
Algorithm: methodes;

Methode: bonjour(nom: chaine_caractère):
    ecrire("bonjour " + nom);
FinMethode;

Function: cube(arg: nombre): reel;
    retourne arg * arg * arg;
FinFunction;

Variables:
    arg : reel;
    nom: chaine_caractère;
Debut:
    nom <- "miguel";
    bonjour(nom);

    ecrire("entryez un nombre: ");
    lire(arg);

    ecrire("le cube de " + arg + " est: " + cube(arg));
Fin

// in english;
Algorithm: TestTwo;

Method: hello(name: string):
    write("hello " + name);
EndMethod;

Function: cube(arg: nombre): real;
    return arg * arg * arg;
FinFunction;

Variables:
    arg: real;
    name: string;
Begin:
    name <- "miguel";
    hello(name);

    write("enter a number: ");
    read(arg);

    write("the cube of " + arg + " is: " + cube(arg));
End
