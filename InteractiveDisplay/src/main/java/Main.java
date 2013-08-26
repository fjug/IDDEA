import model.InteractiveDisplayApplicationModel;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.OSXApplication;
import org.jhotdraw.app.SDIApplication;
import org.jhotdraw.util.ResourceBundleUtil;


/**
 * Main class instanciate a model with a view.
 *
 * @version 0.1beta
 * @since 8/12/13 5:13 PM
 * @author HongKee Moon
 */

public class Main {

    public static void main( final String[] args )
    {
        ResourceBundleUtil.setVerbose(true);

        Application app;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("mac")) {
            app = new OSXApplication();
        } else if (os.startsWith("win")) {
            //app = new MDIApplication();
            app = new SDIApplication();
        } else {
            app = new SDIApplication();
        }

        InteractiveDisplayApplicationModel model = new InteractiveDisplayApplicationModel();
//        model.setViewClass(InteractiveDisplayView.class);
        model.setViewClassName("view.InteractiveDisplayView");
        model.setName("IDDEA Draw");
//        model.setVersion(Main.class.getPackage().getImplementationVersion());
        model.setVersion("0.1beta");
        model.setCopyright("Copyright 2013 (c) by the authors of IDDEA and all its contributors.\n" +
                "This software is licensed under LGPL or Creative Commons 3.0 Attribution.");
        app.setModel(model);
        app.launch(args);
    }
}
