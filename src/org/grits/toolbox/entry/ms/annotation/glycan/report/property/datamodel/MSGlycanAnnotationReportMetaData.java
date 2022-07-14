package org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel;

import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantFileToAlias;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
@XmlRootElement(name = "msMetaData")
//@XmlType(propOrder={"name", "description", "version", "reportId", "creationDate", "updateDate"})
public class MSGlycanAnnotationReportMetaData {
	public static final String CURRENT_VERSION = "1.1";
	private String name = null;
	private String version = null;
	private String description = null;
	private String reportId = null;
	private Date creationDate = null;
	private Date updateDate = null;
	
	/* expToExternalQuant:
	 * key1: experiment id
	 * 
	 * key2: peak custom extra data key
	 * value: ExternalQuantAlias
	 * 
	 */
	private HashMap<String, ExternalQuantFileToAlias> expToExternalQuant = new HashMap<>(); 
		
	
	public void setExpToExternalQuant(HashMap<String, ExternalQuantFileToAlias> expToExternalQuant) {
		this.expToExternalQuant = expToExternalQuant;
	}
	@XmlElement(name="expToExternalQuant")
	public HashMap<String, ExternalQuantFileToAlias> getExpToExternalQuant() {
		return expToExternalQuant;
	}
	
	/**
	 * @return the name
	 */
	@XmlAttribute(name = "name", required= true)
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	@XmlElement(name = "description", required= false)
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the version
	 */
	@XmlAttribute(name = "version", required= true)
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the reportId
	 */
	@XmlAttribute(name = "reportId", required= true)
	public String getReportId() {
		return reportId;
	}

	/**
	 * @param reportId the reportId to set
	 */
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	
	/**
	 * @param creationDate
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	/**
	 * @return the time when the report was created
	 */
	@XmlAttribute(name = "creationDate")	
	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * @param updateDate
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	/**
	 * @return the time when the report was last updated
	 */
	@XmlAttribute(name = "updateDate")	
	public Date getUpdateDate() {
		return updateDate;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		MSGlycanAnnotationReportMetaData newSettings = new MSGlycanAnnotationReportMetaData();
		
		newSettings.setDescription(this.getDescription());
		newSettings.setReportId(this.getReportId());
		newSettings.setVersion(this.getVersion());
		newSettings.setDescription(this.getDescription());
		newSettings.setCreationDate(this.getCreationDate());
		newSettings.setUpdateDate(newSettings.getCreationDate());
		return newSettings;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof MSGlycanAnnotationReportMetaData) )
			return false;
		
		MSGlycanAnnotationReportMetaData castObj = (MSGlycanAnnotationReportMetaData) obj;
		boolean bRes = getDescription() != null && getDescription().equals( castObj.getDescription() );
		bRes &= getReportId() != null && getReportId().equals( castObj.getReportId() );
		return bRes;
	}

}
