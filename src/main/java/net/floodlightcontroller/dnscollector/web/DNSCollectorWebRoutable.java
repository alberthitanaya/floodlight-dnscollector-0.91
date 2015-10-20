package net.floodlightcontroller.dnscollector.web;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class DNSCollectorWebRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
        router.attach("/json", DNSCollectorResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/wm/dnscollector";
	}

}
