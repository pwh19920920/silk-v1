
package com.spark.bitrade.util;

import org.apache.ibatis.io.DefaultVFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MyResolverUtil<T> {
    private static final Log log = LogFactory.getLog(MyResolverUtil.class);
    private Set<Class<? extends T>> matches = new HashSet();
    private ClassLoader classloader;

    public MyResolverUtil() {
    }

    public Set<Class<? extends T>> getClasses() {
        return this.matches;
    }

    public ClassLoader getClassLoader() {
        return this.classloader == null?Thread.currentThread().getContextClassLoader():this.classloader;
    }

    public void setClassLoader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public MyResolverUtil<T> findImplementations(Class<?> parent, String... packageNames) {
        if(packageNames == null) {
            return this;
        } else {
            MyResolverUtil.Test test = new MyResolverUtil.IsA(parent);
            String[] var4 = packageNames;
            int var5 = packageNames.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String pkg = var4[var6];
                this.find(test, pkg);
            }

            return this;
        }
    }

    public MyResolverUtil<T> findAnnotated(Class<? extends Annotation> annotation, String... packageNames) {
        if(packageNames == null) {
            return this;
        } else {
            MyResolverUtil.Test test = new MyResolverUtil.AnnotatedWith(annotation);
            String[] var4 = packageNames;
            int var5 = packageNames.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String pkg = var4[var6];
                this.find(test, pkg);
            }

            return this;
        }
    }

    public MyResolverUtil<T> find(MyResolverUtil.Test test, String packageName) {
        DefaultVFS defaultVFS = new DefaultVFS() {
            @Override
            protected URL findJarForResource (URL url) throws MalformedURLException {
                String urlStr = url.toString ();
                if (urlStr.contains ("jar!")) {
                    return new URL (urlStr.substring (0, urlStr.lastIndexOf ("jar") + "jar".length ()));
                }
                return super.findJarForResource (url);
            }
        };

        String path = this.getPackagePath(packageName);
        try {
            List<String> children = defaultVFS.list (path);
//            List<String> children = SpringBootVFS.getInstance().list(path);
            Iterator var5 = children.iterator();

            while(var5.hasNext()) {
                String child = (String)var5.next();
                if(child.endsWith(".class")) {
                    this.addIfMatching(test, child);
                }
            }
        } catch (IOException var7) {
            log.error("Could not read package: " + packageName, var7);
        }

        return this;
    }

    protected String getPackagePath(String packageName) {
        return packageName == null?null:packageName.replace('.', '/');
    }

    protected void addIfMatching(MyResolverUtil.Test test, String fqn) {
        try {
            String externalName = fqn.substring(0, fqn.indexOf(46)).replace('/', '.');
            ClassLoader loader = this.getClassLoader();
            if(log.isDebugEnabled()) {
                log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");
            }

            Class<? extends T> type = (Class<? extends T>)loader.loadClass(externalName);
            if(test.matches(type)) {
                this.matches.add(type);
            }
        } catch (Throwable var6) {
            log.warn("Could not examine class '" + fqn + "' due to a " + var6.getClass().getName() + " with message: " + var6.getMessage());
        }

    }

    public static class AnnotatedWith implements MyResolverUtil.Test {
        private Class<? extends Annotation> annotation;

        public AnnotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }

        public boolean matches(Class<?> type) {
            return type != null && type.isAnnotationPresent(this.annotation);
        }

        public String toString() {
            return "annotated with @" + this.annotation.getSimpleName();
        }
    }

    public static class IsA implements MyResolverUtil.Test {
        private Class<?> parent;

        public IsA(Class<?> parentType) {
            this.parent = parentType;
        }

        public boolean matches(Class<?> type) {
            return type != null && this.parent.isAssignableFrom(type);
        }

        public String toString() {
            return "is assignable to " + this.parent.getSimpleName();
        }
    }

    public interface Test {
        boolean matches(Class<?> var1);
    }
}
