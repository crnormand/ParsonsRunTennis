package net.parsonsrun;

import javax.mail.*;

public class EmailMessage
{
	private static final long serialVersionUID = 1L;
	
	protected String subject;
	protected String content;
	protected String contentType;
	protected Address[] from;
	
	public EmailMessage(Message m) throws Exception
	{
		setSubject(m.getSubject());
		setContent(Utils.getEmailContent(m.getContent()));
		setContentType(m.getContentType());
		setFrom(m.getReplyTo());
	}
	
	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	
	public String getSubject()
	{
		return subject;
	}
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	public String getContent()
	{
		return content;
	}
	public void setContent(String content)
	{
		this.content = content;
	}
	public Address[] getFrom()
	{
		return from;
	}
	public void setFrom(Address[] from)
	{
		this.from = from;
	}
}
