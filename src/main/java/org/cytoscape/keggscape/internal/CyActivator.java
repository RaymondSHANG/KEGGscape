package org.cytoscape.keggscape.internal;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.ID;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.keggscape.internal.read.kgml.KeggscapeNetworkReaderFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.osgi.framework.BundleContext;

/**
 * {@code CyActivator} is a class that is a starting point for OSGi bundles.
 * 
 * A quick overview of OSGi: The common currency of OSGi is the <i>service</i>.
 * A service is merely a Java interface, along with objects that implement the
 * interface. OSGi establishes a system of <i>bundles</i>. Most bundles import
 * services. Some bundles export services. Some do both. When a bundle exports a
 * service, it provides an implementation to the service's interface. Bundles
 * import a service by asking OSGi for an implementation. The implementation is
 * provided by some other bundle.
 * 
 * When OSGi starts your bundle, it will invoke {@CyActivator}'s
 * {@code start} method. So, the {@code start} method is where
 * you put in all your code that sets up your app. This is where you import and
 * export services.
 * 
 * Your bundle's {@code Bundle-Activator} manifest entry has a fully-qualified
 * path to this class. It's not necessary to inherit from
 * {@code AbstractCyActivator}. However, we provide this class as a convenience
 * to make it easier to work with OSGi.
 *
 * Note: AbstractCyActivator already provides its own {@code stop} method, which
 * {@code unget}s any services we fetch using getService().
 */
public class CyActivator extends AbstractCyActivator {
	/**
	 * This is the {@code start} method, which sets up your app. The
	 * {@code BundleContext} object allows you to communicate with the OSGi
	 * environment. You use {@code BundleContext} to import services or ask OSGi
	 * about the status of some service.
	 */
	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {

		// importing services
		final StreamUtil streamUtil = getService(bc, StreamUtil.class);
		final CyNetworkViewFactory cyNetworkViewFactory = getService(bc, CyNetworkViewFactory.class);
		final CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);
		final CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
		final CyRootNetworkManager cyRootNetworkManager = getService(bc, CyRootNetworkManager.class);
		
		VisualStyleFactory vsFactoryServiceRef = getService(bc, VisualStyleFactory.class); 
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		VisualMappingFunctionFactory discreteMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete");
		
		KGMLVisualStyleBuilder vsBuilder = new KGMLVisualStyleBuilder(vsFactoryServiceRef,
				discreteMappingFactoryRef, passthroughMappingFactoryRef);
		
		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		
		// readers
		final BasicCyFileFilter keggscapeReaderFilter = new BasicCyFileFilter(new String[] { "xml" },
				new String[] { "application/xml" }, "KGML format", DataCategory.NETWORK, streamUtil);
		final KeggscapeNetworkReaderFactory kgmlReaderFactory = new KeggscapeNetworkReaderFactory(
				keggscapeReaderFilter, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager,
				vsBuilder, vmm);
		final Properties keggscapeNetworkReaderFactoryProps = new Properties();


		// Configure the service properties first.
//		Properties properties = new Properties();

		// Our task should be exposed in the "Apps" menu...
//		properties.put(ServiceProperties.PREFERRED_MENU,
//			ServiceProperties.APPS_MENU);

		// ... as a sub menu item called "Say Hello".
//		properties.put(ServiceProperties.TITLE, "KEGGscape");

		// Our menu item should only be enabled if at least one network
		// view exists.
//		properties.put(ServiceProperties.ENABLE_FOR, "always");

		keggscapeNetworkReaderFactoryProps.put(ID, "keggscapeNetworkReaderFactory");

		registerService(bc, kgmlReaderFactory, InputStreamTaskFactory.class, keggscapeNetworkReaderFactoryProps);
			//new KeggscapeTaskFactory(), // Implementation
//			TaskFactory.class, // Interface
//			properties); // Service properties
	}
}

