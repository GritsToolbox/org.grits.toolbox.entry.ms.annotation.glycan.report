package org.grits.toolbox.entry.ms.annotation.glycan.report.views;

import org.apache.log4j.Logger;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;

import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;

public class MSGlycanAnnotationReportMultiPageViewerWorker extends GRITSWorker {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportMultiPageViewerWorker.class);
	protected int iMajorCount = 0;
	protected int iMajorMax = 0;
	protected MSGlycanAnnotationReportProperty prop = null;
	private MSGlycanAnnotationReportMultiPageViewer parentEditor = null;

	public MSGlycanAnnotationReportMultiPageViewerWorker( MSGlycanAnnotationReportMultiPageViewer parentEditor, 
			int _iMajorCount, MSGlycanAnnotationReportProperty prop ) {
		this.setParentEditor(parentEditor);
		this.iMajorMax = _iMajorCount;
		this.prop = prop;
	}

	@Override
	public int doWork() {
		iMajorCount = 0;
		int iSuccess = addResultsPage(prop, iMajorCount );
		iMajorCount++;
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 
		
		iSuccess = addPropertyPage(iMajorCount++);
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 
		updateListeners("Finished MS Glycan Report work!", iMajorCount);
		logger.debug("Finished MS Glycan Report work");
		return iSuccess;
	}

	public MSGlycanAnnotationReportMultiPageViewer getParentEditor() {
		return parentEditor;
	}

	public void setParentEditor(MSGlycanAnnotationReportMultiPageViewer parentEditor) {
		this.parentEditor = parentEditor;
	}

	public int addResultsPage(MSGlycanAnnotationReportProperty prop, int iProcessCount ) {
		try {
			updateListeners("Creating Results tab (loading)", iProcessCount);
			int iSuccess = getParentEditor().addResultsPage_Step1(prop);
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			}
			updateListeners("CreatingResults tab (populating)", iProcessCount + 1);
			iSuccess = getParentEditor().addResultsPage_Step2();
			updateListeners("Creating Results tab (done)", iProcessCount + 2);
			return iSuccess;				 
		} catch( Exception e ) {
			logger.error("Unable to open MS property view", e);
		}
		return GRITSProcessStatus.ERROR;
	}
	
	public int addPropertyPage(int iProcessCount ) {
		try {
			updateListeners("Creating property tab (loading)", iProcessCount);
			int iSuccess = getParentEditor().addPropertyPage();	
			updateListeners("Creating property tab (done)", iProcessCount + 1);
			return iSuccess;
		} catch( Exception ex ) {
			logger.error("Unable to open MS property view", ex);
			return GRITSProcessStatus.ERROR;
		}
	}		

	
}
