package org.grits.toolbox.entry.ms.annotation.glycan.report.adapter;

import java.util.HashMap;

import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ProjectExplorerDialog;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.EntrySelectionAdapter;
import org.grits.toolbox.core.utilShare.ErrorUtils;

import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessorUtil;

public class MSGlycanAnnotationReportDialogAdapter extends EntrySelectionAdapter{
	
	public MSGlycanAnnotationReportDialogAdapter(String a_propertyType,
			String a_dialogTitle, String a_dialogMessage) {
		super(a_propertyType, a_dialogTitle, a_dialogMessage);
		// TODO Auto-generated constructor stub
	}

	private Grid entryDialogGrid = null;
	private HashMap<String, String> listEntries = null;
	private java.util.List<Entry> entries = null;
	
	@Override
	public void widgetSelected(SelectionEvent event) 
	{
		Shell newShell = new Shell(parent.getShell(),SWT.PRIMARY_MODAL | SWT.SHEET);
		ProjectExplorerDialog dlg = new ProjectExplorerDialog(newShell);
		// Set a simglycan entry as a filter
//		dlg.addFilter(MSGlycanAnnotationProperty.TYPE);
		dlg.addFilter(this.m_propertyType);
		// Change the title bar text
//		dlg.setTitle("MS Annotation Selection");
		dlg.setTitle(this.m_title);
		// Customizable message displayed in the dialog
//		dlg.setMessage("Choose an annotation to add");
		dlg.setMessage(this.m_message);
		// Calling open() will open and run the dialog.
		if (dlg.open() == Window.OK) {
			Entry entry = dlg.getEntry();
			if (entry != null) {
				String displayName = MSGlycanAnnotationReportTableDataProcessorUtil.getDisplayNameByEntryName( ((MSGlycanAnnotationProperty)entry.getProperty()).getMSAnnotationMetaData().getAnnotationId());
				if( displayName == null ) {
					throw new NullPointerException("Unable to determine display name for entry: " + entry.getDisplayName() );
				}
	//			String name = entry.getParent().getParent().getDisplayName() + "/" +entry.getParent().getDisplayName() + "/" + entry.getDisplayName();
				if(listEntries.isEmpty())
				{
					addToList(entry,displayName);
				}
				//if something is there, then check if this entry has the same parent!
				else if(!this.listEntries.containsKey(displayName))
				{
					//has to be in the same parent 
					if(sameParent(entry,ProjectProperty.TYPE))
					{
						addToList(entry,displayName);
					}
					else
					{
						//show a message. Please choose the entry in the same parent 
						ErrorUtils.createWarningMessageBox(
								new Shell(parent.getShell(),SWT.PRIMARY_MODAL | SWT.SHEET), 
								"Cannot Merge Across Projects", "Cannot add the selected annotation\nSelected annotation is not in the same project of other annotations.");
					}
				}
			}
		}
	}

	private boolean sameParent(Entry entry, String type) {
		//get a element from the list 
		//and get its parent
		//then compare!
		Entry curParent = DataModelSearch.findParentByType(this.entries.get(0), type);
		Entry myParent = DataModelSearch.findParentByType(entry, type);
		if(curParent.equals(myParent))
		{
			return true;
		}
		return false;
	}

	private void addToList(Entry entry, String name) {
		//update the hashMap entry history
		this.listEntries.put(name, name);
		//update list
		
		GridItem item = new GridItem(this.entryDialogGrid, SWT.NONE);
		item.setText(0, name);
		item.setText(1, name);
		this.entryDialogGrid.setSelection(this.entryDialogGrid.getItemCount()-1);
		this.entryDialogGrid.notifyListeners(SWT.Modify, null);
		//update the entries
		this.entries.add(entry);
	}
	
	public void setList(Grid entryDialogGrid) {
		this.entryDialogGrid = entryDialogGrid;
	}

	public void setListEntries(HashMap<String, String> listEntries) {
		this.listEntries = listEntries;
	}

	public void setEntries(java.util.List<Entry> simGLycanEntryList) {
		this.entries = simGLycanEntryList;
	}
	
}
