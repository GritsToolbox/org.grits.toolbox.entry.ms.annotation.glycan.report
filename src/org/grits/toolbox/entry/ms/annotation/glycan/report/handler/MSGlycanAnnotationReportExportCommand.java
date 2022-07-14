package org.grits.toolbox.entry.ms.annotation.glycan.report.handler;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExportDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.report.adapter.MSGlycanAnnotationReportExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.glycan.report.dialog.MSGlycanReportAnnotationExportDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.report.views.MSGlycanAnnotationReportMultiPageViewer;

/**
 * Export command. call SimianExportDialog.
 * 
 * @author dbrentw
 * 
 */
public class MSGlycanAnnotationReportExportCommand {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportExportCommand.class);

	private Entry entry = null;
	private MSGlycanAnnotationTableDataObject tableDataObject = null;
	private int m_lastVisibleColInx = -1;


	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell,
			EPartService partService) {

		if (initialize(part, partService)) {
			// need to show a dialog which contains three elements in a list:
			// SimGlycanCSV, XML, and load data into XML to export
			createSimianExportDialog(shell);
		} else {
			logger.warn("An MS Annotation Merge entry must be open and active in order to export.");
			// need to show dialog saying please choose a simGlycanEntry
			ErrorUtils.createWarningMessageBox(	shell, "Invalid Entry",	"An MS Annotation Merge entry must be open and active in order to export.");
		}
	}

	protected void createSimianExportDialog(Shell activeShell) {
		MSGlycanAnnotationReportExportFileAdapter adapter = new MSGlycanAnnotationReportExportFileAdapter();
		MSAnnotationExportDialog dialog = new MSGlycanReportAnnotationExportDialog(
				PropertyHandler.getModalDialog(activeShell), adapter);
		// set parent entry
		dialog.setMSAnnotationEntry(entry);
		dialog.setTableDataObject(tableDataObject);
		dialog.setLastVisibleColInx(m_lastVisibleColInx);
		if (dialog.open() == Window.OK) {
			// to do something..
		}
	}

	protected boolean initialize(MPart part, EPartService partService) {
		try {
			MSGlycanAnnotationReportMultiPageViewer viewer = null;
			if (part != null && part.getObject() instanceof MSGlycanAnnotationReportMultiPageViewer) {
				viewer = (MSGlycanAnnotationReportMultiPageViewer) part.getObject();
			}
			else { // try to find an open part of the required type
				for (MPart mPart: partService.getParts()) {
					if (mPart.getObject() instanceof MSGlycanAnnotationReportMultiPageViewer) {
						if (mPart.equals(mPart.getParent().getSelectedElement())) {   // this gives the currently visible part
							viewer = (MSGlycanAnnotationReportMultiPageViewer) mPart.getObject();
							break;
						}
					}
				}
			}
				
			if( viewer == null ) {
				return false;
			}
			this.entry = viewer.getEntry();		
			if (viewer.getResultsView() == null || viewer.getResultsView().getViewBase() == null || viewer.getResultsView().getViewBase().getNatTable() == null) 
				return false;
			MSGlycanAnnotationTableDataObject data = (MSGlycanAnnotationTableDataObject) viewer.getResultsView().getViewBase().getNatTable().getGRITSTableDataObject();
			this.tableDataObject = data;
			TableDataProcessor processor = viewer.getResultsView().getViewBase().getNatTable().getTableDataProcessor();
			this.m_lastVisibleColInx = processor.getLastVisibleCol();
			
			return true;
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
			return false;
		}		
	}
	
	@CanExecute
	public boolean isEnabled(@Named(IServiceConstants.ACTIVE_PART) MPart part, EPartService partService) {
		return initialize(part, partService);
	}
}
