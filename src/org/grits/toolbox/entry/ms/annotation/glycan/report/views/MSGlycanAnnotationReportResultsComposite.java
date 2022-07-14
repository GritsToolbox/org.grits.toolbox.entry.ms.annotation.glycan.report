package org.grits.toolbox.entry.ms.annotation.glycan.report.views;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;

import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationResultsComposite;

public class MSGlycanAnnotationReportResultsComposite extends MSGlycanAnnotationResultsComposite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MSGlycanAnnotationReportResultsComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	public void createPartControl(Composite parent, EntryEditorPart parentEditor, Property entityProprty, 
			TableDataProcessor dataProcessor, FillTypes fillType ) throws Exception {
		baseView = new MSGlycanAnnotationReportTableBase(parent, parentEditor, entityProprty, dataProcessor, fillType);	
		baseView.initializeTable();
		baseView.layout();
	}
	
}
