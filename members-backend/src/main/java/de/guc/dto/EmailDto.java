package de.guc.dto;

public class EmailDto {
    private String subject;
    private String body;
	public String getSubject() {
		return subject;
	}
	public void setSubject(String header) {
		this.subject = header;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
