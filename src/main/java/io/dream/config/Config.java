package io.dream.config;

import io.dream.scanner.TokenType;

import java.util.HashMap;
import java.util.Map;

import static io.dream.scanner.TokenType.*;

/**
 * Configuration class for AlgoLang
 */
final public class Config
{
    // true means French, false means English
    private static boolean language = true;

    public static boolean getLanguage()
    {
        return language;
    }

    public static void setLanguage(boolean language)
    {
        Config.language = language;
    }

    /**
     * Get English keywords mapping
     */
    public static Map<String, TokenType> keywordsEnglish()
    {
        Map<String, TokenType> keywords = new HashMap<>();

        // Program structure
        keywords.put("Algorithm", ALGORITHM);
        keywords.put("Variables", VARIABLE);
        keywords.put("Constant", CONSTANT);
        keywords.put("Type", TYPE);
        keywords.put("Begin", BEGIN);
        keywords.put("End", END);

        // Functions and methods
        keywords.put("Function", FUNCTION);
        keywords.put("EndFunction", END_FUNCTION);
        keywords.put("Method", METHOD);
        keywords.put("EndMethod", END_METHOD);
        keywords.put("return", RETURN);

        // Structures
        keywords.put("Structure", STRUCTURE);
        keywords.put("EndStruct", END_STRUCT);

        // Control flow
        keywords.put("if", IF);
        keywords.put("then", THEN);
        keywords.put("else", ELSE);
        keywords.put("else if", ELSEIF);
        keywords.put("endif", ENDIF);

        keywords.put("for", FOR);
        keywords.put("to", TO);
        keywords.put("step", STEP);
        keywords.put("endfor", ENDFOR);

        keywords.put("while", WHILE);
        keywords.put("do", DO);
        keywords.put("endwhile", ENDWHILE);

        keywords.put("repeat", REPEAT);
        keywords.put("until", UNTIL);

        // Logical operators
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("not", NOT);

        // Boolean literals
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("null", NIL);

        // Types
        keywords.put("integer", INTEGER);
        keywords.put("real", DOUBLE);
        keywords.put("string", STRING);
        keywords.put("char", CHARACTER);
        keywords.put("boolean", BOOLEAN);
        keywords.put("number", NUMBER);
        keywords.put("array", TABLE);
        keywords.put("of", OF);

        // I/O
        keywords.put("write", WRITE);
        keywords.put("read", READ);

        // Other
        keywords.put("Class", CLASS);
        keywords.put("mod", MOD);

        return keywords;
    }

    /**
     * Get French keywords mapping
     */
    public static Map<String, TokenType> keywordsFrench()
    {
        Map<String, TokenType> keywords = new HashMap<>();

        // Program structure
        keywords.put("Algorithme", ALGORITHM);
        keywords.put("Variables", VARIABLE);
        keywords.put("Constante", CONSTANT);
        keywords.put("Type", TYPE);
        keywords.put("Debut", BEGIN);
        keywords.put("Fin", END);

        // Functions and methods
        keywords.put("Fonction", FUNCTION);
        keywords.put("FinFonction", END_FUNCTION);
        keywords.put("Methode", METHOD);
        keywords.put("FinMethode", END_METHOD);
        keywords.put("retourne", RETURN);

        // Structures
        keywords.put("Structure", STRUCTURE);
        keywords.put("FinStruct", END_STRUCT);

        // Control flow
        keywords.put("si", IF);
        keywords.put("alors", THEN);
        keywords.put("sinon", ELSE);
        keywords.put("sinon si", ELSEIF);
        keywords.put("finsi", ENDIF);

        keywords.put("pour", FOR);
        keywords.put("jusqu_a", TO);
        keywords.put("pas", STEP);
        keywords.put("finpour", ENDFOR);

        keywords.put("tant_que", WHILE);
        keywords.put("faire", DO);
        keywords.put("fintantque", ENDWHILE);

        keywords.put("repeter", REPEAT);
        keywords.put("jusqu_a", UNTIL);

        // Logical operators
        keywords.put("et", AND);
        keywords.put("ou", OR);
        keywords.put("non", NOT);

        // Boolean literals
        keywords.put("vrai", TRUE);
        keywords.put("faux", FALSE);
        keywords.put("nil", NIL);

        // Types
        keywords.put("entier", INTEGER);
        keywords.put("reel", DOUBLE);
        keywords.put("chaine_charactere", STRING);
        keywords.put("chaine_caractere", STRING);
        keywords.put("caractere", CHARACTER);
        keywords.put("booleen", BOOLEAN);
        keywords.put("nombre", NUMBER);
        keywords.put("tableau", TABLE);
        keywords.put("de", OF);

        // I/O
        keywords.put("ecrire", WRITE);
        keywords.put("lire", READ);

        // Other
        keywords.put("Classe", CLASS);
        keywords.put("mod", MOD);

        return keywords;
    }
}
