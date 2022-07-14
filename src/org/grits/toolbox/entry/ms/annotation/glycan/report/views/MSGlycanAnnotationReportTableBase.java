package org.grits.toolbox.entry.ms.annotation.glycan.report.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.tablehelpers.MSGlycanAnnotationReportTable;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationTableBase;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationTableBase;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;

public class MSGlycanAnnotationReportTableBase extends MSGlycanAnnotationTableBase {	

	public MSGlycanAnnotationReportTableBase( Composite parent, EntryEditorPart parentEditor, 
			Property entityProperty, TableDataProcessor dataProcessor, FillTypes fillType ) throws Exception {
		super(parent, parentEditor, entityProperty, dataProcessor, fillType);
	}
		
	@Override
	public void initializeTable() throws Exception {
		this.natTable = (MSGlycanAnnotationReportTable) getNewSimianTable(this, dataProcessor);	
		((MSGlycanAnnotationReportTableDataProcessor) this.dataProcessor).initializeTableDataObject(entityProperty);
		this.natTable.loadData();
		this.natTable.createMainTable();
	}
	
	@Override
	public GRITSTable getNewSimianTable( MassSpecTableBase _viewBase, TableDataProcessor _extractor ) throws Exception {
		return new MSGlycanAnnotationReportTable( (MSAnnotationTableBase) _viewBase, _extractor);
	}
	
	public void doSave(IProgressMonitor monitor) {
		String sSourceFile = ( (MSGlycanAnnotationReportTableDataProcessor) getTableDataProcessor()).getScanArchiveFile();
		getNatTable().writeDataToXML(sSourceFile); // an in-place overwrite
		setDirty(false);
	}	
}
