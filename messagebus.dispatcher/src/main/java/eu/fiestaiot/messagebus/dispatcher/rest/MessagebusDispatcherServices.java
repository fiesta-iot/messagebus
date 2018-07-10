package eu.fiestaiot.messagebus.dispatcher.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Katerina Pechlivanidou (kape) e-mail: kape@ait.gr
 * 
 */
@Path("/messagebusDispatcher")
@Consumes({ "application/xml", "application/json" })
@Produces({ "application/xml", "application/json" })
public class MessagebusDispatcherServices {

	/**
	 * The logger's initialization
	 */
	final static Logger logger = LoggerFactory.getLogger(MessagebusDispatcherServices.class);

	/**
	 * @return a message that should be shown under /messagebusDispatcher
	 */
	@GET()
	@Produces("text/plain")
	public String welcomeMessage() {
		String welcomeText = "Welcome to Message Dispatcher Services\n"
				+ "=============================================================\n\n";
		logger.info(welcomeText);
		return welcomeText;
	}
}
