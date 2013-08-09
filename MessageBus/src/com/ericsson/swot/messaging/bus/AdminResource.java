package com.ericsson.swot.messaging.bus;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.ericsson.swot.messaging.bus.hub.HubPlusStoreManager;
import com.ericsson.swot.messaging.bus.hub.PushRecord;
import com.ericsson.swot.messaging.bus.hub.PushRecord.DeliveryResult;
import com.ericsson.swot.messaging.bus.hub.PushRecordManager;
import com.ericsson.swot.messaging.bus.hub.SubPlus;

public class AdminResource extends ServerResource {

	@Get("html")
	public Representation visualize() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		sb.append("<title>SWoT Messaging Bus Admin</title>\n");
		
		sb.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"5\" width=\"100%\">" +
				"<tr>" +
				"<th>Callback</th>" +
				"<th>Topic</th>" +
				"<th>Subscriber Properties</th>" +
				"<th>Publisher Filtering Predicate</th>" +
				"</tr>");
		for (SubPlus sub : HubPlusStoreManager.getStore().getSubs()) {
			sb.append("<tr>");
			sb.append("<td>" + sub.getCallbackURL() + "</td>");
			sb.append("<td>" + sub.getTopic() + "</td>");
			sb.append("<td>" + sub.getSubProperties() + "</td>");
			sb.append("<td>" + sub.getPubFilterPredicate() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		
		sb.append("</br><table border=\"1\" cellpadding=\"5\" cellspacing=\"5\" width=\"100%\">" +
				"<tr>" +
				"<th>Time</th>" +
				"<th>Topic</th>" +
				"<th>Message</th>" +
				"<th>Deliveries</th>" +
				"</tr>");
		Map<Date, PushRecord> records = PushRecordManager.getManager().getPushRecords();
		for (Entry<Date, PushRecord> entry : records.entrySet()) {
			sb.append("<tr>");
			sb.append("<td>" + entry.getKey() + "</td>");
			sb.append("<td>" + entry.getValue().getTopic() + "</td>");
			sb.append("<td>" + entry.getValue().getMessage() + "</td>");
			sb.append("<td>");
			Map<String, DeliveryResult> results = entry.getValue().getDeliveries();
			for (Entry<String, DeliveryResult> entry1: results.entrySet()) {
				String success = entry1.getValue().success ? "success" : "failure";
				if (entry1.getValue().numTries == 0)
					sb.append(entry1.getKey() + ": " + success + " due to invalid access token</br>");
				else
					sb.append(entry1.getKey() + ": " + success + " after " + entry1.getValue().numTries + " attempts</br>");
			}
			sb.append("</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		
		return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
		
	}
}
