package org.grits.toolbox.entry.ms.annotation.glycan.report.views;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.CancelableMultiPageEditor;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.core.editor.IEntryEditorPart;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.preference.MSGlycanAnnotationReportViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.tablehelpers.MSGlycanAnnotationReportTable;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;

public class MSGlycanAnnotationReportMultiPageViewer extends CancelableMultiPageEditor {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportMultiPageViewer.class);
	protected Entry entry = null;
	protected MSGlycanAnnotationReportPropertyView propertyView = null;
	protected MSGlycanAnnotationReportResultsView resultsView = null;
	protected int propertyViewTabIndex = -1;
	protected int resultsViewTabIndex = -1;

	public static String VIEW_ID = "plugin.ms.annotation.glycan.views.MSGlycanAnnotationReportMultiPageViewer";

	@Inject protected IGritsPreferenceStore gritsPreferenceStore;
	@Inject MDirtyable dirtyable;
	@Inject protected static IGritsDataModelService gritsModelService;
	@Inject ESelectionService selectionService;
	@Inject IEventBroker eventBroker;

	@Override
	public String toString() {
		return "MSGlycanAnnotationReportMultiPageViewer (" + entry + ")";
	}

	@Inject
	public MSGlycanAnnotationReportMultiPageViewer( Entry entry ) {
		super();
		this.entry = entry;
	}

	@Inject
	public MSGlycanAnnotationReportMultiPageViewer (MPart part) {
		super();
		this.entry = (Entry) part.getTransientData().get(IGritsUIService.TRANSIENT_DATA_KEY_PART_ENTRY);
	}


	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}


	public void setPropertyViewTabIndex(int propertyViewTabIndex) {
		this.propertyViewTabIndex = propertyViewTabIndex;
	}

	public int getPropertyViewTabIndex() {
		return propertyViewTabIndex;
	}


	public void setResultsViewTabIndex(int resultsViewTabIndex) {
		this.resultsViewTabIndex = resultsViewTabIndex;
	}

	public int getResultsViewTabIndex() {
		return resultsViewTabIndex;
	}

	protected Entry getFirstPageEntry() {
		try {
			Entry parentEntry = null;
			if (gritsModelService.getLastSelection() != null
					&& gritsModelService.getLastSelection().getFirstElement() instanceof Entry)
				parentEntry = (Entry) gritsModelService.getLastSelection().getFirstElement();
			return parentEntry;
		}catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	protected Object getDesiredActivePage() {
		return this.resultsView;
	}

	public void setActivePage() {
		try {
			for (int i = 0; i < getPageCount(); i++ ) {
				Object page = getPageItem(i);
				if( page == null )
					continue;
				if( page.equals(getDesiredActivePage()) ) {
					setActivePage(i);
					return;
				}
			}
		} catch( Exception ex ) {
			logger.error("Unable to getFirstTabIndex", ex);
		}	
	}

	protected int addPages( int _iMajorCount, MSGlycanAnnotationReportProperty prop ) {
		this.dtpdThreadedDialog = new GRITSProgressDialog(new Shell(), 1, true);
		this.dtpdThreadedDialog.open();
		this.dtpdThreadedDialog.getMajorProgressBarListener().setMaxValue(_iMajorCount);
		MSGlycanAnnotationReportMultiPageViewerWorker msmpvw = new MSGlycanAnnotationReportMultiPageViewerWorker(this, _iMajorCount, prop);
		this.dtpdThreadedDialog.setGritsWorker(msmpvw);
		int iSuccess = this.dtpdThreadedDialog.startWorker();
		return iSuccess;
	}

	protected int getNumMajorSteps(MSGlycanAnnotationReportProperty prop) {
		int iCount = 3; // property view and result view
		return iCount;
	}


	@Override
	public void createPages() {
		if (entry == null) {
			entry = getFirstPageEntry();
		} 
		Property prop = entry.getProperty();	
		int iNumSteps = getNumMajorSteps((MSGlycanAnnotationReportProperty) prop);

		iStatus = addPages(iNumSteps, (MSGlycanAnnotationReportProperty) prop);
		if (iStatus == GRITSProcessStatus.ERROR) {
			// need to close the editor, it failed to open
			throw new RuntimeException("Failed to open the entry");
		}
		setPartName(entry.getDisplayName());	
		setActivePage();
	}

	protected boolean initMSPropertyView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			propertyView = ContextInjectionFactory.make(MSGlycanAnnotationReportPropertyView.class, getPart().getContext());
			propertyView.setMergeSettings( ((MSGlycanAnnotationReportTableDataProcessor) resultsView.getTableDataProcessor()).getMergeReportSettings() );
			//new MSGlycanAnnotationReportPropertyView();
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open ms property view", ex);
		}		
		return false;
	}

	protected int initResultsView( MSGlycanAnnotationReportProperty entityProperty ) {
		try {
			resultsView = getNewResultsView( this.entry, entityProperty );
			resultsView.setTableDataProcessor( getThreadedDialog() );
			return GRITSProcessStatus.OK;
		} catch( Exception ex ) {
			logger.error("Unable to open scans view", ex);
		}		
		return GRITSProcessStatus.ERROR;
	}

	protected MassSpecProperty getMSProperty( Entry entry ) {
		if ( entry == null || entry.getProperty() == null)
			return null;
		else if ( entry.getProperty() instanceof MassSpecProperty ) {
			return (MassSpecProperty) entry.getProperty();
		}
		return getMSProperty(entry.getParent());
	}

	protected MSGlycanAnnotationReportResultsView getNewResultsView( Entry entry, MSGlycanAnnotationReportProperty property) {
		getPart().getContext().set(Property.class, property);
		getPart().getContext().set(Entry.class, entry);
		return ContextInjectionFactory.make(MSGlycanAnnotationReportResultsView.class, getPart().getContext());
	}

	public MSGlycanAnnotationReportResultsView getResultsView() {
		return resultsView;
	}

	public MSGlycanAnnotationReportPropertyView getPropertyView() {
		return propertyView;
	}


	@Persist
	public void doSave(IProgressMonitor monitor) {
		GRITSProgressDialog progressDialog = new GRITSProgressDialog(Display.getCurrent().getActiveShell(), 0, false, false);
		progressDialog.open();
		progressDialog.getMajorProgressBarListener().setMaxValue(2);
		progressDialog.setGritsWorker(new GRITSWorker() {

			@Override
			public int doWork() {
				if( propertyView != null && propertyView.isDirty()) {
					updateListeners("Saving properties", 1);
					propertyView.doSave(monitor);
				}
				if( resultsView != null && resultsView.isDirty()) {
					updateListeners("Saving resport", 2);
					resultsView.doSave(monitor);
				}
				setDirty(false);
				updateListeners("Done saving", 2);
				return GRITSProcessStatus.OK;
			}
		});
		progressDialog.startWorker();
	}

	/** 
	 * this method is called whenever a page (tab) is updated 
	 * However we have to check to make sure the modified page is one of the pages of this
	 * multi-page editor
	 *  
	 * @param the part that gets dirty
	 */
	@Optional @Inject
	public void tabContentModified (@UIEventTopic
			(EntryEditorPart.EVENT_TOPIC_CONTENT_MODIFIED) IEntryEditorPart part) {
		if (part.equals(propertyView) || part.equals(resultsView))
			setDirty (part.isDirty());
	}

	public void setDirty(boolean d) {
		this.dirtyable.setDirty(d);
	}

	public boolean isDirty() {
		return this.dirtyable.isDirty();
	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	public int addPropertyPage() {
		try {
			boolean success = initMSPropertyView();	
			int iPageCount = getPageCount();
			if( success ) {
				try {
					int inx = addPage( propertyView, entry);
					setPageText(inx, "Report Properties");	
					setActivePage(inx);
					setStatus(GRITSProcessStatus.OK);
				} catch( Exception ex ) {
					logger.error("Unable to open Report property view", ex);
					setStatus(GRITSProcessStatus.ERROR);
				}
			}
			if( isCanceled() ) {
				setStatus(GRITSProcessStatus.CANCEL);
			}
			success = (getStatus() == GRITSProcessStatus.OK );
			if( ! success ) {
				if( getPageCount() != iPageCount ) {
					removePage(getPageCount());
				}
				propertyView = null;
			} else {
				setPropertyViewTabIndex(getPageCount() - 1);
			}
		} catch( Exception ex ) {
			logger.error("Unable to open Report property view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the Report Properties tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();
	}


	public int addResultsPage_Step1( MSGlycanAnnotationReportProperty prop ) {
		try {
			int iSuccess = initResultsView(prop);	
			dtpdThreadedDialog.setMinorStatus(iSuccess);
			if( iSuccess == GRITSProcessStatus.CANCEL ) {
				setStatus(GRITSProcessStatus.CANCEL);
				return GRITSProcessStatus.CANCEL;
			}
		} catch( Exception ex ) {
			logger.error("Unable to open MS Peaks view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the Report.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();	
	}

	public int addResultsPage_Step2() {
		try {
			boolean success = true;
			int iPageCount = getPageCount();
			try {
				int inx = addPage( resultsView, entry );		
				setPageText(inx, "Results View");
				setActivePage(inx);
				int iSuccess = resultsView.getStatus();
				setStatus(iSuccess);
				dtpdThreadedDialog.setMinorStatus(iSuccess);
			} catch( Exception ex ) {
				logger.error("Error adding Report tab.", ex);
				setStatus(GRITSProcessStatus.ERROR);
			}			
			success = (getStatus() != GRITSProcessStatus.ERROR);

			if( ! success ) {
				if( getPageCount() != iPageCount && getPageCount() >= 1) {
					removePage(getPageCount() -1);
				}
				resultsView = null;
			} else {
				setResultsViewTabIndex(getPageCount() - 1);
			}
		} catch( Exception ex ) {
			logger.error("Unable to open Report view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the Report.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();	
	}

	protected void updateColumnVisibility( MSGlycanAnnotationReportTable table, 
			MSGlycanAnnotationReportViewerPreference curPref, MSGlycanAnnotationReportViewerPreference updatePref ) {
		if( curPref.getClass().equals(updatePref.getClass()) && 
				curPref.getMSLevel() == updatePref.getMSLevel() && 
				curPref.getFillType() == updatePref.getFillType() ) {
			// don't update if not changed!
			if( ! updatePref.getColumnSettings().equals(curPref.getColumnSettings()) ) {
				table.getGRITSTableDataObject().setTablePreferences( updatePref );
				table.updateViewFromPreferenceSettings();						
			}
		}
	}		

	protected void updateColumnVisibility( MSGlycanAnnotationReportViewerPreference updatePref ) {
		try {
			if( getResultsView() != null ) {
				MSGlycanAnnotationReportResultsView resultsView = getResultsView();
				if( resultsView.getResultsView() == null ) {
					return;
				}
				MassSpecTableBase viewBase = resultsView.getResultsView().getBaseView();
				if( viewBase == null || viewBase.getNatTable() == null ) {
					return;
				}
				MSGlycanAnnotationReportTable table = (MSGlycanAnnotationReportTable) viewBase.getNatTable();

				MSGlycanAnnotationReportTableDataProcessor proc = (MSGlycanAnnotationReportTableDataProcessor) resultsView.getTableDataProcessor();
				MSGlycanAnnotationReportViewerPreference curPref = (MSGlycanAnnotationReportViewerPreference) proc.getSimianTableDataObject().getTablePreferences();
				updateColumnVisibility(table, curPref, updatePref);
			}
		} catch( Exception ex ) {
			logger.error("Error updating Results view from editor: " + getTitle(), ex);
		}	
	}

	@Optional @Inject
	public void updatePreferences(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED)
	String preferenceName)
	{
		if(preferenceName != null && preferenceName.startsWith(MSGlycanAnnotationReportViewerPreference.class.getName())) {
			PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);

				MSGlycanAnnotationReportViewerPreference updatePref = (MSGlycanAnnotationReportViewerPreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, MSGlycanAnnotationReportViewerPreference.class);
				this.updateColumnVisibility(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility", e);
			}
		}
	}


	public static String[] getPreferencePageLabels() {
		return new String[]{"Results View"};
	}

	public static FillTypes[] getPreferencePageFillTypes() {
		return new FillTypes[]{FillTypes.Scans};
	}

	public static int getPreferencePageMaxNumPages() {
		return 1;
	}


	/*
	protected int initDetailsView( MassSpecEntityProperty entityProperty ) {
		try {
			annotDetails = getNewDetailsView( this.entry, entityProperty);
			return GRITSProcessStatus.OK;
		} catch( Exception ex ) {
			logger.error("Unable to open peaks view", ex);
		}		
		return GRITSProcessStatus.ERROR;
	}


	protected MSGlycanAnnotationDetails getNewDetailsView( Entry entry, MassSpecEntityProperty entityProperty) {
		//		return new MassSpecPeaksView(entry, entityProperty);
		MSGlycanAnnotationReportMultiPageViewer parent = MSGlycanAnnotationReportMultiPageViewer.getActiveViewerForEntry(getContext(), entry.getParent());
		if ( parent != null ) {
			CartoonOptions cOptions = getCartoonOptions();	
			getPart().getContext().set(MSGlycanAnnotationMultiPageViewer.class, parent);
			getPart().getContext().set(Entry.class, entry);
			getPart().getContext().set(Property.class, entityProperty);
			getPart().getContext().set(CartoonOptions.class, cOptions);
			getPart().getContext().set(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
			MSGlycanAnnotationDetails view = ContextInjectionFactory.make(MSGlycanAnnotationDetails.class, getPart().getContext());
					//new MSGlycanAnnotationDetails(parent, entry, (MSAnnotationEntityProperty) entityProperty, cOptions, getMinMSLevel());
			//		view.updateFeature(entry, (MSAnnotationEntityProperty) entityProperty, cOptions);
			return view;
		}
		return null;
	}

	 */
	public static MSGlycanAnnotationReportMultiPageViewer getActiveViewerForEntry(IEclipseContext context, Entry entry ) {
		EPartService partService = context.get(EPartService.class);
		for (MPart part: partService.getParts()) {
			if (part.getObject() instanceof MSGlycanAnnotationReportMultiPageViewer) {
				if (((MSGlycanAnnotationReportMultiPageViewer)part.getObject()).getEntry().equals(entry)) {
					return (MSGlycanAnnotationReportMultiPageViewer)part.getObject();
				}
			}
		}
		return null;
	}


	@Focus
	public void setFocus() {
		getContainer().forceFocus();
	}

	public static MSGlycanAnnotationReportMultiPageViewer getActiveViewer(IEclipseContext context) {
		MPart part = (MPart) context.get(IServiceConstants.ACTIVE_PART);
		if (part != null && part.getObject() instanceof MSGlycanAnnotationReportMultiPageViewer)
			return (MSGlycanAnnotationReportMultiPageViewer) part.getObject();
		return null;
	}

	public void selectMSGlycanAnnotationEntry( Entry msAnnotationEntry ) {
		// post will not work because of synchronization 
		// the selection needs to change before we try to open the part!
		eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, msAnnotationEntry);		
	}


}
