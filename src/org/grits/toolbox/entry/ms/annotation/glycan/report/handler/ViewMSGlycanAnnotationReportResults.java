package org.grits.toolbox.entry.ms.annotation.glycan.report.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.ICancelableEditor;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;


/**
 * Create a new MS dialog
 * @author dbrentw
 *
 */
public class ViewMSGlycanAnnotationReportResults {

	public final static String VIEW_ID = "plugin.report.ms.merge.views.MSAnnotationMergeResultsView";
	
	//log4J Logger
	private static final Logger logger = Logger.getLogger(ViewMSGlycanAnnotationReportResults.class);
	
	@Inject static IGritsDataModelService gritsDataModelService = null;
    @Inject static IGritsUIService gritsUIService = null;
    @Inject EPartService partService;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
				@Named (IServiceConstants.ACTIVE_SHELL) Shell shell) {
		Entry selectedEntry = null;
		if(object instanceof Entry)
		{
			selectedEntry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				selectedEntry = (Entry) ((StructuredSelection) object).getFirstElement();
			}
		}
		// try getting the last selection from the data model
		if(selectedEntry == null
				&& gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
		{
			selectedEntry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
		}
		
		showPlugInView(shell, selectedEntry);
	}
		
	private void showPlugInView(Shell shell, Entry entry) {
		
		if(entry != null)
		{
			MPart part = null;
			try {
				part = gritsUIService.openEntryInPart(entry);
				if (part != null && part.getObject() != null && part.getObject() instanceof ICancelableEditor) {
					if ( ((ICancelableEditor) part.getObject()).isCanceled()) {
						partService.hidePart(part, true);
					}
				}
			}
			catch (Exception e) {
				Exception pie = new Exception("There was an error converting the XML to a table.", e);
				logger.error(pie.getMessage(),pie);
				ErrorUtils.createErrorMessageBox(shell, "Unable to open the results viewer", pie);
				if (part != null)
					partService.hidePart(part, true);
			}
		}
	}
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object) {
		Entry entry = null;
		if(object instanceof Entry)
		{
			entry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				entry = (Entry) ((StructuredSelection) object).getFirstElement();
			}
		}
		if (entry == null && gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
			// try getting the last selection from the data model
		{
			entry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
		}
		
        if (entry != null)
        {
        	if ( entry.getProperty().getType().equals( MSGlycanAnnotationReportProperty.TYPE ) )
            	return true;
        }
		return false;
	}
	
}
