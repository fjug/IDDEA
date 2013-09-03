package plugin;

import plugin.compile.CachedCompiler;
import plugin.compile.CompilerUtils;
import plugin.compile.JavaSourceFromString;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class PluginRuntime {
    public boolean compile()
    {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        StringWriter writer = new StringWriter();
        PrintWriter printout = new PrintWriter(writer);
        printout.print(composeAProgram("TestPlugin"));
        printout.close();

        JavaFileObject file = new JavaSourceFromString("TestPlugin", writer.toString());

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);

        boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            out.println(diagnostic.getCode());
            out.println(diagnostic.getKind());
            out.println(diagnostic.getPosition());
            out.println(diagnostic.getStartPosition());
            out.println(diagnostic.getEndPosition());
            out.println(diagnostic.getSource());
            out.println(diagnostic.getMessage(null));
        }
        out.println("Success: " + success);

        return success;
    }

    private String composeAProgram( String className )
    {
        final StringBuilder sb = new StringBuilder( 1000 );
        sb.append( "package plugin;\n" );
        sb.append( "import java.util.Date;\n" );
        sb.append( "import plugin.IPlugin;\n" );
        sb.append( "import plugin.ProcessPlugin;\n" );
        sb.append( "public final class " ).append( className ).append( " extends ProcessPlugin\n" );
        sb.append( "{\n" );
        sb.append( "    @Override\n" );
        sb.append( "    public String getName()\n" );
        sb.append( "    {\n" );
        sb.append( "        return \"" );
        sb.append( "TestProcess" );
        sb.append( "\";\n" );
        sb.append( "     }\n" );
        sb.append( "    @Override\n" );
        sb.append( "    public String getAuthor()\n" );
        sb.append( "    {\n" );
        sb.append( "        return \"" );
        sb.append( "HongKee Moon" );
        sb.append( "\";\n" );
        sb.append( "     }\n" );
        sb.append( "    @Override\n" );
        sb.append( "    public String getVersion()\n" );
        sb.append( "    {\n" );
        sb.append( "        return \"" );
        sb.append( "1.0" );
        sb.append( "\";\n" );
        sb.append( "     }\n" );
        sb.append( "}\n" );
        return sb.toString();
    }

    public boolean call() throws
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException
    {
        CachedCompiler cc = CompilerUtils.CACHED_COMPILER;

        final String programText =
                composeAProgram( "TestPlugin" );
        out.println( "PROGRAM TO BE COMPILED" );
        out.println( programText );
        // compile item
        final boolean status = cc.compileCheckFromJava("plugin.TestPlugin", programText);
        out.println( "status of compile: " + status );

        if(status)
        {
            Class pluginClass = cc.loadFromJava("plugin.TestPlugin", programText);

            IPlugin plugin = null;

            try {
                plugin = (IPlugin) pluginClass.newInstance();
            } catch (IllegalAccessException e) {
                System.err.println("Illegal access: " + e);
            }

            out.println( "TestPlugin name is : " + plugin.getName() );
            out.println( "TestPlugin author is : " + plugin.getAuthor() );
            out.println( "TestPlugin version is : " + plugin.getVersion() );
        }

        return true;
    }
}
