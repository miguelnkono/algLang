package io.dream.config;

import io.dream.scanner.TokenType;

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
        keywords.put("Method", METHOD);
        keywords.put("Class", CLASS);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("for", FOR);
        keywords.put("until", WHILE);
        keywords.put("while", DO_WHILE);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("null", NIL);
        keywords.put("table", TABLE);
        keywords.put("integer", INTEGER);
        keywords.put("real", DOUBLE);
        keywords.put("string", STRING);

        return keywords;
    }

    public static Map<String, TokenType> keywordsFrench()
    {
        Map<String, TokenType> keywords = new HashMap<>();
        keywords.put("Algorithme", ALGORITHM);
        keywords.put("Variables", VARIABLE);
        keywords.put("Debut", BEGIN);
        keywords.put("Fin", END);
        keywords.put("Methode", METHOD);
        keywords.put("Classe", CLASS);
        keywords.put("si", IF);
        keywords.put("sinon", ELSE);
        keywords.put("pour", FOR);
        keywords.put("tant-que", WHILE);
        keywords.put("repeter", DO_WHILE);
        keywords.put("vrai", TRUE);
        keywords.put("faux", FALSE);
        keywords.put("nil", NIL);
        keywords.put("tableau", TABLE);
        keywords.put("entier", INTEGER);
        keywords.put("reel", DOUBLE);
        keywords.put("chaine_character", STRING);

        return keywords;
    }

}
