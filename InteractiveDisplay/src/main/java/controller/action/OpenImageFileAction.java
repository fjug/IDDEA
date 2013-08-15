package controller.action;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.jhotdraw.app.action.*;
import org.jhotdraw.app.*;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.JSheet;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.gui.Worker;
import org.jhotdraw.gui.filechooser.ExtensionFileFilter;
import org.jhotdraw.net.URIUtil;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.ApplicationModel;
import org.jhotdraw.app.View;
import org.jhotdraw.util.ResourceBundleUtil;

import java.awt.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.prefs.Preferences;

import model.*;
import org.jhotdraw.util.prefs.PreferencesUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 8/13/13 2:56 PM
 * @author HongKee Moon
 */
public class OpenImageFileAction extends AbstractApplicationAction
{
    public final static String ID = "file.openImageFile";
    private URIChooser openChooser;

    /** Creates a new instance. */
    public OpenImageFileAction(Application app) {
        this(app,ID);
    }
    public OpenImageFileAction(Application app, String id) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("model.Labels");
        labels.configureAction(this, id);
    }

    protected URIChooser getChooser(View view) {
        // Note: We pass null here, because we want the application-wide chooser
        //return getApplication().getOpenChooser(null);
        return getOpenChooser(null);
    }

    protected URIChooser getOpenChooser(@Nullable View v) {
        if(openChooser == null)
        {
            JFileURIChooser c = new JFileURIChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Images","tif", "tiff", "jpg", "jpeg", "png");
            c.setFileFilter(filter);
            c.getComponent().putClientProperty("application", this);

            openChooser = c;
        }

        return openChooser;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        final Application app = getApplication();
        if (app.isEnabled()) {
            app.setEnabled(false);
            // Search for an empty view
            View emptyView = app.getActiveView();
            if (emptyView == null
                    || !emptyView.isEmpty()
                    || !emptyView.isEnabled()) {
                emptyView = null;
            }

            final View view;
            boolean disposeView;
            if (emptyView == null) {
                view = app.createView();
                app.add(view);
                disposeView = true;
            } else {
                view = emptyView;
                disposeView = false;
            }
            URIChooser chooser = getChooser(view);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            if (showDialog(chooser, app.getComponent()) == JFileChooser.APPROVE_OPTION) {
                app.show(view);

                URI uri = chooser.getSelectedURI();

                for (View v : getApplication().views()) {
                    if (v.getURI() != null && v.getURI().equals(uri)) {
                        v.getComponent().requestFocus();
                        if (disposeView) {
                            app.dispose(view);
                        }
                        app.setEnabled(true);
                        return;
                    }
                }

                openViewFromURI(view, uri, chooser);
            } else {
                if (disposeView) {
                    app.dispose(view);
                }
                app.setEnabled(true);
            }
        }
    }

    protected void openViewFromURI(final View view, final URI uri, final URIChooser chooser) {
        final Application app = getApplication();
        app.setEnabled(true);
        view.setEnabled(false);

        // If there is another view with the same URI we set the multiple open
        // id of our view to max(multiple open id) + 1.
        int multipleOpenId = 1;
        for (View aView : app.views()) {
            if (aView != view
                    && aView.isEmpty()) {
                multipleOpenId = Math.max(multipleOpenId, aView.getMultipleOpenId() + 1);
            }
        }
        view.setMultipleOpenId(multipleOpenId);
        view.setEnabled(false);

        // Open the file
        view.execute(new Worker() {

            @Override
            public Object construct() throws IOException {
                boolean exists = true;
                try {
                    exists = new File(uri).exists();
                } catch (IllegalArgumentException e) {
                }
                if (exists) {
                    //TODO: Consider the better way to handle images
                    //view.read(uri, chooser);
                    view.setURI(uri);
                    return null;
                } else {
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
                    throw new IOException(labels.getFormatted("file.open.fileDoesNotExist.message", URIUtil.getName(uri)));
                }
            }

            @Override
            protected void done(Object value) {
                final Application app = getApplication();
                //view.setURI(uri);
                view.setEnabled(true);
                Frame w = (Frame) SwingUtilities.getWindowAncestor(view.getComponent());
                if (w != null) {
                    w.setExtendedState(w.getExtendedState() & ~Frame.ICONIFIED);
                    w.toFront();
                }
                view.getComponent().requestFocus();
                app.setEnabled(true);
            }

            @Override
            protected void failed(Throwable value) {
                value.printStackTrace();
                view.setEnabled(true);
                app.setEnabled(true);
                String message = value.getMessage() != null ? value.getMessage() : value.toString();
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
                JSheet.showMessageSheet(view.getComponent(),
                        "<html>" + UIManager.getString("OptionPane.css")
                                + "<b>" + labels.getFormatted("file.open.couldntOpen.message", URIUtil.getName(uri)) + "</b><p>"
                                + ((message == null) ? "" : message),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /** We implement JFileChooser.showDialog by ourselves, so that we can center
     * dialogs properly on screen on Mac OS X.
     */
    public int showDialog(URIChooser chooser, Component parent) {
        final Component finalParent = parent;
        final int[] returnValue = new int[1];
        final JDialog dialog = createDialog(chooser, finalParent);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                returnValue[0] = JFileChooser.CANCEL_OPTION;
            }
        });
        chooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ("CancelSelection".equals(e.getActionCommand())) {
                    returnValue[0] = JFileChooser.CANCEL_OPTION;
                    dialog.setVisible(false);
                } else if ("ApproveSelection".equals(e.getActionCommand())) {
                    returnValue[0] = JFileChooser.APPROVE_OPTION;
                    dialog.setVisible(false);
                }
            }
        });
        returnValue[0] = JFileChooser.ERROR_OPTION;
        chooser.rescanCurrentDirectory();

        dialog.setVisible(true);
        //chooser.firePropertyChange("JFileChooserDialogIsClosingProperty", dialog, null);
        dialog.removeAll();
        dialog.dispose();
        return returnValue[0];
    }

    /** We implement JFileChooser.showDialog by ourselves, so that we can center
     * dialogs properly on screen on Mac OS X.
     */
    protected JDialog createDialog(URIChooser chooser, Component parent) throws HeadlessException {
        String title = chooser.getDialogTitle();
        if (chooser instanceof JFileChooser) {
            ((JFileChooser) chooser).getAccessibleContext().setAccessibleDescription(title);
        }

        JDialog dialog;
        Window window = (parent == null || (parent instanceof Window)) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        dialog = new JDialog(window, title, Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setComponentOrientation(chooser.getComponent().getComponentOrientation());

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooser.getComponent(), BorderLayout.CENTER);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations =
                    UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }
        }
        //dialog.pack();
        Preferences prefs = PreferencesUtil.userNodeForPackage(getApplication().getModel().getClass());

        PreferencesUtil.installFramePrefsHandler(prefs, "openChooser", dialog);
        /*
        if (window.getBounds().isEmpty()) {
        Rectangle screenBounds = window.getGraphicsConfiguration().getBounds();
        dialog.setLocation(screenBounds.x + (screenBounds.width - dialog.getWidth()) / 2, //
        screenBounds.y + (screenBounds.height - dialog.getHeight()) / 3);
        } else {
        dialog.setLocationRelativeTo(parent);
        }*/

        return dialog;
    }
}
