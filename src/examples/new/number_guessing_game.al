Algorithme: jeu_devinette;
Variables:
    nombre, essai, tentatives : entier;
    rejouer : booleen;
Debut:
    rejouer <- vrai;

    tant_que (rejouer) faire:
        nombre <- 42;  // In real game, use random number
        tentatives <- 0;

        ecrire("=== Jeu de devinette ===");
        ecrire("Devinez le nombre entre 1 et 100");

        repeter:
            ecrire("Votre essai:");
            lire(essai);
            tentatives <- tentatives + 1;

            si essai < nombre alors:
                ecrire("Trop petit!");
            sinon si essai > nombre alors:
                ecrire("Trop grand!");
            finsi
        jusqu_a (essai == nombre)

        ecrire("Bravo! Trouve en " + tentatives + " tentatives!");

        ecrire("Rejouer? (1=oui, 0=non)");
        lire(rejouer);
    fintantque
Fin