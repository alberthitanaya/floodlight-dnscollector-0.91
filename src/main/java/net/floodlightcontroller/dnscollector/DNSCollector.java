package net.floodlightcontroller.dnscollector;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.dnscollector.web.DNSCollectorWebRoutable;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.restserver.IRestApiService;


import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

public class DNSCollector implements IOFMessageListener, IFloodlightModule, IDNSCollectorService {
	protected IFloodlightProviderService floodlightProvider;
	protected IRestApiService restApiService;
	protected ArrayList<Map<String,Object>> DNSqueries = new ArrayList<Map<String,Object>>();
	
	@Override
	public String getName() {
		return DNSCollector.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IDNSCollectorService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		m.put(IDNSCollectorService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = 
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		restApiService.addRestletRoutable(new DNSCollectorWebRoutable());
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		switch (msg.getType()){
		case PACKET_IN:
			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
			if (eth.getEtherType() == Ethernet.TYPE_IPv4) {
				IPv4 ipv4 = (IPv4) eth.getPayload();
				if(ipv4.getProtocol() == IPv4.PROTOCOL_UDP){
					UDP udp = (UDP) ipv4.getPayload();
					Short dstPort = udp.getDestinationPort();
					if (dstPort == 53) { //it's a DNS PACKET!
						Data dataPkt = (Data) udp.getPayload();
						byte[] arr = dataPkt.getData();
						StringBuilder strBuilder = new StringBuilder();
						for(int i = 0; i < dataPkt.getData().length; i++) {
							if (this.isPrintableChar((char)arr[i])){
								strBuilder.append((char)arr[i]);
							} else {
								strBuilder.append(".");
							}
							
						}
						strBuilder.delete(0, 13);
						strBuilder.delete(strBuilder.length()-5, strBuilder.length());
						Map<String,Object> hm = new HashMap<String, Object>();
						hm.put("query", strBuilder.toString());
						hm.put("mac", eth.getSourceMACAddress().toString());
						hm.put("switch", sw.getStringId());
						hm.put("time", System.currentTimeMillis());
						this.DNSqueries.add(hm);
					} else {
						break; // NOT A DNS PACKET
					}
				}
			}
		default:
			break;
		}
		return Command.CONTINUE;
	}
	
	@Override 
	public ArrayList<Map<String,Object>> getDNSqueries() {
		ArrayList<Map<String,Object>> result = new ArrayList<Map<String,Object>>();	
		result = (ArrayList<Map<String,Object>>) DNSqueries.clone();
		DNSqueries.clear();
		return result;
	}

	public boolean isPrintableChar( char c ) {
	    Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
	    return (!Character.isISOControl(c)) &&
	            c != KeyEvent.CHAR_UNDEFINED &&
	            block != null &&
	            block != Character.UnicodeBlock.SPECIALS;
	}
}
