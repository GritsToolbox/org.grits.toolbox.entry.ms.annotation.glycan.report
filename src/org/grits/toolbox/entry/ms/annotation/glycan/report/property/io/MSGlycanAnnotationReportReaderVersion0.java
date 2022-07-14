package org.grits.toolbox.entry.ms.annotation.glycan.report.property.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.UnsupportedTypeException;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.jdom.Element;

import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportFileInfo;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportMetaData;

/**
 * 
 * @author Brent Weatherly
 *
 */
public class MSGlycanAnnotationReportReaderVersion0 {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportReaderVersion0.class);

	public static Property read(Element propertyElement, MSGlycanAnnotationReportProperty reportProperty) throws IOException, UnsupportedVersionException, UnsupportedTypeException {
		String t_attributeValue = null;
		Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
		String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");

		MSGlycanAnnotationReportMetaData model = new MSGlycanAnnotationReportMetaData();
		reportProperty.setMsGlycanAnnotReportMetaData(model);

		Element child = propertyElement.getChild("ms-annotation-merge");
		if( child != null ) {
			//     <settings derivatisation="" instrument="" experimentType="Direct Infusion" collusionTypeName="" 
			//               collusionEnergy="0.0" adductName="" releaseType="" glycanType="" />

			t_attributeValue = child.getAttributeValue("id");
			model.setReportId(t_attributeValue);
		} 
		if( model.getReportId() == null ) {
			throw new UnsupportedVersionException("Expecting the 'id' attribute", "Preversioned");
		}
		
		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		String reportFolder = workspaceFolder.substring(0, workspaceFolder.length()-1) 
				+ File.separator
				+ projectName + File.separator
				+ ReportsProperty.getFolder() + File.separator
				+ reportProperty.getArchiveFolder();
		
		Element descriptionElement = propertyElement.getChild("descripton");
		String description = descriptionElement == null ? "" : descriptionElement.getValue();
		model.setDescription(description);
		
		String sMetaFileName = model.getReportId() + File.separator + reportProperty.getMetaDataFileName();
		// Ugh. Hacky fix to the naming scheme that also becomes a path name. If created on windows and opened on linux (or vice-versa), then the 
		// file name will be invalid. So we have to correct the separator character
		if( sMetaFileName.contains("\\") && ! File.separator.equals("\\") ) {
			sMetaFileName = sMetaFileName.replace("\\", File.separator);
		} else if( sMetaFileName.contains("/") && ! File.separator.equals("/") ){
			sMetaFileName = sMetaFileName.replace("/", File.separator);
		}
		String sSettingsPath = reportFolder + File.separator + sMetaFileName;
		
		MSGlycanAnnotationReportProperty.marshallSettingsFile(sSettingsPath, model);
		model.setVersion(MSGlycanAnnotationReportMetaData.CURRENT_VERSION);
		model.setName(sMetaFileName);
		PropertyDataFile msMetaData = MSGlycanAnnotationReportProperty.getNewSettingsFile(sMetaFileName, model);
		reportProperty.getDataFiles().add(msMetaData);
		
		String sReportFile = model.getReportId() + File.separator + reportProperty.getArchiveFile();
		File file = new File( reportFolder + File.separator + sReportFile );
		PropertyDataFile pdf = null;
		if( file.exists() ) { // archive is a single zip file
			pdf = new PropertyDataFile(sReportFile, MSGlycanAnnotationReportFileInfo.MS_GLYCAN_ANNOTATION_REPORT_CURRENT_VERSION, 
					MSGlycanAnnotationReportFileInfo.MS_GLYCAN_ANNOTATION_REPORT_TYPE_XML);
		} 
		
		if( pdf == null ) {
			throw new UnsupportedVersionException("Expecting an archive file or folder. Not found.", "Preversion");
		}
		reportProperty.getDataFiles().add(pdf);
		return reportProperty;
	}
}
