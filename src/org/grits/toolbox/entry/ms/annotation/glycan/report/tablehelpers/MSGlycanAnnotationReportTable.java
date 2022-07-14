package org.grits.toolbox.entry.ms.annotation.glycan.report.tablehelpers;

/**
 * NatTable subclass designed to generate a table for merged MSGlycanAnnotation Tables
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * 
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
//import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommand;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.MSGlycanAnnotationReportTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.command.MSGlycanAnnotationReportViewColumnChooserCommandHandler;
import org.grits.toolbox.entry.ms.annotation.glycan.report.command.ViewMSOverviewFromMergeCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.views.MSGlycanAnnotationReportMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTableDataChangedMessage;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationEntityScroller;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationTableBase;
import org.grits.toolbox.merge.om.data.ExperimentAnnotation;
import org.grits.toolbox.merge.om.data.MergeSettings;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationReportTable extends MSGlycanAnnotationTable {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportTable.class);

	public MSGlycanAnnotationReportTable(MSAnnotationTableBase parent, TableDataProcessor xmlExtractor) throws Exception {
		super(parent, xmlExtractor);
	}

	public MSGlycanAnnotationReportTable(Composite parent, MSGlycanAnnotationReportTable parentTable, int iRowNumber, int iScanNum, String sRowId) {
		super(parent, parentTable, iRowNumber, iScanNum, sRowId);
	}	

	/* 
	 * (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable#createMainTable()
	 * 
	 * Create the common components of the table
	 */
	@Override
	public void createMainTable() throws Exception  {	
		try {
			initCommonTableComponents();
			initColumnChooserLayer();

			updateEventListForVisibility();
			updateImageRegistry( false );

			updateRowVisibilityAfterRead();
			//		registerEditableCells(configRegistry);
			finishNatTable();
			performAutoResizeAfterPaint();
		} catch (Exception e) {
			logger.error("Failed to create table.", e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public int getScanNumberForVisibility( MSAnnotationTableDataObject tdo, int i ) {
		// there is no parent scan number for reports
		return 0;
	}

	@Override
	public int getScanNumberForVisibility( MSAnnotationTable table, int i ) {
		// there is no parent scan number for reports
		return 0;
	}

	@Override
	protected void initColumnChooserLayer() {
		MSGlycanAnnotationReportViewColumnChooserCommandHandler columnChooserCommandHandler = new MSGlycanAnnotationReportViewColumnChooserCommandHandler( this );		
		columnGroupHeaderLayer.registerCommandHandler(columnChooserCommandHandler);		
	}


	private MSGlycanAnnotationReportTableDataObject getMyTableDataObject() {
		return (MSGlycanAnnotationReportTableDataObject) getGRITSTableDataObject();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable#refreshTableImages()
	 */
	@Override
	public void refreshTableImages() {
		updateImageRegistry(true);
		performAutoResize();		
	}

	/* 
	 * (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable#initImageConfigRegistry()
	 * 
	 * Initialize the image config registry
	 */
	@Override
	protected void initImageConfigRegistry() {
		if ( getMyTableDataObject().getCartoonCols() != null ) {
			if ( getGRITSTableDataObject().getTableData() != null && ! getGRITSTableDataObject().getTableData().isEmpty() ) {
				for(int i = 0; i < getGRITSTableDataObject().getTableData().size(); i++ ) {
					for( int j = 0; j < getMyTableDataObject().getCartoonCols().size(); j++ ) {
						Integer iCartoonCol = getMyTableDataObject().getCartoonCols().get(j);
						String sCartoonFile = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(iCartoonCol);
						if ( sCartoonFile == null || sCartoonFile.equals("") ) 
							continue;
						int iInx = sCartoonFile.indexOf(".png");
						String sSequence = sCartoonFile.substring(0, iInx);
						registerImage(configRegistry, sSequence, sCartoonFile);
					}
				}
			}
		}
	}	

	/* 
	 * (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable#updateImageRegistry(boolean)
	 * 
	 * Update the image config registry when needing to redraw images
	 */
	@Override
	protected void updateImageRegistry( boolean bForceImageRedraw ) {	
		if ( getGRITSTableDataObject().getTableData() == null || 
				getGRITSTableDataObject().getTableData().isEmpty() || 
				getMyTableDataObject().getPeakIdCols().isEmpty() ||
				getMyTableDataObject().getCartoonCols().isEmpty() ) 
			return;
		// note: only applicable for non-merge results
		for(int i = 0; i < getGRITSTableDataObject().getTableData().size(); i++ ) {
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow()
					.get( getMyTableDataObject().getCartoonCols().get(0) ) == null )
				continue;
			Integer iScanNum = null;
			if( getMyTableDataObject().getScanNoCols() != null && ! getMyTableDataObject().getScanNoCols().isEmpty() ) {
				if( getGRITSTableDataObject().getTableData().get(i).getDataRow().get(getMyTableDataObject().getScanNoCols().get(0)) != null ) {
					iScanNum = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(getMyTableDataObject().getScanNoCols().get(0));
				}
			}
			// MERGE REPORT HACK!
			// There is an extra column for the "selected"  cartoon, but we don't know which exp and scan produced it
			// FIX:  find the first exp whose GlycanID assigned matches the selected. Then use that scan num

			// must now do the same for the images
			for( int j = 0; j < getMyTableDataObject().getCartoonCols().size(); j++ ) {
				int iCartoonCol = getMyTableDataObject().getCartoonCols().get(j);
				Integer iPeakId = null;
				String sCartoonID = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(iCartoonCol);
				if ( sCartoonID == null || sCartoonID.equals("") ) 
					continue;
				int iInx = sCartoonID.indexOf(".png");
				if( iInx < 0 ) {
					logger.error("Invalid format for cartoon string. PNG not found!");
				}
				String sSequence = sCartoonID.substring(0, iInx);
				if ( sSequence == null || sSequence.equals("") ) 
					continue;
				if ( getMyTableDataObject().getCartoonCols().size() == getMyTableDataObject().getPeakIdCols().size() ) { // probably size=1 for single SimGlgetMyTableDataObject()taObject) getSimDataObject()).getPeakIdCols().get(j);
					iPeakId = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
							.get( getMyTableDataObject().getPeakIdCols().get(j) );
				} else { // must be merge report
					if ( j == 0 ) { // find the scan num matching selected glycan id
						for( int k = 1; k < getMyTableDataObject().getCartoonCols().size(); k++ ) {
							String sThisCartoonID = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow()
									.get(getMyTableDataObject().getCartoonCols().get(k));
							if ( sThisCartoonID != null && sCartoonID.equals(sThisCartoonID) ) {
								iPeakId = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
										.get( getMyTableDataObject().getPeakIdCols().get(k-1) );
							}
						}
					} else {
						iPeakId = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
								.get( getMyTableDataObject().getPeakIdCols().get(j-1) );
						if( iPeakId == null ){
							logger.debug("Null peak id!");
						}
					}
				}
				boolean bInvisible = false;
				if( iPeakId != null ){
					bInvisible = getGRITSTableDataObject().isInvisibleRow(iScanNum, iPeakId.toString());
				}
				Object ob = configRegistry.getSpecificConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
				if ( bForceImageRedraw ) {
					configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
					registerImage(configRegistry, sSequence, sCartoonID);							
				} else {
					if ( ob == null && ! bInvisible )
						registerImage(configRegistry, sSequence, sCartoonID);
					else if ( ob != null && bInvisible )
						configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
				}
			}
		}

	}

	/* 
	 * (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.tablehelpers.MassSpecTable#getPreferenceSettingsFromCurrentView()
	 * 
	 * Creates a new instance of TableViewerColumnSettings and populates th ecolumn order and visibility based
	 * on the current report table
	 */
	@Override
	public TableViewerColumnSettings getPreferenceSettingsFromCurrentView() {
		if ( this.columnGroupModel == null || this.columnGroupModel .isEmpty() ) 
			return super.getPreferenceSettingsFromCurrentView();

		if ( getMyTableDataObject().getFirstGroupIndices() == null )
			getMyTableDataObject().discoverGroups(this.columnGroupModel);

		TableViewerColumnSettings newEntity = new TableViewerColumnSettings();
		if (getMyTableDataObject().getFirstGroupIndices().size() < 2) {
			logger.log(Level.WARN, "Not enough column groups to update visibility");
			return newEntity;
		}

		int iPos = 0;
		for (int i = 0; i < 2; i++) {
			ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(i));
			List<Integer> members = group.getMembers();
			for (int j = 0; j < members.size(); j++) {
				int iColLayerInx = members.get(j);
				int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);

				GRITSColumnHeader header = (GRITSColumnHeader) this.columnHeaderDataLayer.getDataValueByPosition(iColLayerPos, 0);
				newEntity.setVisColInx(header, iPos++);
			}
		}
		return newEntity;
	}

	/**
	 * Using the EventBroker, is called to update the current Merge Report based on changes from one of the source MS Glycan Annotation
	 * viewers
	 * @param message
	 * 		The event broker message containing necessary information from MS Glycan Annotation table to update rows in the Merge report.
	 */
	public void updateTable(MSAnnotationTableDataChangedMessage message) {
		int iNumRows = message.getParentTable().getBottomDataLayer().getRowCount();
		if (iNumRows == 0)
			return;
		for (int i = 0; i < iNumRows; i++) {
			if (message.getParentTable().getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) message.getParentTable().getGRITSTableDataObject()).getPeakIdCols().get(0), i) == null)
				continue;
			if (message.getParentTable().getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) message.getParentTable().getGRITSTableDataObject()).getFeatureIdCols().get(0), i) == null)
				continue;
			Integer iPeakId = ((Integer) message.getParentTable().getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) message.getParentTable().getGRITSTableDataObject()).getPeakIdCols().get(0), i));
			String sId = message.getParentTable().getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) message.getParentTable().getGRITSTableDataObject()).getFeatureIdCols().get(0), i).toString();
			String sCompareTo = MSAnnotationEntityScroller.getCombinedKeyForLookup(iPeakId, sId);
			if( message.getFeature().getId().equals(sId)) {
				// toggle this row
				boolean bCurSelected = ((Boolean) message.getParentTable().getBottomDataLayer().getDataValueByPosition(0, i)).booleanValue();
				MSGlycanAnnotationReportTableDataProcessor proc = (MSGlycanAnnotationReportTableDataProcessor) getTableDataProcessor();
				
				String sExpId = null; 
				if(message.getParentTable().getTableDataProcessor().getEntry().getProperty() instanceof MSAnnotationEntityProperty ) {
					MSAnnotationEntityProperty prop = (MSAnnotationEntityProperty) message.getParentTable().getTableDataProcessor().getEntry().getProperty();
					sExpId = ((MSAnnotationProperty) prop.getParentProperty()).getMSAnnotationMetaData().getAnnotationId();
				}

				int iRowInx = proc.findMatchingRow( sExpId, iPeakId );
				if( iRowInx >= 0 ) {
					boolean bDirty = proc.toggleFeatureSelection(sExpId, iRowInx, message, bCurSelected);
					if( bDirty ) {
						finishUpdateHiddenRowsAfterEdit(true);
						parentView.setDirty(true);
					}
				}
				break;
			}
		}				
	}
	
	@Override
	public boolean startUpdateHiddenRowsAfterEdit(MSAnnotationTable subsetTable) {
		return super.startUpdateHiddenRowsAfterEdit(subsetTable);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable#updateViewFromPreferenceSettings()
	 * 
	 * Updates the positions in the report table based on current stored preferences
	 */
	@Override
	public boolean updateViewFromPreferenceSettings() {
		boolean bTotalSuccess = true;
		try {
			if ( this.columnGroupModel == null || this.columnGroupModel.isEmpty() ) {
				return super.updateViewFromPreferenceSettings();
			}
			if (this.columnHeaderDataLayer == null || this.columnHeaderDataLayer.getColumnCount() == 0)
				return false;
			int iNumCols = this.columnHeaderDataLayer.getColumnCount();
			if (iNumCols == 0)
				return false;

			this.columnHideShowLayer.showAllColumns(); // first show all columns
			ArrayList<Integer> alHiddenCols = new ArrayList<Integer>();
			if ( getMyTableDataObject().getFirstGroupIndices() == null )
				getMyTableDataObject().discoverGroups(this.columnGroupModel);

			for( int i = 0; i < getMyTableDataObject().getFirstGroupIndices().size(); i++ ) {
				ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(i));  // change 07/31/2013. No longer using "Exp" text so relying on order
				List<Integer> members = group.getMembers();
				for( int j = 0; j < members.size(); j++ ) {  // I believe members are index based
					int iColLayerInx = members.get(j);
					int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
					String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColLayerPos, 0);
					int iColShowLayerPos = LayerUtil.convertColumnPosition(this.columnHeaderDataLayer, iColLayerPos, this.columnHideShowLayer);
					if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings()
							.hasColumn(sHeaderKey)) {
						int iPrefColPos = getGRITSTableDataObject().getTablePreferences()
								.getPreferenceSettings()
								.getVisColInx(sHeaderKey);
						if ( iPrefColPos == -1 ) {
							alHiddenCols.add(iColShowLayerPos);
						} 
					} else { // not there????
						alHiddenCols.add(iColShowLayerPos);
						logger.warn("Header: " + sHeaderKey + " not found in preferences!");
					}
				}
			}
			this.columnHideShowLayer.hideColumnPositions(alHiddenCols);
			Object selCell = getGRITSTableDataObject().getLastHeader().get(0);
			boolean bAddSelect = selCell.equals(TableDataProcessor.selColHeader);

			int iNumVisFirstGroup = 0;
			for( int i = 0; i < getMyTableDataObject().getFirstGroupIndices().size(); i++ ) {
				ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(i));  // change 07/31/2013. No longer using "Exp" text so relying on order
				int iNumNonHidden = 0;
				List<Integer> members = group.getMembers();
				for( int j = 0; j < members.size(); j++ ) {  // I believe members are index based
					int iColLayerInx = members.get(j);
					int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
					String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColLayerPos, 0);
					if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings()
							.hasColumn(sHeaderKey)) {
						int iPrefColPos = getGRITSTableDataObject().getTablePreferences()
								.getPreferenceSettings()
								.getVisColInx(sHeaderKey);
						if ( iPrefColPos != -1 ) {
							iNumNonHidden++;
						} 
						if ( i == 0 ) {
							iNumVisFirstGroup++;
						}
					}			
				}
				bTotalSuccess &= doReorderForExpGroup(this.columnHeaderDataLayer, this.columnHideShowLayer, group, i == 0 ? 0 : iNumVisFirstGroup, iNumNonHidden, i, bAddSelect);
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		if( ! bTotalSuccess ) {
			// something was wrong with the preferences!
			logger.error("There is an error in the preference settings.");
			ErrorUtils.createErrorMessageBox(getShell(), "Invalid Column Settings", "Column preference settings were invalid. You may want to reconfigure them.   Please click \"Restore Defaults\" first.");
			return false;
		}
		return true;
	}

	/**
	 * Column visibility and order can be group-specific (group being an individual experiment)
	 * This method applies visibility and order for a particular group
	 * @param columnLayer
	 * @param columnShowLayer
	 * @param group
	 * @param iNumFirstGroup
	 * @param iNumNonHidden
	 * @param iGroupNum
	 * @param bAddSelect
	 * @return
	 */
	private boolean doReorderForExpGroup( DataLayer columnLayer, ColumnHideShowLayer columnShowLayer, ColumnGroup group, 
			int iNumFirstGroup, int iNumNonHidden,
			int iGroupNum, boolean bAddSelect  ) {
		List<Integer> members = group.getMembers();
		int iAdder = iGroupNum > 0 ? (iGroupNum - 1) * iNumNonHidden : 0;
		iAdder += iNumFirstGroup;
		List<String> lProcessed = new ArrayList<>();
		for (int iPrefColPos = 0; iPrefColPos < iNumNonHidden; iPrefColPos++) { // going in position order of the new PREFERENCES
			GRITSColumnHeader prefHeader = getGRITSTableDataObject()
					.getTablePreferences().getPreferenceSettings()
					.getColumnAtVisColInx(iPrefColPos + iNumFirstGroup);
			if ( prefHeader == null )
				continue;
			for (int iMemInx = 0; iMemInx < members.size(); iMemInx++) { // column index based
				int iColLayerInx = members.get(iMemInx);
				int iColPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
				int iFromPos = LayerUtil.convertColumnPosition(
						this.columnHeaderDataLayer, iColPos,
						this.columnHideShowLayer);
				String sThisHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
				if (prefHeader.getKeyValue().equals(sThisHeaderKey)) {
					int iToPos = iPrefColPos + iAdder;
					if (bAddSelect)
						iToPos++;
					if (iFromPos + iAdder != iToPos) {
						ColumnReorderCommand command = new ColumnReorderCommand(
								this.columnHideShowLayer, iFromPos, iToPos);
						this.columnHideShowLayer.doCommand(command);
					} else {
					}
					lProcessed.add(sThisHeaderKey);
					break;
				}
			}
		}
		if( lProcessed.size() != iNumNonHidden ) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable#updatePreferenceSettingsFromCurrentView()
	 * 
	 * Updates preference object based on current column settings in report table
	 */
	@Override
	public void updatePreferenceSettingsFromCurrentView() {
		if ( this.columnGroupModel == null || this.columnGroupModel.isEmpty() ) {
			super.updatePreferenceSettingsFromCurrentView();
			return;
		}
		if (this.columnHeaderDataLayer == null || this.columnHeaderDataLayer.getColumnCount() == 0)
			return;

		if ( getMyTableDataObject().getFirstGroupIndices() == null )
			getMyTableDataObject().discoverGroups(this.columnGroupModel);

		setVisibilityOfGroups(this.columnHeaderDataLayer, this.columnHideShowLayer, this.columnGroupModel, 0);
		setVisibilityOfGroups(this.columnHeaderDataLayer, this.columnHideShowLayer, this.columnGroupModel, 1);

	}

	/**
	 * @param columnLayer
	 * @param columnShowLayer
	 * @param groupModel
	 * @param iGroupNum
	 */
	private void setVisibilityOfGroups(DataLayer columnLayer, 
			ColumnHideShowLayer columnShowLayer, ColumnGroupModel groupModel, int iGroupNum ) {
		// i suppose it is possible for users to rearrange the groups and put the "first" group somewhere else. Well, we 
		// aren't supporting that. the first groups will always be first, thus I will order them separately
		int iAdder = iGroupNum > 0 ? groupModel.getColumnGroupByIndex(0).getSize() : 0;

		ColumnGroup group = groupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(iGroupNum));	
		List<Integer> members = group.getMembers();
		int iNewNumCols = 0;
		for( int iMemInx = 0; iMemInx < members.size(); iMemInx++ ) {
			int iColInx = members.get(iMemInx);
			boolean bHidden = columnShowLayer.isColumnIndexHidden(iColInx);	
			if( ! bHidden ) {
				iNewNumCols++;
			}
		}

		// now iterate over the visible columns using the columnshowlayer and
		// set the preference value
		int iToPos = iAdder;
		for (int iVisPos = 0; iVisPos < iNewNumCols; iVisPos++) { // position
			// based on the column show  header layer
			int iColPos = LayerUtil.convertColumnPosition(
					this.columnHideShowLayer, iVisPos + iAdder,
					this.columnHeaderDataLayer);
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
			if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().hasColumn(sHeaderKey)) {
				GRITSColumnHeader header = getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().getColumnHeader(sHeaderKey);
				getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().setVisColInx(header, iToPos++);
			}
		}

		for (int iMemInx = 0; iMemInx < members.size(); iMemInx++) { // index based
			// off of column layer (all data)
			int iColInx = members.get(iMemInx);
			boolean bHidden = columnShowLayer.isColumnIndexHidden(iColInx);	
			if (!bHidden)
				continue;
			int iColPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColInx);
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
			if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().hasColumn(sHeaderKey)) {
				GRITSColumnHeader header = getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().getColumnHeader(sHeaderKey);
				getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().setVisColInx(header, -1);
			}
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 * 
	 * Double-clicking a row in merge will open the corresonding experiment. GRITS will detect what group the double-clicked
	 * cell belongs to and then open the editor for the corresponding experiment
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		logger.debug("Double click on the table");

		if ( ! (getTableDataProcessor() instanceof MSGlycanAnnotationReportTableDataProcessor) || ! hasColumnGroupHeader() ) {
			logger.error("Double-click on a non-merge table??");
			return;			
		}		
		GridLayer gl = (GridLayer) getLayer();

		int origRow = gl.getRowPositionByY(e.y); 
		if ( origRow < 2 )
			return;

		int origCol = gl.getColumnPositionByX(e.x); 
		if ( origCol < 1 )
			return;

		// The cell that was clicked
		int iRowPostion = LayerUtil.convertRowPosition(gl, origRow, getBottomDataLayer());
		int iColPostion = LayerUtil.convertColumnPosition(gl, origCol, getBottomDataLayer());

		// get the header for the merge report
		MSGlycanAnnotationReportTableDataProcessor proc = (MSGlycanAnnotationReportTableDataProcessor) getTableDataProcessor();
		Object oColHeader = proc.getSimianTableDataObject().getTableHeader().get(0).get(iColPostion);
		if( !( oColHeader instanceof GRITSColumnHeader) ) {
			logger.error("Wrong type when expecting String for experiment name.");
			return;						
		}
		GRITSColumnHeader header = (GRITSColumnHeader) oColHeader;

		// find the MS Annotation entry that matches the grouped header
		int iExpNum = -1;
		MergeSettings settings = proc.getMergeReportSettings();
		for( int i = 0; i < settings.getExperimentList().size(); i++ ) {
			ExperimentAnnotation exp = settings.getExperimentList().get(i);
			if( exp.getAnnotationEntryId().toString().equals(header.getKeyValue()) ) {
				logger.debug("Found the experiment: " + exp.getAnnotationEntryId());
				iExpNum = i;
				break;
			}
		}
		if( iExpNum < 0 ) {
			logger.error("Unable to locate experiment!");
			return;									
		}
		
		// Get the scan and peak ids
		Object scanObj = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getParentNoCol().get(iExpNum), iRowPostion);	
		int iSourceScanNum = -1;		
		try {
			iSourceScanNum = Integer.parseInt(scanObj.toString());
		} catch(NumberFormatException ex) {
			logger.debug(ex.getMessage(), ex);
		}

		Object peakObj = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getSourcePeakIdCol().get(iExpNum), iRowPostion);	
		int iSourcePeakId = -1;		
		try {
			iSourcePeakId = Integer.parseInt(peakObj.toString());
		} catch(NumberFormatException ex) {
			logger.debug(ex.getMessage(), ex);
		}
		if( iSourceScanNum == -1 && iSourcePeakId == -1 ) {
			// nothing we can do to find the data...
			logger.error("Unable to find souce scan number or peak id.");
			return;
		}
		
		if( iSourceScanNum != -1 ) {
			// Preference towards matching the scan number because it is unique..the same peak id could product multiple
			// scans, but ultimately the data are the same. But, if the scan is present, find it!
			iSourcePeakId = -1;
		}
		
		ExperimentAnnotation mergeExp = settings.getExperimentList().get(iExpNum);
		Entry annotEntry = MSAnnotationMultiPageViewer.getEntryByAnnotationId( mergeExp.getAnnotationEntryId().toString() );
		/*
		int iCurMSLevel = 0;
		StringBuilder sb = new StringBuilder(annotEntry.getDisplayName());
		double dMz = 0.0;
		if( dMz == 0.0 ) {
			sb.append(": ");
		} else { // fragmentation pathway
			sb.append("->");
		}
		sb.append("[Scan ");
		sb.append(iScanNum);
		sb.append(", MS");
		sb.append(iCurMSLevel);
		if( dMz > 0.0 ) {
			sb.append(", ");
			sb.append(dMz);
		}
		sb.append("]");
*/
		// first select the current entry in the Project Explorer (I'm not sure this is necessary, though)
		MSGlycanAnnotationReportMultiPageViewer viewer = MSGlycanAnnotationReportMultiPageViewer.getActiveViewer( parentView.getParentEditor().getContext() );
		viewer.selectMSGlycanAnnotationEntry(annotEntry);

		/*
		Entry newEntry = new Entry(); 
		newEntry.setDisplayName(annotEntry.getDisplayName());
		newEntry.setEntryType(annotEntry.getEntryType());
		newEntry.setParent(annotEntry.getParent());
		newEntry.setProperty((Property)annotEntry.getProperty().clone());
		*/
		
		// open the first level editor for the entry (Summary View)
		showMSOverview(annotEntry);		
		MSGlycanAnnotationMultiPageViewer msAnnotView1 = MSGlycanAnnotationMultiPageViewer.getActiveViewer( parentView.getParentEditor().getContext() );
		int iFirstScanNum = -1;
		MSAnnotationTable msTable = (MSAnnotationTable) msAnnotView1.getScansView().getViewBase().getNatTable();
		MSAnnotationTableDataObject tdo = (MSAnnotationTableDataObject) msTable.getGRITSTableDataObject();
		if( tdo.getScanNoCols() != null && ! tdo.getScanNoCols().isEmpty() && 
				msTable.getBottomDataLayer().getDataValueByPosition( tdo.getScanNoCols().get(0), 0 ) != null ) {
			Integer iScan = (Integer) msTable.getBottomDataLayer().getDataValueByPosition( tdo.getScanNoCols().get(0), 0 );
			iFirstScanNum = iScan.intValue();
		}

		// Perform a double click on the appropriate row (by scan) to open the MS Structure view
		msTable.performDoubleClickOnScan(iFirstScanNum, 0.0, -1, null, 1, -1);

		// now find the row that matches by either scan number (preferred) or peak id from the source report
		MSGlycanAnnotationMultiPageViewer msAnnotView2 = MSGlycanAnnotationMultiPageViewer.getActiveViewer( parentView.getParentEditor().getContext() );
		MSAnnotationTable msTable2 = (MSAnnotationTable) msAnnotView2.getPeaksView().get(0).getViewBase().getNatTable();
		MSAnnotationTableDataObject tdo2 = (MSAnnotationTableDataObject)  msTable2.getGRITSTableDataObject();
		for( int i = 0; i < msTable2.getBottomDataLayer().getRowCount(); i++ ) {
			Object targetScanObj = msTable2.getBottomDataLayer().getDataValueByPosition(tdo2.getScanNoCols().get(0), i);	
			int iTargetScanNum = -1;		
			try {
				iTargetScanNum = Integer.parseInt(targetScanObj.toString());
			} catch(NumberFormatException ex) {
				logger.debug(ex.getMessage(), ex);
			}
			if(iTargetScanNum == -1) {
				logger.error("Unable to find scan number source table. Can't continue.");
				return;				
			}
			Object targetPeakObj = msTable2.getBottomDataLayer().getDataValueByPosition(tdo2.getPeakIdCols().get(0), i);	
			int iTargetPeakId = -1;		
			try {
				iTargetPeakId = Integer.parseInt(targetPeakObj.toString());
			} catch(NumberFormatException ex) {
				logger.debug(ex.getMessage(), ex);
			}
			if( (iSourceScanNum != -1 && iTargetScanNum == iSourceScanNum) || 
					(iSourcePeakId != -1 && iTargetPeakId == iSourcePeakId) ) {
				// matched! Now look up the annotation id for that row
				int iAnnotId = -1;
				if( tdo2.getAnnotationIdCols()!= null && ! tdo2.getAnnotationIdCols().isEmpty() ) {
					Object annotObj = msTable2.getBottomDataLayer().getDataValueByPosition( tdo2.getAnnotationIdCols().get(0), i);	
					if( annotObj != null ) {
						try {
							iAnnotId = Integer.parseInt(annotObj.toString());
						} catch(NumberFormatException ex) {
							logger.error(ex.getMessage(), ex);
							return;
						}
					}
				}
				
				// DBW 09/08/17:disabling opening of summary view for now
				// open the Summary viewer
				//msTable2.performDoubleClickOnScan(iTargetScanNum, 0.0, iAnnotId, 2, iFirstScanNum);
				//int iVisPos = LayerUtil.convertRowPosition(msTable2.getBottomDataLayer(), i, msTable2.getViewportLayer());
				//msTable2.moveRowIntoViewport(i);  //mouseDown now selects the row and that action is supposed to move the row in viewport!
				msTable2.performMouseDown(i);
				
				break;
			}
		}

	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable#showMSOverview(org.grits.toolbox.core.datamodel.Entry)
	 */
	@Override
	protected void showMSOverview(Entry newEntry) {
		ViewMSOverviewFromMergeCommandExecutor.showMSOverview(parentView.getParentEditor().getContext(), newEntry);		
	}

}
