package eu.fiestaiot.messagebus.dispatcher.impl.dataservices;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "MessageModel")
public class MessageModel {

	private List<String> observations;

	public MessageModel() {
		observations = new ArrayList<String>();
	}

	public MessageModel(List<String> list) {
		this.observations = new ArrayList<String>(list);
	}

	public List<String> getSensorIds() {
		return observations;
	}

	@XmlElementWrapper(name="context")
	@XmlElement(name = "context")
	@JsonProperty(value = "context")
	public void setSensorIds(List<String> list) {
		this.observations = list;
	}
}