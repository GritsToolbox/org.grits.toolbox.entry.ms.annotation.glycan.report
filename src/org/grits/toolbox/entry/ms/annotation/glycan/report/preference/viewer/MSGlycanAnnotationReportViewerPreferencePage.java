package org.grits.toolbox.entry.ms.annotation.glycan.report.preference.viewer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.dialog.GRITSTableColumnChooser;
import org.grits.toolbox.entry.ms.annotation.glycan.report.views.MSGlycanAnnotationReportMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.preference.viewer.MSAnnotationViewerPreferencePage;
import org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge;


public class MSGlycanAnnotationReportViewerPreferencePage extends MSAnnotationViewerPreferencePage {
	public final static String[] TABLE_TYPES = {FillTypes.Scans.getLabel()};
	public final static String[] MS_LEVELS = {OVERVIEW};	

	public MSGlycanAnnotationReportViewerPreferencePage() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Override
	protected void updateTableTypeCombo( int _iMSLevel ) {
		String[] tableTypes = MSGlycanAnnotationReportMultiPageViewer.getPreferencePageLabels();
		String defaultTable = tableTypes[0];
		comboTablelevel.setItems(tableTypes);
		comboTablelevel.setText(defaultTable);		
	}
	
	@Override
	protected FillTypes getTableFillType() {
		FillTypes[] fillTypes = MSGlycanAnnotationReportMultiPageViewer.getPreferencePageFillTypes();
		FillTypes fillType = fillTypes[getTableNumber()];
		return fillType;
	}
	
	@Override
	protected int getMaxNumTables() {
		int iMaxTableTypes = MSGlycanAnnotationReportMultiPageViewer.getPreferencePageMaxNumPages();
		return iMaxTableTypes;
	}

	@Override
	protected String[] getMSLevels() {
		return MS_LEVELS;
	}

	@Override
	protected MassSpecViewerPreferencePage_NatBridge getPreferenceUItoNatBridge(boolean _bDefault) {
		MSGlycanAnnotationReportViewerPreferencePage_NatBridge natBridge = new MSGlycanAnnotationReportViewerPreferencePage_NatBridge( 
				new Composite(getShell(), NONE), 
				getCurMSLevel(), getTableFillType(), getHideUnannotatedPeaks() );
		natBridge.initializeComponents(_bDefault);
		return natBridge;

	}

	@Override
	protected int getCurMSLevel() {
		return 1;
	}

	@Override
	protected int getTableNumber() {
		return 0;
	}

	@Override
	protected Point doComputeSize() {
		// TODO Auto-generated method stub
		return super.doComputeSize();
	}

	@Override
	public Point computeSize() {
		// TODO Auto-generated method stub
		return super.computeSize();
	}

	@Override
	protected void save() {
		if( natBridge[0][0] != null ) {
			natBridge[0][0].updatePreferences();
		}	
	}
	

	@Override
	protected void updateColumnChooserElements(Composite container, boolean _bDefault) {
		int iCurMS = getCurMSLevel() - 1;
		if ( natBridge[getTableNumber()][iCurMS] == null || _bDefault ) {
			natBridge[getTableNumber()][iCurMS] = getPreferenceUItoNatBridge(_bDefault);
		}
		if( chooser == null) {
			chooser = new GRITSTableColumnChooser(
					parent.getShell(),
//					natBridge[getTableNumber()][iCurMS].getNatTable().getSelectionLayer(), 
//					natBridge[getTableNumber()][iCurMS].getNatTable().getColumnHideShowLayer(),
//					natBridge[getTableNumber()][iCurMS].getNatTable().getColumnHeaderLayer(),
//					natBridge[getTableNumber()][iCurMS].getNatTable().getColumnHeaderDataLayer(), 
//					natBridge[getTableNumber()][iCurMS].getNatTable().getColumnGroupHeaderLayer(),
//					natBridge[getTableNumber()][iCurMS].getNatTable().getColumnGroupModel(), 
					false, true, natBridge[getTableNumber()][iCurMS].getNatTable());			
			chooser.getColumnChooserDialog().populateDialogArea(container);
			chooser.addListenersOnColumnChooserDialog();			
		} else {
			chooser.getHiddenColumnEntries().clear();
			chooser.getColumnChooserDialog().removeAllLeaves();
			chooser.reInit(natBridge[getTableNumber()][iCurMS].getNatTable());
		}
		chooser.populateDialog();
	}

	@Override
	protected void initComponents( Composite container ) {
		initNatTable(container);
		GridData gridData1 = GridDataFactory.fillDefaults().grab(true, false).create();
		gridData1.horizontalSpan = 4;
		Label lbl = new Label(container, SWT.None);
		lbl.setText("");
		lbl.setLayoutData(gridData1);
	}
}
