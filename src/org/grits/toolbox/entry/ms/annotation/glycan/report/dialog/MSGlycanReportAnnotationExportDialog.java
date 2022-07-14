package org.grits.toolbox.entry.ms.annotation.glycan.report.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.entry.ms.annotation.adaptor.MSAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExportDialog;

public class MSGlycanReportAnnotationExportDialog extends MSAnnotationExportDialog{

	public MSGlycanReportAnnotationExportDialog(Shell parentShell,
			MSAnnotationExportFileAdapter msAnnotationExportFileAdapter) {
		super(parentShell, msAnnotationExportFileAdapter);
	}
	
	@Override
	protected void createFilters(Composite parent) {
		// no filtering options for the report export
	}
	
	
	@Override
	protected void createList(Composite parent2) {
		 //we don't need export to byonic for merge report - so overwriting it here
		downloadlist = new List(parent2, SWT.SINGLE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 4;
		gridData.verticalSpan = 1;
		downloadlist.setLayoutData(gridData);
		//add data to list
		downloadlist.add(downloadOptions[0]);
		downloadlist.add(downloadOptions[1]);
		//add listener
		downloadlist.addSelectionListener(downloadlistListener);
	}

}
