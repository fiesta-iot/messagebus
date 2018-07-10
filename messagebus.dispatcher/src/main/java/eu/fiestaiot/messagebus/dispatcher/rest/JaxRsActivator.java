package eu.fiestaiot.messagebus.dispatcher.rest;

import java.util.Set;
import java.util.HashSet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiestaiot.messagebus.dispatcher.impl.dataservices.MessageDispatcherImpl;

/**
 * A class extending {@link Application} and annotated with @ApplicationPath is the Java EE 6 "no XML" approach to activating
 * JAX-RS.
 * 
 * <p>
 * Resources are served relative to the servlet path specified in the {@link ApplicationPath} annotation.
 * </p>
 */
/**
 * @author Katerina Pechlivanidou (kape) e-mail: kape@ait.gr
 * 
 */
@ApplicationPath("/rest")
public class JaxRsActivator extends Application {

	// Initialize the Logger
	final static Logger logger = LoggerFactory.getLogger(JaxRsActivator.class.getName());

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> empty = new HashSet<Class<?>>();

	public JaxRsActivator() {
		singletons.add(new MessagebusDispatcherServices());
		logger.info("Initialize and run the Dispatcher.");

		MessageDispatcherImpl md = new MessageDispatcherImpl();
		md.start();
	}

	@Override
	public Set<Class<?>> getClasses() {
		return empty;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

}
