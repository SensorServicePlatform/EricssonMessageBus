package com.ericsson.swot.messaging.peer.restlet;

import java.util.ArrayList;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.ericsson.swot.messaging.peer.MessagingPeer;

public class WorkflowDemoResource extends ServerResource {

	@Get("html")
	public Representation visualize() {

		MessagingPeer peer = ((DefaultMessagingPeerApp)getApplication()).getPeer();
		
		String peerType = peer.peerType;
		
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		sb.append("<title>Demo</title>\n");
		sb.append("<h2>Peer Type: " + peerType + "</h2>");
		
		if(peerType.equals("room-temperature1")) {
		
//			sb.append("<p>Temperature Sensor 1: " + peer.temp_sensor1 + "</p>");
			sb.append("<p>Interval: " + peer.interval + "s</p>");
		
		} else if(peerType.equals("room-temperature2")) {
			
//			sb.append("<p>Temperature Sensor 2: " + peer.temp_sensor2 + "</p>");
			sb.append("<p>Interval: " + peer.interval + "s</p>");
		
		} else if(peerType.equals("blood-pressure")) {
			
//			sb.append("<p>Blood Pressure: " + peer.blood_pressure + "</p>");
			sb.append("<p>Interval: " + peer.interval + "s</p>");

			
		} else if(peerType.equals("heart-rate")) {
			
//			sb.append("<p>Heart Rate: " + peer.heart_rate + "</p>");
			sb.append("<p>Interval: " + peer.interval + "s</p>");
			
		} else if(peerType.equals("body-temperature")) {
			
//			sb.append("<p>Body temperature: " + peer.body_temp + "</p>");
			sb.append("<p>Interval: " + peer.interval + "s</p>");
			
		}  else if(peerType.equals("virtual-sensor1")) {
			
			sb.append("<p>Virtual Sensor: </p>");
			sb.append("<p>Temperature Sensor 1: " + peer.temp_sensor1 + "</p>");
			sb.append("<p>Temperature Sensor 2: " + peer.temp_sensor2 + "</p>");
			sb.append("<p>Average Temperature: " + peer.avg_temp + "</p>");
			sb.append("<p>Interval: " + peer.interval + "s</p>");
			
		} else if(peerType.equals("virtual-device1")) {
			
			sb.append("<p>Virtual Device: </p>");
			sb.append("<p>Temperature Sensor 1: " + peer.temp_sensor1 + "</p>");
			sb.append("<p>Temperature Sensor 2: " + peer.temp_sensor2 + "</p>");
			sb.append("<p>Average Temperature: " + peer.avg_temp + "</p>");
			sb.append("<p>Blood Pressure: " + peer.blood_pressure + "</p>");
			sb.append("<p>Heart Rate: " + peer.heart_rate + "</p>");
			sb.append("<p>Body temperature: " + peer.body_temp + "</p>");
			sb.append("<p>Interval: " + peer.interval + "s</p>");
			
		} else if(peerType.equals("listener")) {

			sb.append("<p>Interval: " + peer.interval + "s</p>");
			
			if(peer.msgHistory.isEmpty()) {
				sb.append("<p>msghistory is empty</p>");
			} else {
//				for(ArrayList<String> s : peer.msgHistory) {
//					sb.append("<p>" + "haha" + "</p>");
//				}
			}
			
		}
		
		return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);	
	}
}
