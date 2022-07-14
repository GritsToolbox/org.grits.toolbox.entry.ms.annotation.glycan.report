/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.grits.toolbox.entry.ms.annotation.glycan.report.command;

import org.grits.toolbox.display.control.table.command.GRITSTableDisplayColumnChooserCommand;
import org.grits.toolbox.display.control.table.dialog.GRITSTableColumnChooser;
import org.grits.toolbox.display.control.table.tablecore.IGritsTable;
import org.grits.toolbox.entry.ms.annotation.command.MSAnnotationViewColumnChooserCommandHandler;
import org.grits.toolbox.entry.ms.annotation.glycan.report.tablehelpers.gui.MSGlycanAnnotationReportTableColumnChooser;

public class MSGlycanAnnotationReportViewColumnChooserCommandHandler 
	extends MSAnnotationViewColumnChooserCommandHandler {
	
	public MSGlycanAnnotationReportViewColumnChooserCommandHandler(
			IGritsTable gritsTable ) {

		this(false, gritsTable);
	}

	public MSGlycanAnnotationReportViewColumnChooserCommandHandler(
			boolean sortAvalableColumns,
			IGritsTable gritsTable ) {
		super( sortAvalableColumns, gritsTable );
	}
		
	@Override
	public GRITSTableColumnChooser getNewGRITSTableColumnChooser(
			GRITSTableDisplayColumnChooserCommand command) {
		MSGlycanAnnotationReportTableColumnChooser columnChooser = new MSGlycanAnnotationReportTableColumnChooser(
				command.getNatTable().getShell(),
				sortAvailableColumns, false, gritsTable );
		return columnChooser;
	}
		
}
