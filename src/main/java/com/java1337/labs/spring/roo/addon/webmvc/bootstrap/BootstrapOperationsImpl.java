package com.java1337.labs.spring.roo.addon.webmvc.bootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class BootstrapOperationsImpl extends AbstractOperations implements BootstrapOperations {

    private static final char SEPARATOR = File.separatorChar;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;

    /**
     * Use TypeManagementService to change types
     */
    @Reference private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isInstallBootstrapAvailable() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        return
                projectOperations.isFeatureInstalledInFocusedModule(FeatureNames.MVC) 
            && !projectOperations.isFeatureInstalledInFocusedModule(FeatureNames.JSF)
            &&  fileManager.exists(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF" + SEPARATOR + "tags"))
            &&  fileManager.exists(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF" + SEPARATOR + "views"));

    }

    /** {@inheritDoc} */
    public void setup() {
        // Install the add-on Google code repository needed to get the annotation 
        projectOperations.addRepository("", new Repository("Bootstrap Roo add-on repository", "Bootstrap Roo add-on repository", "https://addon-webmvc-bootstrap.googlecode.com/svn/repo"));
        
        List<Dependency> dependencies = new ArrayList<Dependency>();
        
        // Install the dependency on the add-on jar (
        dependencies.add(new Dependency("com.java1337.labs.spring.roo.addon.webmvc.bootstrap", "com.java1337.labs.spring.roo.addon.webmvc.bootstrap", "0.1.0.BUILD-SNAPSHOT", DependencyType.JAR, DependencyScope.PROVIDED));
        
        // Install dependencies defined in external XML file
        for (Element dependencyElement : XmlUtils.findElements("/configuration/bootstrap/dependencies/dependency", XmlUtils.getConfiguration(getClass()))) {
            dependencies.add(new Dependency(dependencyElement));
        }

        // Add all new dependencies to pom.xml
        projectOperations.addDependencies("", dependencies);
    }

    /**
     * Copies the specified source directory to the destination.
     * <p>
     * Both the source must exist. If the destination does not already exist, it
     * will be created. If the destination does exist, it must be a directory
     * (not a file).
     * 
     * @param source the already-existing source directory (required)
     * @param destination the destination directory (required)
     * @return true if the copy was successful
     */
    protected boolean copyDirectoryContents(File source, File target) {
        return FileUtils.copyRecursively(source, target, false);
    }
}