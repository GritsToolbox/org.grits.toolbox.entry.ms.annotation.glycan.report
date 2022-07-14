package org.grits.toolbox.entry.ms.annotation.glycan.report.tablehelpers.gui;

import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.tablecore.IGritsTable;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.gui.MSAnnotationTableColumnChooser;

public class MSGlycanAnnotationReportTableColumnChooser extends MSAnnotationTableColumnChooser {

	public MSGlycanAnnotationReportTableColumnChooser(Shell shell,
			boolean sortAvailableColumns,
			boolean asGlobalPreference, 
			IGritsTable gritsTable ) {
		super(shell, sortAvailableColumns, asGlobalPreference, gritsTable );
	}

	@Override
	protected TableViewerColumnSettings getDefaultSettings() {
		MSGlycanAnnotationReportTableDataProcessor proc = (MSGlycanAnnotationReportTableDataProcessor) getGRITSTable().getTableDataProcessor();
		TableViewerPreference newPref = proc.initializePreferences();
		TableViewerColumnSettings newSettings = newPref.getPreferenceSettings();
		MSGlycanAnnotationReportTableDataProcessor.setDefaultColumnViewSettings(newSettings);
		return newSettings;
	}
	
}
