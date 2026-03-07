Algorithme: complet;

Type:
    Structure Etudiant
        nom : chaine_charactere;
        notes : tableau[1..3] de reel;
    FinStruct

Fonction: moyenne(e: Etudiant): reel;
Variables:
    somme : reel;
    i : entier;
Debut:
    somme <- 0;
    pour i <- 1 jusqu_a 3 faire:
        somme <- somme + e.notes[i];
    finpour
    retourne somme / 3;
Fin
FinFonction;

Variables:
    etudiant : Etudiant;
    i : entier;
Debut:
    ecrire("Nom: ");
    lire(etudiant.nom);

    pour i <- 1 jusqu_a 3 faire:
        ecrire("Note " + i + ": ");
        lire(etudiant.notes[i]);
    finpour

    ecrire(etudiant.nom + " - Moyenne: " + moyenne(etudiant));
Fin