Algorithme: factorielle;

Fonction: fact(n: entier): entier;
Variables:
    resultat, i : entier;
Debut:
    resultat <- 1;
    pour i <- 1 jusqu_a n faire:
        resultat <- resultat * i;
    finpour
    retourne resultat;
Fin
FinFonction;

Variables:
    x : entier;
Debut:
    ecrire("Nombre: ");
    lire(x);
    ecrire("Factorielle: " + fact(x));
Fin
