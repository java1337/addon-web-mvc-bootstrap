package com.java1337.labs.spring.roo.addon.webmvc.bootstrap;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class BootstrapOperationsImpl extends AbstractOperations implements BootstrapOperations {

    private static final char SEPARATOR = File.separatorChar;
    private static final String WEBMVC_CONFIG_XML = "WEB-INF/spring/webmvc-config.xml";

    @Reference private FileManager fileManager;
    @Reference private PathResolver pathResolver;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;

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
        // Parse the configuration.xml file
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Add POM properties
        updatePomProperties(configuration);

        // Add dependencies to POM
        updateDependencies(configuration);

        // Import custom webmvc-config-additions.xml into webmvc-config.xml
        updateWebMvcConfig();
    }

//    protected void updateWebMvcConfigOld() {
//
//        final String webConfigFile = pathResolver.getFocusedIdentifier(
//            Path.SRC_MAIN_WEBAPP,
//            WEBMVC_CONFIG_XML);
//
//        final LogicalPath webappPath = Path.SRC_MAIN_WEBAPP.getModulePathId(projectOperations.getFocusedModuleName());
//
//        InputStream webConfigStream = fileManager.getInputStream(webConfigFile);
//        Document webConfigDocument = XmlUtils.readXml(webConfigStream);
//        Element rootElement = webConfigDocument.getDocumentElement();
//        Element importElement = XmlUtils.findFirstElement("/beans/import/[@resource='web-config-bootstrap.xml']", rootElement);
//        if (importElement == null) {
//            try {
//                Element element = webConfigDocument.createElement("import");
//                element.setAttribute("resource", "web-config-bootstrap.xml");
//                rootElement.appendChild(element);
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                XmlUtils.writeXml(outputStream, webConfigDocument);
//                String newContents = new String(outputStream.toByteArray(),"UTF-8");
//                fileManager.createOrUpdateTextFileIfRequired(webConfigFile, newContents, "Added import of web-config-bootstrap.xml", true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        final String webinfSpringDir = pathResolver.getFocusedIdentifier(
//            Path.SRC_MAIN_WEBAPP,
//            WEBINF_SPRING_DIR);
//        copyTemplate("web-config-bootstrap.xml", webinfSpringDir);
//    }
 
    protected void updateWebMvcConfig() {
        // Update webmvc-config.xml if needed.
        final String webConfigFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, WEBMVC_CONFIG_XML);
        Validate.isTrue(fileManager.exists(webConfigFile),
                "Aborting: Unable to find %s", webConfigFile);
        InputStream webMvcConfigInputStream = null;
        try {
            webMvcConfigInputStream = fileManager.getInputStream(webConfigFile);
            Validate.notNull(webMvcConfigInputStream, "Aborting: Unable to acquire webmvc-config.xml file");
            final Document webMvcConfig = XmlUtils
                    .readXml(webMvcConfigInputStream);
            final Element root = webMvcConfig.getDocumentElement();
            if (XmlUtils.findFirstElement("/beans/import/[@resource='webmvc-config-additions.xml']", root) == null) {
                final InputStream templateInputStream 
                    = FileUtils.getInputStream(getClass(), "webmvc-config-additions.xml");
                Validate.notNull(templateInputStream, "Could not acquire webmvc-config-additions.xml template");
                final Document webMvcConfigAdditions = XmlUtils.readXml(templateInputStream);
                final NodeList nodes = webMvcConfigAdditions.getDocumentElement().getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    root.appendChild(webMvcConfig.importNode(nodes.item(i), true));
                }
                fileManager.createOrUpdateTextFileIfRequired(webConfigFile, XmlUtils.nodeToString(webMvcConfig), true);
            }
        }
        finally {
            IOUtils.closeQuietly(webMvcConfigInputStream);
        }
    }


    // Add/Update POM properties, such as version numbers
    protected void updatePomProperties(Element configuration) {
        List<Element> properties = XmlUtils.findElements("/configuration/bootstrap/properties/*", configuration);
        for (Element property : properties) {
            projectOperations.addProperty(projectOperations.getFocusedModuleName(), new Property(property));
        }
    }

    // Install dependencies defined in external XML file
    protected void updateDependencies(Element configuration) {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        List<Element> jamonDependencies = XmlUtils.findElements("/configuration/bootstrap/dependencies/dependency", configuration);
        for (Element dependencyElement : jamonDependencies) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.addDependencies(projectOperations.getFocusedModuleName(), dependencies);
    }
}