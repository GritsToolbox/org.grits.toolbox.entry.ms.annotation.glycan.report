package org.grits.toolbox.entry.ms.annotation.glycan.report.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridEditor;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ModalDialog;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.ListenerFactory;

import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.adapter.MSGlycanAnnotationReportDialogAdapter;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;

/**
 * To merge Ms Annotation Entries
 * 
 * @author aljadda
 * 
 */
public class MSGlycanAnnotationReportDialog extends ModalDialog {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportDialog.class);

	private Text nameText;
	private String name;
	private Label nameLabel;

	private Text descriptionText;
	private String description;
	private Label descriptionLabel;

	private Label listLabel;
	private Label intervalLabel;

	// Texts
	private Text intervalText;
	private String interval = null;
	private boolean blnPPM;

	// buttons
	private Button btnPPMCheck = null;
	private Button daltonCheck = null;
	private Button okButton = null;
	private Button cancelButton = null;
	private String accuracyType = "Ppm";
	private java.util.List<Entry> msAnnEntryList = null;

	private MSGlycanAnnotationReportDialogAdapter msAnnEntrySelectionAdapter = null;

	// org.eclipse.swt.widgets.List;
	// private List msAnnDialoglist;
	private Grid msAnnDialogGrid = null;
	private Button btnUp = null;
	private Button btnDown = null;


	// create HashSet
	private HashMap<String, String> listEntries = new HashMap<String, String>();

	public MSGlycanAnnotationReportDialog(Shell parentShell,
			java.util.List<Entry> entries) {
		super(parentShell);
		if (entries == null) {
			this.msAnnEntryList = new java.util.ArrayList<Entry>();
		} else {
			this.msAnnEntryList = entries;
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle("Merge MS Annotations");
		setMessage("");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		this.parent = parent;

		initGrid(parent);		
		createDisplayName(parent);
		createDescription(parent);
		createIntervalAndPpm(parent);

		createEmptyLine(parent);
		createListHeader(parent);		
		createList(parent);
		createAddAndDelButtons(parent);

		createEmptyLine(parent);
		createLineSeparator(parent);
		createMergeOKAndCancelButtons(parent);

		return parent;
	}

	private void initGrid(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);
	}

	private void createDisplayName(Composite parent) {
		/*
		 * First row starts:name
		 */
		GridData nameData = new GridData();
		nameData.grabExcessHorizontalSpace = false;
		nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText("Display Name");
		nameLabel = setMandatoryLabel(nameLabel);
		nameLabel.setLayoutData(nameData);

		GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
		nameTextData.grabExcessHorizontalSpace = true;
		nameTextData.horizontalSpan = 6;
		nameText = new Text(parent, SWT.BORDER);
		nameText.setLayoutData(nameTextData);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});		
	}

	private void createDescription(Composite parent) {
		/*
		 * Second row starts:description with minimum size 80;
		 */
		GridData descriptionData = new GridData();
		descriptionData.grabExcessHorizontalSpace = false;
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText("Description");
		descriptionLabel.setLayoutData(descriptionData);

		GridData descriptionTextData = new GridData(GridData.FILL_BOTH);
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.horizontalSpan = 6;
		descriptionText = new Text(parent, SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER);
		descriptionText.setLayoutData(descriptionTextData);
		descriptionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
		descriptionText.addTraverseListener(ListenerFactory
				.getTabTraverseListener());
		descriptionText.addKeyListener(ListenerFactory.getCTRLAListener());		
	}

	private void createEmptyLine(Composite parent) {
		GridData dummy = new GridData();
		Label dummyLbl = new Label(parent, SWT.NONE);
		dummy.grabExcessHorizontalSpace = true;
		dummy.horizontalSpan = 7;
		dummyLbl.setLayoutData(dummy);
	}

	private void createLineSeparator(Composite parent) {
		GridData dummy = new GridData(GridData.FILL_HORIZONTAL);
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);	    
		dummy.grabExcessHorizontalSpace = true;
		dummy.horizontalSpan = 7;
		separator.setLayoutData(dummy);
	}

	private void createListHeader(Composite parent) {
		/*
		 * third row starts:List
		 */
		GridData listLabelData = new GridData();
		listLabel = new Label(parent, SWT.NONE);
		listLabelData.grabExcessHorizontalSpace = true;
		listLabelData.horizontalSpan = 7;
		listLabel.setText("MS Annotations");
		listLabel.setLayoutData(listLabelData);
		listLabel = setMandatoryLabel(listLabel);		
	}

	private void createList(Composite parent) {
		msAnnDialogGrid = new Grid(parent, SWT.BORDER);	
		msAnnDialogGrid.setHeaderVisible(true);

		GridColumn gridColumn = new GridColumn(msAnnDialogGrid, SWT.NONE);
		gridColumn.setWidth(300);
		gridColumn.setText("Annotation Result Name");

		GridColumn gridColumn2 = new GridColumn(msAnnDialogGrid, SWT.NONE);
		gridColumn2.setWidth(300);
		gridColumn2.setText("Alias (user-defined)");

		setListData();
		int desiredHeight = msAnnDialogGrid.getItemHeight() * (msAnnDialogGrid.getItemCount()+1) + msAnnDialogGrid.getHeaderHeight();
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.minimumHeight = desiredHeight+5;
		gridData.horizontalSpan = 6;
		gridData.verticalSpan = 2;
		msAnnDialogGrid.setLayoutData(gridData);
		msAnnDialogGrid.setAutoHeight(true);

		btnUp = new Button(parent, SWT.ARROW | SWT.UP | SWT.BORDER);
		btnUp.setText("Up");
		GridData gdBtnUp = new GridData(SWT.END, SWT.BEGINNING, false, true);
		gdBtnUp.horizontalSpan = 1;
		btnUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				moveItem(SWT.UP);
			}
		});

		btnUp.setLayoutData(gdBtnUp);

		btnDown = new Button(parent,  SWT.ARROW | SWT.DOWN | SWT.BORDER);
		btnDown.setText("D");
		GridData gdBtnDown = new GridData(SWT.END, SWT.END, false, true);
		gdBtnDown.horizontalSpan = 1;
		btnDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				moveItem(SWT.DOWN);
			}
		});

		btnDown.setLayoutData(gdBtnDown);

		// update list
		msAnnDialogGrid.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				// re-calculate the height of the grid
				int desiredHeight = msAnnDialogGrid.getItemHeight() * (msAnnDialogGrid.getItemCount()+1) + msAnnDialogGrid.getHeaderHeight();
				GridData gd = (GridData) msAnnDialogGrid.getLayoutData();
				gd.minimumHeight = desiredHeight + 5;
				msAnnDialogGrid.setLayoutData(gd);
				parent.layout();
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});

		final GridEditor editor = new GridEditor( msAnnDialogGrid);
		msAnnDialogGrid.addMouseListener(new MouseAdapter() {
			public void mouseDown( MouseEvent e) {

				Control oldEditor = editor.getEditor();
				if ( oldEditor != null)
					oldEditor.dispose();

				Point pt = new Point( e.x, e.y);

				final GridItem item = msAnnDialogGrid.getItem( pt);
				final Point cell = msAnnDialogGrid.getCell( pt);
				if ( item == null || cell == null)
					return;

				if( cell.x == 0 ) { // can't edit the first column!
					return;
				}

				msAnnDialogGrid.deselect(cell.y);
				// The control that will be the editor must be a child of the Table
				final Text newEditor = new Text( msAnnDialogGrid, SWT.BORDER | SWT.SINGLE);
				String curText = item.getText(cell.x);
				newEditor.setText(curText);
				editor.setEditor( newEditor, item, cell.x);
				editor.grabHorizontal = true;
				editor.grabVertical = true;
				newEditor.addKeyListener(new KeyListener() {

					@Override
					public void keyReleased(KeyEvent e) {
						item.setText(cell.x, newEditor.getText());		
						listEntries.put( item.getText(0), newEditor.getText() );
						if (isReadyToFinish()) {
							setPageComplete(true);
						} else {
							setPageComplete(false);
						}
					}

					@Override
					public void keyPressed(KeyEvent e) {
						// TODO Auto-generated method stub						
					}
				});
				newEditor.forceFocus();
				newEditor.setSelection(newEditor.getText().length());
				newEditor.selectAll();
				//                newEditor.setSelection(0, curText.length());
			}
		});
	}	


	private void setListData() {
		for (int i = 0; i < msAnnEntryList.size(); i++) {
			Entry entry = msAnnEntryList.get(i);
			MSGlycanAnnotationProperty prop = (MSGlycanAnnotationProperty) entry.getProperty();
			String sEntryId = prop.getMSAnnotationMetaData().getAnnotationId();
			if( sEntryId == null ) {
				continue;
			}
			String displayName = MSGlycanAnnotationReportTableDataProcessorUtil.getDisplayNameByEntryName(sEntryId);
			if( displayName == null ) {
				continue;
			}

			listEntries.put(displayName, displayName);
			GridItem item = new GridItem(msAnnDialogGrid, SWT.NONE);

			item.setText(0, displayName);

			item.setBackground(0, Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			item.setText(1, displayName);
		}		
	}

	private void moveItem( int direction ) {
		int iFoundPos = msAnnDialogGrid.getSelectionIndex();
		if( iFoundPos < 0 ) {
			return;
		}
		GridItem foundItem = msAnnDialogGrid.getItem(iFoundPos);
		GridItem switchItem = null;
		int iSwitchInx = -1;
		if( direction == SWT.UP ) {
			if( iFoundPos == 0 ) {
				return;
			}
			iSwitchInx = iFoundPos - 1;
		} else {
			if( iFoundPos == msAnnDialogGrid.getItemCount() - 1) {
				return;
			}
			iSwitchInx = iFoundPos + 1;
		}
		switchItem = msAnnDialogGrid.getItem(iSwitchInx);
		String s0 = switchItem.getText(0);
		String s1 = switchItem.getText(1);

		switchItem.setText(0, foundItem.getText(0));
		switchItem.setText(1, foundItem.getText(1));

		foundItem.setText(0, s0);
		foundItem.setText(1, s1);
		msAnnDialogGrid.setSelection(iSwitchInx);

		Entry curEntry = msAnnEntryList.remove(iFoundPos);
		msAnnEntryList.add(iSwitchInx, curEntry);
		logger.debug("Done with move");
	}

	private void createAddAndDelButtons(Composite parent) {
		// create a grdiData for OKButton
		Label dummy = new Label(parent, SWT.NONE);
		GridData gdDummy = new GridData();
		dummy.setLayoutData(gdDummy);

		Label dummy2 = new Label(parent, SWT.NONE);
		GridData gdDummy2 = new GridData();
		gdDummy2.horizontalSpan = 2;
		gdDummy2.grabExcessHorizontalSpace = true;
		dummy2.setLayoutData(gdDummy2);

		Label dummy3 = new Label(parent, SWT.NONE);
		GridData gdDummy3 = new GridData();
		dummy3.setLayoutData(gdDummy3);

		GridData gdAddBtn = new GridData();
		gdAddBtn.grabExcessHorizontalSpace = false;
		gdAddBtn.horizontalAlignment = GridData.END;
		gdAddBtn.horizontalSpan = 1;
		Button btnAddButton = new Button(parent, SWT.PUSH);
		btnAddButton.setText("  Add  ");
//		msAnnEntrySelectionAdapter = new MSGlycanAnnotationReportDialogAdapter(
//				MSAnnotationProperty.TYPE, "Select Annotation",
//				"Select an MS Annotation");
		msAnnEntrySelectionAdapter = new MSGlycanAnnotationReportDialogAdapter(
				getAnnotationPropertyType(), "MS Annotation Selection",
				"Choose an annotation to add");
		msAnnEntrySelectionAdapter.setParent(parent);
		msAnnEntrySelectionAdapter.setList(msAnnDialogGrid);
		msAnnEntrySelectionAdapter.setListEntries(listEntries);
		msAnnEntrySelectionAdapter.setEntries(msAnnEntryList);
		btnAddButton.addSelectionListener(msAnnEntrySelectionAdapter);
		btnAddButton.setLayoutData(gdAddBtn);

		Button deleteButton = new Button(parent, SWT.PUSH);
		GridData gdDelBtn = new GridData();
		gdDelBtn.grabExcessHorizontalSpace = false;
		gdDelBtn.horizontalAlignment = GridData.END;
		gdDelBtn.horizontalSpan = 1;
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(gdDelBtn);
		deleteButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// then delete selected items from the list
				int iRemoveInx = msAnnDialogGrid.getSelectionIndex();
				msAnnEntryList.remove(iRemoveInx);
				msAnnDialogGrid.remove(iRemoveInx);
				listEntries.clear();
				for (int i = 0; i < msAnnDialogGrid.getItemCount(); i++) {
					String displayName = msAnnDialogGrid.getItem(i).getText(0).trim();
					String alias = msAnnDialogGrid.getItem(i).getText(1).trim();
					listEntries.put(displayName, alias);
				}

				// if list is empty should disable OKbutton
				if (listEntries.isEmpty()) {
					setPageComplete(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		Label dummy4 = new Label(parent, SWT.NONE);		
	}

	protected String getAnnotationPropertyType() {
		return MSGlycanAnnotationProperty.TYPE;
	}

	private void createIntervalAndPpm(Composite parent) {

		// create label
		intervalLabel = new Label(parent, SWT.NONE);
		intervalLabel.setText("Interval");
		intervalLabel = setMandatoryLabel(intervalLabel);

		// create text
		intervalText = new Text(parent, SWT.BORDER | SWT.SINGLE);
		intervalText.setText("500");
		interval = "500";
		GridData intervalgd = new GridData(GridData.FILL_HORIZONTAL);
		intervalgd.horizontalSpan = 3;

		intervalText.setLayoutData(intervalgd);
		intervalText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				interval = intervalText.getText();
				if (!intervalText.getText().isEmpty()) {
					if (isReadyToFinish()) {
						setPageComplete(true);
					} else {
						setPageComplete(false);
					}
				} else {
					// Interval needs to be there
					setPageComplete(false);
				}
			}
		});

		// create two check boxes
		btnPPMCheck = new Button(parent, SWT.RADIO);
		btnPPMCheck.setSelection(true);
		btnPPMCheck.setText("PPM");
		blnPPM = true;
		btnPPMCheck.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		btnPPMCheck.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!btnPPMCheck.getSelection()) {
					btnPPMCheck.setSelection(true);
					accuracyType = "Ppm";
				} else {
					daltonCheck.setSelection(false);
				}
				blnPPM = btnPPMCheck.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		//		Label PPMlabel = new Label(parent2, SWT.NONE);
		//		PPMlabel.setText("PPM");

		daltonCheck = new Button(parent, SWT.RADIO);
		daltonCheck.setText("Dalton");
		daltonCheck.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		daltonCheck.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!daltonCheck.getSelection()) {
					daltonCheck.setSelection(true);
					accuracyType = "Dalton";
				} else {
					btnPPMCheck.setSelection(false);
				}
				blnPPM = btnPPMCheck.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		//		Label lblDalton = new Label(parent2, SWT.NONE);
		//		lblDalton.setText("Dalton");
		Label dummy = new Label(parent, SWT.NONE);
	}


	private void createMergeOKAndCancelButtons(Composite parent) {
		// create a grdiData for OKButton
		Label dummy2 = new Label(parent, SWT.NONE);
		GridData gdDummy2 = new GridData();
		gdDummy2.horizontalSpan = 4;
		gdDummy2.grabExcessHorizontalSpace = true;
		dummy2.setLayoutData(gdDummy2);
		
		GridData cancelData = new GridData();
		cancelData.grabExcessHorizontalSpace = false;
		cancelData.horizontalAlignment = GridData.END;
		cancelData.horizontalSpan = 1;
		cancelButton = new Button(parent, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				cancelPressed();
			}
		});
		cancelButton.setLayoutData(cancelData);
			

		GridData okData = new GridData();
		okData.grabExcessHorizontalSpace = false;
		okData.horizontalAlignment = GridData.END;
		okData.horizontalSpan = 1;
		okButton = new Button(parent, SWT.PUSH);
		okButton.setText("   OK   ");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				okPressed();
				close();
			}
		});
		okButton.setLayoutData(okData);
		// check is ready to finish
		if (isReadyToFinish()) {
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}

		new Label(parent, SWT.NONE);	
	}

	protected boolean isReadyToFinish() {
		// check if list is not empty and interval is not empty and name is not
		// empty.
		if (!intervalText.getText().isEmpty() && !nameText.getText().isEmpty()
				&& msAnnDialogGrid.getItemCount() != 0) {
			// check if name is not too long and description is not too long
			if (!checkBasicLengthCheck(nameLabel, nameText, 0, 32)) {
				return false;
			} else {
				// if not then look for same name report
				if (this.msAnnEntryList != null
						&& this.msAnnEntryList.size() != 0) {
					for (Entry child : DataModelSearch.findParentByType(
							this.msAnnEntryList.get(0), ProjectProperty.TYPE)
							.getChildren()) {
						if (child.getProperty().getType()
								.equals(MSGlycanAnnotationReportProperty.TYPE)) {
							// then look for the same name given by the user
							for (Entry child2 : child.getChildren()) {
								if (child2.getDisplayName().equals(
										nameText.getText())) {
									setError(nameLabel,
											"The name is in use. Please choose another name.");
									return false;
								}
							}
						}
					}
				}
			}
			if (!descriptionLabel.getText().isEmpty()) {
				// check if description is not empty, then should not go above
				// its limit
				if (!checkBasicLengthCheck(descriptionLabel, descriptionText,
						0, Integer.parseInt(PropertyHandler
								.getVariable("descriptionLength")))) {
					return false;
				}
			}
			// check interval
			boolean ok = true;
			try {
				double number = Double.parseDouble(intervalText.getText());
				if (number < 0) {
					ok = false;
					setError(intervalLabel,
							"Interval (" + intervalText.getText()
							+ ") cannot be less than 0");
				}
			} catch (NumberFormatException e2) {
				ok = false;
				setError(intervalLabel, "Interval (" + intervalText.getText()
						+ ") is an invalid number");
			}
			if (ok) {
				removeError(intervalLabel);
			} else {
				return false;
			}
			List<String> alContains = new ArrayList<>();

			for (int i = 0; i < msAnnDialogGrid.getItemCount(); i++) {
				String displayName = msAnnDialogGrid.getItem(i).getText(0);
				String alias = msAnnDialogGrid.getItem(i).getText(1);
				if( alias.equals("") ) {
					setErrorMessage("Alias for " + displayName + " cannot be blank.");
					return false;
				}
				if( alContains.contains(alias) ) {
					setErrorMessage("Duplicate aliases are not allowed.");
					return false;
				}
				alContains.add(alias);
			}
			setErrorMessage(null);
			return true;
		}
		return false;
	}

	private void setPageComplete(boolean flag) {
		if (flag) {
			// then save inputs
			name = nameText.getText();
			description = descriptionText.getText();
		}
		okButton.setEnabled(flag);
	}


	@Override
	protected boolean isValidInput() {	
		return true;
	}

	@Override
	public Entry createEntry() {
		// return a new SimGlycanMerge Entry
		Entry simGlycanMergerEntry = new Entry();
		simGlycanMergerEntry.setDisplayName(name);
		MSGlycanAnnotationReportProperty property = new MSGlycanAnnotationReportProperty();
		MSGlycanAnnotationReportMetaData metaData = new MSGlycanAnnotationReportMetaData();
		metaData.setName(name);
		metaData.setVersion( MSGlycanAnnotationReportMetaData.CURRENT_VERSION );
//		metaData.setReportId(name);
		
		metaData.setDescription(description);
		metaData.setCreationDate(new Date());
		metaData.setUpdateDate(metaData.getCreationDate());
		property.setMsGlycanAnnotReportMetaData(metaData);
		simGlycanMergerEntry.setProperty(property);
		return simGlycanMergerEntry;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setSimGLycanEntryList(java.util.List<Entry> simGLycanEntryList) {
		this.msAnnEntryList = simGLycanEntryList;
	}

	public boolean getPPM() {
		return blnPPM;
	}

	public double getInterval() {
		return Double.parseDouble(interval);
	}

	public String getAccuracyType() {
		return accuracyType;
	}

	public void setAccuracyType(String accuracyType) {
		this.accuracyType = accuracyType;
	}

	public java.util.List<Entry> getAnnotationEntryList() {
		return msAnnEntryList;
	}

	public String getAnnotationInfo(String sEntryName) {
		return listEntries.get(sEntryName);
	}

}
