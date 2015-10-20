package net.floodlightcontroller.dnscollector.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import net.floodlightcontroller.dnscollector.IDNSCollectorService;

public class DNSCollectorResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(DNSCollectorResource.class);
	
	@Get("json")
	public String retrieve() {
		IDNSCollectorService DNSService = (IDNSCollectorService) getContext().getAttributes().
                get(IDNSCollectorService.class.getCanonicalName());
		Gson gson = new Gson();
		String json = gson.toJson(DNSService.getDNSqueries());
		return json;
	}
}
