package org.grits.toolbox.entry.ms.annotation.glycan.report.views;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.tablehelpers.MSGlycanAnnotationReportTable;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationScansView;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTableDataChangedMessage;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.IMSPeaksViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;

public class MSGlycanAnnotationReportResultsView extends MSGlycanAnnotationScansView implements IMSPeaksViewer {

	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportResultsView.class);
	public static final String VIEW_ID = "org.grits.toolbox.entry.ms.annotation.glycan.report.views.ms-glycan-annotation-report-view"; //$NON-NLS-1$
	
	@Inject
	public MSGlycanAnnotationReportResultsView(Entry entry, Property msEntityProperty) {
		super(entry, msEntityProperty, 1);
	}

	@Optional
	@Inject
	public void updateTable( @UIEventTopic
			(MSAnnotationMultiPageViewer.EVENT_PARENT_ENTRY_VALUE_MODIFIED) MSAnnotationTableDataChangedMessage message) {
		if( viewBase != null && viewBase.getNatTable() != null ) {
			((MSGlycanAnnotationReportTable) viewBase.getNatTable()).updateTable(message);
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PaintListener pl = new PaintListener() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
			 */
			@Override
			public void paintControl(PaintEvent e) {
				if( viewBase != null && viewBase.getNatTable() != null && viewBase.getNatTable().getResetColumnPreferences() ) {
					viewBase.getNatTable().showColumnDialog();
				}
				compositeTop.removePaintListener(this);
			}
		};
		compositeTop.addPaintListener(pl);
	}

	@Override
	protected void initResultsView( Composite parent ) throws Exception {
		compositeTop = new Composite(parent, SWT.BORDER);
		compositeTop.setLayout(new GridLayout(1,false));

		try {
			resultsComposite = getNewResultsComposite(compositeTop, SWT.NONE);
			( (MSGlycanAnnotationReportResultsComposite) resultsComposite).createPartControl(this.compositeTop, this, this.entityProperty, this.dataProcessor, FillTypes.Scans);
			resultsComposite.setLayout(new FillLayout());
			this.viewBase = resultsComposite.getBaseView();

		} catch( Exception e ) {
			viewBase = null;
			resultsComposite = null;
			logger.error("Error in initResultsView", e);
			throw new Exception(e.getMessage());
		}		
	}
	
	@Override
	protected MSGlycanAnnotationReportResultsComposite getNewResultsComposite(
			Composite composite, int style) {
		return new MSGlycanAnnotationReportResultsComposite(composite, style);
	}

	@Override
	protected TableDataProcessor getNewTableDataProcessor(Entry entry, Property entityProperty) {
		MSGlycanAnnotationReportTableDataProcessor proc = new MSGlycanAnnotationReportTableDataProcessor(
				entry, entityProperty, FillTypes.Scans, getMinMSLevel());
		return proc;
	}

	@Override
	protected TableDataProcessor getNewTableDataProcessor(Property entityProperty) {
		return null; // always going to create a new processor
	}
	
	@Persist
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		//		super.doSave(monitor);
		this.viewBase.doSave(monitor);
		setDirty(false);
	}
	
	/**
	 * This method is called whenever a preference change occurs
	 * We need to act upon cartoon preference changes for this view 
	 * 
	 * @param preferenceName
	 */
	@Optional @Inject
	public void updatePreferences(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED)
	 					String preferenceName)
	{
	 	if (MSGlycanAnnotationCartoonPreferences.getPreferenceID().equals(preferenceName)) {
	 		try {
				MassSpecTableBase viewBase = (MassSpecTableBase) ( (IMSPeaksViewer) this ).getViewBase();
				((MSGlycanAnnotationReportTable) viewBase.getNatTable()).refreshTableImages();
			} catch( Exception e ) {
				logger.error("Could not refresh table images", e);
			}
	 	}
	}
}
