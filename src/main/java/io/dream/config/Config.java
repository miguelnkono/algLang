package io.dream.config;

import io.dream.scanner.TokenType;
import io.dream.types.Type;

import java.util.HashMap;
import java.util.Map;

import static io.dream.scanner.TokenType.*;
import static io.dream.scanner.TokenType.CLASS;
import static io.dream.scanner.TokenType.DOUBLE;
import static io.dream.scanner.TokenType.DO_WHILE;
import static io.dream.scanner.TokenType.ELSE;
import static io.dream.scanner.TokenType.END;
import static io.dream.scanner.TokenType.FALSE;
import static io.dream.scanner.TokenType.FOR;
import static io.dream.scanner.TokenType.IF;
import static io.dream.scanner.TokenType.INTEGER;
import static io.dream.scanner.TokenType.METHOD;
import static io.dream.scanner.TokenType.NIL;
import static io.dream.scanner.TokenType.STRING;
import static io.dream.scanner.TokenType.TABLE;
import static io.dream.scanner.TokenType.TRUE;
import static io.dream.scanner.TokenType.WHILE;

final public class Config
{
    // true means that we want to use French and false means that we want to use English
    private static boolean language = true;

    public static boolean getLanguage()
    {
        return language;
    }

    public static void setLanguage(boolean language)
    {
        Config.language = language;
    }

    public static Map<String, TokenType> keywordsEnglish()
    {
        Map<String, TokenType> keywords = new HashMap<>();
        keywords.put("Algorithm", ALGORITHM);
        keywords.put("Variables", VARIABLE);
        keywords.put("Begin", BEGIN);
        keywords.put("End", END);
        keywords.put("EndStruct", END_STRUCT);
        keywords.put("Method", METHOD);
        keywords.put("Class", CLASS);
        keywords.put("Type", TYPE);
        keywords.put("Structure", STRUCTURE);
        keywords.put("if", IF);
        keywords.put("then", THEN);
        keywords.put("else", ELSE);
        keywords.put("else if", ELSEIF);
        keywords.put("endif", ENDIF);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("until", UNTIL);
        keywords.put("do", DO);
        keywords.put("repeat", REPEAT);
        keywords.put("step", STEP);
        keywords.put("true", TRUE);
        keywords.put("endwhile", ENDWHILE);
        keywords.put("endfor", ENDFOR);
        keywords.put("false", FALSE);
        keywords.put("null", NIL);
        keywords.put("table", TABLE);
        keywords.put("integer", INTEGER);
        keywords.put("real", DOUBLE);
        keywords.put("string", STRING);
        keywords.put("char", CHARACTER);
        keywords.put("boolean", BOOLEAN);
        keywords.put("write", WRITE);
        keywords.put("read", READ);

        return keywords;
    }

    public static Map<String, TokenType> keywordsFrench()
    {
        Map<String, TokenType> keywords = new HashMap<>();
        keywords.put("Algorithme", ALGORITHM);
        keywords.put("Variables", VARIABLE);
        keywords.put("Debut", BEGIN);
        keywords.put("Fin", END);
        keywords.put("FinStruct", END_STRUCT);
        keywords.put("Methode", METHOD);
        keywords.put("Classe", CLASS);
        keywords.put("Type", TYPE);
        keywords.put("Structure", STRUCTURE);
        keywords.put("si", IF);
        keywords.put("sinon", ELSE);
        keywords.put("sinon si", ELSEIF);
        keywords.put("finsi", ENDIF);
        keywords.put("alors", THEN);
        keywords.put("pour", FOR);
        keywords.put("pas", STEP);
        keywords.put("faire", DO);
        keywords.put("fintantque", ENDWHILE);
        keywords.put("finpour", ENDFOR);
        keywords.put("tant_que", WHILE);
        keywords.put("repeter", REPEAT);
        keywords.put("jusqu_a", UNTIL);
        keywords.put("vrai", TRUE);
        keywords.put("faux", FALSE);
        keywords.put("nil", NIL);
        keywords.put("tableau", TABLE);
        keywords.put("entier", INTEGER);
        keywords.put("reel", DOUBLE);
        keywords.put("chaine_charactere", STRING);
        keywords.put("caractere", CHARACTER);
        keywords.put("booleen", BOOLEAN);
        keywords.put("ecrire", WRITE);
        keywords.put("lire", READ);

        return keywords;
    }

}
