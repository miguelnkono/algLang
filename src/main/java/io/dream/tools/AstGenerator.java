package io.dream.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class AstGenerator
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("Usage: AstGenerator <filename>");
            System.exit(64);
        }

        String outputDirectory = args[0];

        // define the ast tree.
        defineAst(outputDirectory, "Expression", Arrays.asList(
                "Binary : Expression left, Token operator, Expression right",
                "Grouping : Expression expression",
                "Unary : Token operator, Expression expression",
                "Literal : Object value"
        ));
    }

    /**
     * Create a new file in the output directory with a given name and containing all the
     * subclasses that will construct the ast tree.
     *
     * @param outputDirectory  the name of the directory where the "name" tree will be created.
     * @param name represent the name of the parent class for all the subclasses for the tree.
     * @param types the description of the of each types present in the tree.
     * */
    private static void defineAst(String outputDirectory, String name, List<String> types) throws IOException
    {
        String filePath = outputDirectory + "/" + name + ".java";

        try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8))
        {
            writer.println("package io.dream.ast;\n");   // write the package name
            writer.println("import java.util.List;\n"); // import some java class : List
            writer.println("import io.dream.scanner.Token;\n"); // import some java class : Token
            writer.println("abstract class " + name + "\n{");

            // we define each subclasses
            for (String type : types)
            {
                String subclassName = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer, name, subclassName, fields);
            }

            writer.println("}");
        }
        catch (FileNotFoundException e)
        {
            System.err.println("File not found: " + filePath);
        }
    }

    /**
     * This is the function that will actually define the subclasses of the base class with all
     * of its fields.
     *
     * @param writer the writer pointer that will allow us to write into the baseName file.
     * @param baseName the base name on which all the subclasses will inherit from.
     * @param subclassName the name of the base class that will inherit the base class.
     * @param fieldList the fields of the subclass.
     * */
    private static void defineType(PrintWriter writer, String baseName, String subclassName,
                                   String fieldList)
    {
        // static class Binary extends Expression {}
        writer.printf("    static class %s extends %s \n    {\n", subclassName, baseName);

        // Binary (Expression left, Token operator, Expression right){}
        writer.printf("        %s (%s)\n        {\n", subclassName, fieldList);
        // the parameters in the constructor of the subclass.
        String[] fields = fieldList.split(", ");
        for (String field : fields)
        {
            String name = field.split(" ")[1];
            writer.printf("            this.%s = %s;\n", name, name);
        }
        writer.println("        }\n");

        // fields
        for (String field : fields)
        {
            writer.printf("        final %s;\n", field);
        }

        writer.println("    }\n");
    }
}
