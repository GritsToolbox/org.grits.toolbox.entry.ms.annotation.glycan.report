package org.grits.toolbox.entry.ms.annotation.glycan.report.adapter;

import java.io.File;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;

import org.grits.toolbox.entry.ms.annotation.glycan.adaptor.MSGlycanAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.io.ms.annotation.glycan.report.process.export.MSGlycanAnnotationReportExportProcess;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportProcess;

public class MSGlycanAnnotationReportExportFileAdapter extends MSGlycanAnnotationExportFileAdapter {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportExportFileAdapter.class);

	@Override
	protected String getId() {
		MSGlycanAnnotationReportProperty property = (MSGlycanAnnotationReportProperty)this.msAnnotationEntry.getProperty();
		String id = property.getMsGlycanAnnotReportMetaData().getReportId();
		return id;		
	}
	
	@Override
	public String getArchiveFolder() {
		String folder = MSGlycanAnnotationReportProperty.ARCHIVE_FOLDER;
		return folder;
	}

	@Override
	public String getArchiveExtension() {
		String extension = MSGlycanAnnotationReportProperty.ARCHIVE_EXTENSION;
		return extension;
	}
	
	@Override
	protected String getFullyQualifiedArchivePath() {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		String projectName = DataModelSearch.findParentByType(msAnnotationEntry, ProjectProperty.TYPE).getDisplayName();
		String id = getId();
		String from = workspaceLocation + projectName + File.separator +
				ReportsProperty.getFolder() + File.separator +
				getArchiveFolder() + File.separator + 
				id + File.separator + 
				id + fileExtension;
		return from;
	}
	
	@Override
	protected MSAnnotationExportProcess getNewExportProcess() {
		return new MSGlycanAnnotationReportExportProcess();
	}

}
