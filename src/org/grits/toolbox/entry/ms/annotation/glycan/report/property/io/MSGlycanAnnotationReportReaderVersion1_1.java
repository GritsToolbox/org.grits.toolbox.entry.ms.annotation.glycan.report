package org.grits.toolbox.entry.ms.annotation.glycan.report.property.io;

import java.io.File;

import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.jdom.Element;

import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportMetaData;

/**
 * 
 * @author D Brent Weatherly
 *
 */
public class MSGlycanAnnotationReportReaderVersion1_1
{
	public static Property read(Element propertyElement, MSGlycanAnnotationReportProperty property) {
		property.adjustPropertyFilePaths();
		Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
		String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");


		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		String reportFolder = workspaceFolder.substring(0, workspaceFolder.length()-1) 
				+ File.separator
				+ projectName + File.separator
				+ ReportsProperty.getFolder() + File.separator
				+ MSGlycanAnnotationReportProperty.ARCHIVE_FOLDER;
				
		// lets read the settings file
		String settingsFile = property.getMetaDataFile().getName();
		String fullPath = reportFolder + File.separator + settingsFile;
		MSGlycanAnnotationReportMetaData reportMetaData = MSGlycanAnnotationReportProperty.unmarshallSettingsFile(fullPath);
		property.setMsGlycanAnnotReportMetaData(reportMetaData);
		return property;
	}
}
