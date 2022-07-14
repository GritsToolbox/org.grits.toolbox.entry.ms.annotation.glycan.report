package org.grits.toolbox.entry.ms.annotation.glycan.report.property.io;

import java.io.IOException;

import org.grits.toolbox.core.datamodel.UnsupportedTypeException;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.jdom.Element;

/**
 * Reader for sample entry. Should check for empty values
 * @author Brent Weatherly
 *
 */
public class MSGlycanAnnotationReportPropertyReader extends PropertyReader {
	@Override
	public Property read(Element propertyElement) throws IOException, UnsupportedVersionException
	{
		MSGlycanAnnotationReportProperty property = getNewMSGlycanAnnotationReportProperty();
		
		PropertyReader.addGenericInfo(propertyElement, property);

		if(property.getVersion() == null) {
			// we must also convert the meta-data to the model and write out. Do that here?
			try {
				MSGlycanAnnotationReportReaderVersion0.read(propertyElement, property);
				property.setVersion(MSGlycanAnnotationReportProperty.CURRENT_VERSION);
				PropertyReader.UPDATE_PROJECT_XML = true;
			} catch (UnsupportedTypeException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		else if(property.getVersion().equals("1.0")) {
			MSGlycanAnnotationReportReaderVersion1.read(propertyElement, property);
		}
		else if(property.getVersion().equals("1.1")) {
			MSGlycanAnnotationReportReaderVersion1_1.read(propertyElement, property);
		}
		else 
			throw new UnsupportedVersionException("This version is currently not supported.", property.getVersion());
		return property;
	}
	
	protected MSGlycanAnnotationReportProperty getNewMSGlycanAnnotationReportProperty() {
		return new MSGlycanAnnotationReportProperty();
	}
}
