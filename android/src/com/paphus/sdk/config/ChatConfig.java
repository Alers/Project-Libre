package com.paphus.sdk.config;

import java.io.StringWriter;


/**
 * DTO for XML chat config.
 */
public class ChatConfig extends Config {
	
	public String conversation;	
	public boolean correction;
	public boolean offensive;
	public boolean disconnect;
	public String emote;
	public String message;
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<chat");
		writeCredentials(writer);
		if (this.conversation != null) {
			writer.write(" conversation=\"" + this.conversation + "\"");
		}
		if (this.emote != null) {
			writer.write(" emote=\"" + this.emote + "\"");
		}
		if (this.correction) {
			writer.write(" correction=\"" + this.correction + "\"");
		}
		if (this.offensive) {
			writer.write(" offensive=\"" + this.offensive + "\"");
		}
		if (this.disconnect) {
			writer.write(" disconnect=\"" + this.disconnect + "\"");
		}
		writer.write(">");
		
		if (this.message != null) {
			writer.write("<message>");
			writer.write(this.message);
			writer.write("</message>");
		}
		writer.write("</chat>");
		return writer.toString();
	}
}