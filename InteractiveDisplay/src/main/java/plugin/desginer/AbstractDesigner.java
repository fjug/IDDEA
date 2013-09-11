package plugin.desginer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.Nullable;
import model.application.InteractiveDisplayApplicationModel;
import net.imglib2.ui.InteractiveDisplayCanvas;
import net.imglib2.ui.OverlayRenderer;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.gui.filechooser.ExtensionFileFilter;
import plugin.IPlugin;
import plugin.PluginRuntime;
import plugin.compile.CompilerUtils;
import view.display.JHotDrawInteractiveDisplay2D;

import javax.swing.*;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

import static java.lang.System.out;

/**
 * AbstractDesigner provides runtime compilation.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/5/13
 */
public abstract class AbstractDesigner extends JFrame {
    protected InteractiveDisplayApplicationModel model;
    protected final String pluginType;
    protected RSyntaxTextArea textArea;
    protected IPlugin plugin;
    protected HashMap<String, ActionListener> buttons = new HashMap<String, ActionListener>();

    private InteractiveDisplayCanvas canvas;

    protected void load()
    {
        System.out.println("Loaded");
        // Initialize itself when it's compiled.
        if(canvas != null)
        {
            for(OverlayRenderer painter: plugin.getPainters())
                canvas.addOverlayRenderer(painter);
        }
        else
            System.out.println("Canvas is null");
    }
    protected void unload()
    {
        System.out.println("Unloaded");
        if(canvas != null)
        {
            for(OverlayRenderer painter: plugin.getPainters())
                canvas.removeOverlayRenderer(painter);
        }
        else
            System.out.println("Canvas is null");
    }

    protected AbstractDesigner(String pluginType) {
        this.pluginType = pluginType;
        initializeComponents();
    }

    public void setModel(InteractiveDisplayApplicationModel model)
    {
        this.model = model;
        this.canvas = model.getDisplayView().getInteractiveDisplayCanvas();
    }

    protected void initializeComponents()
    {
        JPanel cp = new JPanel(new BorderLayout());

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton loadBtn = new JButton("Load");
        loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = getJavaFileChooser();

                int returnVal = chooser.showOpenDialog(getParent());
                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    String filename = chooser.getSelectedFile().getAbsolutePath();

                    try {
                        FileInputStream fis = new FileInputStream(filename);
                        InputStreamReader in = new InputStreamReader(fis, "UTF-8");

                        textArea.read(in, null);

                        in.close();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });
        bp.add(loadBtn);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = getJavaFileChooser();

                int returnVal = chooser.showSaveDialog(getParent());
                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    String filename = chooser.getSelectedFile().getAbsolutePath();
                    if(!filename.endsWith(".java"))
                    {
                        filename += ".java";
                    }

                    try {
                        FileOutputStream fos = new FileOutputStream(filename);
                        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

                        textArea.write(out);

                        out.close();

                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });
        bp.add(saveBtn);

        JButton compileBtn = new JButton("Compile");
        compileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compile();
            }
        });
        bp.add(compileBtn);

        for(Map.Entry<String, ActionListener> item : buttons.entrySet())
        {
            JButton btn = new JButton(item.getKey());
            btn.addActionListener(item.getValue());
            bp.add(btn);
        }

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setFoldIndicatorEnabled(true);
        cp.add(bp, BorderLayout.NORTH);
        cp.add(sp, BorderLayout.CENTER);

        setContentPane(cp);
        setTitle(pluginType);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void compile() {
        if(plugin != null)
        {
            unload();
        }

        StringWriter writer = new StringWriter();
        try {
            textArea.write(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PluginRuntime runtime = new PluginRuntime();

        String code = writer.toString();

        String className = code.substring(code.indexOf("public class") + 13);
        className = className.substring(0, className.indexOf(" "));

        if(runtime.compile(className, writer.toString()))
        {
            try {
                plugin = runtime.instanciate(className, writer.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            out.println("Compiled successfully.");
            out.println("Plugin name : " + plugin.getName());
            out.println("Plugin author : " + plugin.getAuthor());
            out.println("Plugin version : " + plugin.getVersion());
            out.println("No. of painters : " + plugin.getPainters().size());

            load();
            model.getDisplayView().updateRequest();
        }
    }

    private JFileChooser getJavaFileChooser() {
        JFileChooser c = new JFileChooser();
        ExtensionFileFilter defaultFilter = new ExtensionFileFilter("JavaFile","java");
        c.addChoosableFileFilter(defaultFilter);

        c.setFileFilter(defaultFilter);
        return c;
    }
}
