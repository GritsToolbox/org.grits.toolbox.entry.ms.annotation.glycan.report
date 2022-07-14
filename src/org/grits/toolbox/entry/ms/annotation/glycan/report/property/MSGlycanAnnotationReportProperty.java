package org.grits.toolbox.entry.ms.annotation.glycan.report.property;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.DeleteUtils;
import org.grits.toolbox.core.utilShare.XMLUtils;
import org.grits.toolbox.entry.ms.ImageRegistry;
import org.grits.toolbox.entry.ms.annotation.glycan.report.Activator;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportFileInfo;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportMetaData;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.io.MSGlycanAnnotationReportPropertyWriter;

public class MSGlycanAnnotationReportProperty extends Property {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportProperty.class);

	public static final String TYPE = "org.grits.toolbox.property.report.ms_annotation_merge";
	protected static PropertyWriter writer = new MSGlycanAnnotationReportPropertyWriter();
	public static final String ARCHIVE_FOLDER = "merge";
	public static final String ARCHIVE_EXTENSION = ".xml";
	protected static ImageDescriptor imageDescriptor = ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID,
			ImageRegistry.MSImage.MSMERGE_ICON);
	private static final String META_DATA_FILE = "msGlycanAnnotReportMetaData.xml";
	private MSGlycanAnnotationReportMetaData msGlycanAnnotReportMetaData = null;

	public MSGlycanAnnotationReportProperty() {
		super();
	}

	@Override
	public String getType() {
		return MSGlycanAnnotationReportProperty.TYPE;
	}

	@Override
	public PropertyWriter getWriter() {
		return MSGlycanAnnotationReportProperty.writer;
	}

	@Override
	public ImageDescriptor getImage() {
		return MSGlycanAnnotationReportProperty.imageDescriptor;
	}

	public MSGlycanAnnotationReportMetaData getMsGlycanAnnotReportMetaData() {
		return msGlycanAnnotReportMetaData;
	}

	public void setMsGlycanAnnotReportMetaData(MSGlycanAnnotationReportMetaData msGlycanAnnotReportMetaData) {
		this.msGlycanAnnotReportMetaData = msGlycanAnnotReportMetaData;
	}

	public static PropertyDataFile getNewSettingsFile(String msAnnotDetails,
			MSGlycanAnnotationReportMetaData metaData) {
		PropertyDataFile msMetaData = new PropertyDataFile(msAnnotDetails, metaData.getVersion(),
				MSGlycanAnnotationReportFileInfo.MS_GLYCAN_ANNOTATION_REPORT_METADATA_TYPE);
		return msMetaData;
	}

	public void adjustPropertyFilePaths() {
		Iterator<PropertyDataFile> itr = getDataFiles().iterator();
		while (itr.hasNext()) {
			PropertyDataFile file = itr.next();
			if (file.getName().contains("\\") && !File.separator.equals("\\")) {
				file.setName(file.getName().replace("\\", File.separator));
			} else if (file.getName().contains("/") && !File.separator.equals("/")) {
				file.setName(file.getName().replace("/", File.separator));
			}
		}
	}

	public PropertyDataFile getMetaDataFile() {
		Iterator<PropertyDataFile> itr = getDataFiles().iterator();
		while (itr.hasNext()) {
			PropertyDataFile file = itr.next();
			if (file.getType().equals(MSGlycanAnnotationReportFileInfo.MS_GLYCAN_ANNOTATION_REPORT_METADATA_TYPE)) {
				return file;
			}
		}
		return null;
	}

	public static MSGlycanAnnotationReportMetaData unmarshallSettingsFile(String sFileName) {
		MSGlycanAnnotationReportMetaData metaData = null;
		try {
			metaData = (MSGlycanAnnotationReportMetaData) XMLUtils.unmarshalObjectXML(sFileName,
					MSGlycanAnnotationReportMetaData.class);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return metaData;
	}

	public static void marshallSettingsFile(String sFileName, MSGlycanAnnotationReportMetaData metaData) {
		try {
			String xmlString = XMLUtils.marshalObjectXML(metaData);
			// write the serialized data to the folder
			FileWriter fileWriter = new FileWriter(sFileName);
			fileWriter.write(xmlString);
			fileWriter.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void delete(Entry entry) throws IOException {
		try {
			String reportFolder = getFullyQualifiedReportsFolder(entry);
			Iterator<PropertyDataFile> itr = getDataFiles().iterator();
			while (itr.hasNext()) {
				PropertyDataFile pdf = itr.next();
				String sFile = reportFolder + File.separator + pdf.getName();
				File f = new File(sFile);
				DeleteUtils.delete(f);
			}
			String entryFolder = getFullyQualifiedReportsFolderName(entry);
			DeleteUtils.delete(new File(entryFolder));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private String getFullyQualifiedReportsFolder(Entry entry) {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		String projectName = DataModelSearch.findParentByType(entry, ProjectProperty.TYPE).getDisplayName();
		String folder = workspaceLocation + projectName + File.separator + ReportsProperty.getFolder() + File.separator
				+ getArchiveFolder();
		return folder;
	}

	public String getFullyQualifiedReportsFolderName(Entry entry) {
		String folder = getFullyQualifiedReportsFolder(entry);
		String file = folder + File.separator + getMsGlycanAnnotReportMetaData().getReportId();
		return file;
	}

	public String getFullyQualifiedMetaDataFileName(Entry entry) {
		String folder = getFullyQualifiedReportsFolder(entry);
		String file = folder + File.separator + getMsGlycanAnnotReportMetaData().getReportId() + File.separator
				+ getMetaDataFileName();
		return file;
	}

	public String getFullyQualifiedXMLFileName(Entry entry) {
		String folder = getFullyQualifiedReportsFolder(entry);
		String file = folder + File.separator + getMsGlycanAnnotReportMetaData().getReportId() + File.separator
				+ getArchiveFile();
		return file;
	}

	public String getArchiveFile() {
		return getMsGlycanAnnotReportMetaData().getReportId() + MSGlycanAnnotationReportProperty.ARCHIVE_EXTENSION;
	}

	public String getArchiveExtension() {
		return MSGlycanAnnotationReportProperty.ARCHIVE_EXTENSION;
	}

	public String getArchiveFolder() {
		return MSGlycanAnnotationReportProperty.ARCHIVE_FOLDER;
	}

	public String getMetaDataFileName() {
		return getMsGlycanAnnotReportMetaData().getReportId() + "." + MSGlycanAnnotationReportProperty.META_DATA_FILE;
	}

	/**
	 * Generate random id
	 * 
	 * @return
	 */
	public static String getRandomId() {
		Random random = new Random();
		return ((Integer) random.nextInt(10000)).toString();
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Property getParentProperty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean directCopyEnabled() {
		return false;
	}
}
