package net.floodlightcontroller.dnscollector;

import net.floodlightcontroller.core.module.IFloodlightService;
import java.util.ArrayList;
import java.util.Map;

public interface IDNSCollectorService extends IFloodlightService {
	public ArrayList<Map<String,Object>> getDNSqueries();
}

