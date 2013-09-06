package plugin.compile;

/**
 * Thanks to http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
 * ReloadableClassLoader is used.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/6/13
 */
public class ReloadableClassLoader extends ClassLoader{

    public ReloadableClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

}
