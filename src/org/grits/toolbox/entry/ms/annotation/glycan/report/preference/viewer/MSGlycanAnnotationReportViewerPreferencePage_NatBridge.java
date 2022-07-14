package org.grits.toolbox.entry.ms.annotation.glycan.report.preference.viewer;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.preference.MSGlycanAnnotationReportViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.preference.MSGlycanAnnotationReportViewerPreferenceLoader;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.MSGlycanAnnotationReportTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.annotation.preference.viewer.MSAnnotationViewerPreferencePage_NatBridge;

public class MSGlycanAnnotationReportViewerPreferencePage_NatBridge extends
		MSAnnotationViewerPreferencePage_NatBridge {

	public MSGlycanAnnotationReportViewerPreferencePage_NatBridge(Composite parent,
			int iMSLevel, FillTypes fillTypes, boolean bHideUnannotated) {
		super(parent, iMSLevel, fillTypes, bHideUnannotated);
	}

	@Override
	protected MSAnnotationTableDataObject getNewTableDataObject() {
		return new MSGlycanAnnotationReportTableDataObject(this.iMSLevel, this.fillType);
	}
		
	@Override
	protected TableViewerPreference getNewTableViewerPreference() {
		return new MSGlycanAnnotationReportViewerPreference();
	}
	
	@Override
	protected void initializeTableData(boolean _bDefault) throws Exception {
		super.initializeTableData(_bDefault);
		MSAnnotationViewerPreference preferences = (MSAnnotationViewerPreference ) getNatTable().getGRITSTableDataObject().getTablePreferences();
		if( ! _bDefault ) {
			setHideUnannotatedPeaks( preferences.isHideUnannotatedPeaks() );
		}
	}
	
	@Override	
	protected TableViewerColumnSettings getDefaultSettings() {
		TableViewerColumnSettings newSettings = getNewTableViewerSettings();
		MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportColumnSettingsGlycanAnnotation(newSettings);
		return newSettings;
	}
		
	@Override
	protected MassSpecViewerPreference getCurrentTableViewerPreference( int _iMSLevel, FillTypes _fillType ) {
		return MSGlycanAnnotationReportViewerPreferenceLoader.getTableViewerPreference(_iMSLevel, _fillType);
	}
	
	@Override
	protected void initializePreferences() throws Exception {
		super.initializePreferences();
	}
	
	@Override
	protected void setDefaultPreferences() {
		super.setDefaultPreferences();
		MSGlycanAnnotationReportViewerPreference preferences = (MSGlycanAnnotationReportViewerPreference) getNatTable().getGRITSTableDataObject().getTablePreferences();
		MSGlycanAnnotationReportTableDataProcessor.setDefaultColumnViewSettings(preferences.getPreferenceSettings());
	}
	
	
}
