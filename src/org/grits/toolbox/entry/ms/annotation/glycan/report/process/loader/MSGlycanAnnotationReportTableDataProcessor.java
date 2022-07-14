package org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.preference.MSGlycanAnnotationReportViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.MSGlycanAnnotationReportTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.dmtranslate.DMExtGlycanFeature;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.dmtranslate.DMExtPeak;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.dmtranslate.DMInterval;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanAnnotation;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.datamodel.ms.tablemodel.MassSpecTableDataObject;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPrecursorPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMScan;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.datamodel.GRITSTableDataObject;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTableDataChangedMessage;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantFileToAlias;
import org.grits.toolbox.merge.action.ExtractRequiredData;
import org.grits.toolbox.merge.om.data.ExperimentAnnotation;
import org.grits.toolbox.merge.om.data.ExtGlycanFeature;
import org.grits.toolbox.merge.om.data.ExtPeak;
import org.grits.toolbox.merge.om.data.Interval;
import org.grits.toolbox.merge.om.data.MergeReport;
import org.grits.toolbox.merge.om.data.MergeSettings;
import org.grits.toolbox.merge.om.data.ReportRow;
import org.grits.toolbox.merge.xml.Deserialize;
import org.grits.toolbox.merge.xml.Serializer;
import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.utils.image.GlycanImageProvider;
import org.grits.toolbox.utils.image.ImageCreationException;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;

/**
 * @author dbrentw (D Brent Weatherly <dbrentw@uga.edu>)
 * Extends MSAnnotationTable with specific options for displaying a report of MS Glycan Annotations 
 */

public class MSGlycanAnnotationReportTableDataProcessor extends MSAnnotationTableDataProcessor {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportTableDataProcessor.class);
	protected Deserialize des = null;
	public static final GRITSColumnHeader FIRST_GROUP = new GRITSColumnHeader("MS Annotation File",
			"ms_annotation_file");

	public MSGlycanAnnotationReportTableDataProcessor(Entry _entry, Property _sourceProperty, int iMinMSLevel) {
		super(_entry, _sourceProperty, iMinMSLevel);
	}

	public MSGlycanAnnotationReportTableDataProcessor(Entry _entry, Property _sourceProperty, FillTypes _fillType,
			int iMinMSLevel) {
		super(_entry, _sourceProperty, _fillType, iMinMSLevel);
	}

	public MSGlycanAnnotationReportTableDataProcessor(TableDataProcessor _parent, Property _sourceProperty,
			FillTypes _fillType, int iMinMSLevel) {
		super(_parent, _sourceProperty, _fillType, iMinMSLevel);
	}

	@Override
	protected void loadExternalQuant() {
		// no external quant, so do nothing when called
	}

	/**
	 * Returns the fully qualified name of the archive file for this merge report
	 * 
	 * @param <none>
	 * @return String value of fully qualified name of the archive file
	 */
	@Override
	public String getScanArchiveFile() {
		if (entry == null)
			return null;

		String sMSFile = null;
		MSGlycanAnnotationReportProperty msProp = (MSGlycanAnnotationReportProperty) getEntry().getProperty();
		sMSFile = msProp.getFullyQualifiedXMLFileName(getEntry());

		return sMSFile;
	}

	/**
	 * @return
	 */
	public MergeSettings getMergeReportSettings() {
		return getMergeReportData().getSettings();
	}

	/**
	 * @return
	 */
	protected MergeReport getMergeReportData() {
		return (MergeReport) data;
	}

	/**
	 * @param data
	 */
	public void setData(MergeReport data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#initializeColumnSettings()
	 */
	@Override
	protected TableViewerColumnSettings initializeColumnSettings() {
		TableViewerColumnSettings newSettings = getNewTableViewerSettings();
		addAnnotationColumns(newSettings);
		return newSettings;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#getNewTableViewerPreferences()
	 */
	@Override
	protected TableViewerPreference getNewTableViewerPreferences() {
		return new MSGlycanAnnotationReportViewerPreference();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#initializePreferences()
	 */
	@Override
	public TableViewerPreference initializePreferences() {
		try {
			TableViewerColumnSettings newSettings = initializeColumnSettings(); // testing ticket 918
			MSGlycanAnnotationReportViewerPreference newPreferences = (MSGlycanAnnotationReportViewerPreference) getNewTableViewerPreferences(); // testing ticket 918

			newPreferences.setPreferenceSettings(newSettings); // testing ticket 918
			MSGlycanAnnotationReportViewerPreference oldPreferences = (MSGlycanAnnotationReportViewerPreference) getSimianTableDataObject()
					.getTablePreferences();
			if (oldPreferences != null) { // preserve previous setting if present
				newPreferences.setHideUnannotatedPeaks(oldPreferences.isHideUnannotatedPeaks());
			}

			return newPreferences;
		} catch (Exception e) {
			logger.error("initializePreferences: unable to initialize preferences.", e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#initializeTableDataObject(org.grits.toolbox.core.datamodel.property.Property)
	 */
	@Override
	public void initializeTableDataObject(Property _sourceProperty) {
		MSGlycanAnnotationReportTableDataObject mobj = new MSGlycanAnnotationReportTableDataObject(1, this.fillType);
		setSimianTableDataObject(mobj);
		getSimianTableDataObject().initializePreferences();
		if (getSimianTableDataObject().getTablePreferences().settingsNeedInitialization()) {
			TableViewerPreference tvp = initializePreferences();
			MSGlycanAnnotationReportTableDataProcessor.setDefaultColumnViewSettings(tvp.getPreferenceSettings());
			getSimianTableDataObject().setTablePreferences(tvp);
			getSimianTableDataObject().getTablePreferences().writePreference();
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#saveChanges()
	 */
	@Override
	public boolean saveChanges() throws Exception {
		try {
			Serializer ser = new Serializer();
			String sSourceFile = getScanArchiveFile();
			//				this.progressBarDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Indeterminant);
			//				this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Reading data file...");
			ser.serialize(getMergeReportData(), sSourceFile);
			//				this.progressBarDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Determinant);
			return true;
		} catch (Exception e) {
			throw new Exception("Unable to write XML File", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#addUnrecognizedHeaders(org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference)
	 */
	@Override
	protected int addUnrecognizedHeaders(MassSpecViewerPreference _preference) {
		int iNumCols = 0;
		try {
			if (getMergeReportData().getFeatureCustomExtraData() != null) {
				iNumCols += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(
						_preference.getPreferenceSettings(), getMergeReportData().getFeatureCustomExtraData(),
						_preference.getPreferenceSettings().getUnrecognizedHeaders());
			}
			if (getMergeReportData().getAnnotationCustomExtraData() != null) {
				iNumCols += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(
						_preference.getPreferenceSettings(), getMergeReportData().getAnnotationCustomExtraData(),
						_preference.getPreferenceSettings().getUnrecognizedHeaders());
			}
			if (getMergeReportData().getPeakCustomExtraData() != null) {
				iNumCols += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(
						_preference.getPreferenceSettings(), getMergeReportData().getPeakCustomExtraData(),
						_preference.getPreferenceSettings().getUnrecognizedHeaders());
			}
		} catch (Exception e) {
			logger.error("readDataFromFile: unable to read mzXML.", e);
		}
		return iNumCols;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#readDataFromFile()
	 */
	@Override
	public boolean readDataFromFile() {
		try {
			this.des = new Deserialize();
			String sSourceFile = getScanArchiveFile();
			this.progressBarDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Indeterminant);
			this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Reading data file...");
			logger.debug("Reading archive: " + sSourceFile);
			this.data = this.des.deserialize(sSourceFile);

			//	test ticket 918		initializeTableDataObject(getSourceProperty());
			this.progressBarDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Determinant);
			return (this.data != null);
		} catch (Exception e) {
			logger.error("readDataFromFile: unable to read mzXML.", e);
		}
		this.progressBarDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Determinant);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#buildTable()
	 */
	@Override
	public void buildTable() throws Exception {
		this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Building table...");
		int iMax = getMergeReportData().getRows().size();
		this.progressBarDialog.getMinorProgressBarListener(0).setMaxValue(iMax);
		// List<Integer> alEntryIds = getEntryIds();
		ArrayList<ArrayList<GRITSColumnHeader>> alHeaders = getHeaderLines(
				getTempPreference().getPreferenceSettings());

		this.getSimianTableDataObject().getTableHeader().add(alHeaders.get(0));
		this.getSimianTableDataObject().getTableHeader().add(alHeaders.get(1));
		addPeaksTableData();

		if (getSimianTableDataObject().getTableData().isEmpty()) {
			// adding 2 blank rows to the subset table
			getSimianTableDataObject().getTableData()
			.add(TableDataProcessor.getNewRow(getSimianTableDataObject().getLastHeader().size(),
					getSimianTableDataObject().getTableData().size()));
			getSimianTableDataObject().getTableData()
			.add(TableDataProcessor.getNewRow(getSimianTableDataObject().getLastHeader().size(),
					getSimianTableDataObject().getTableData().size()));
		}
	}

	/**
	 * Configures the default column visibility for a Merge report (before the user sets them in preferences)
	 * @param tvs
	 * 		The current column settings object
	 */
	public static void setDefaultColumnViewSettings(TableViewerColumnSettings tvs) {
		for (GRITSColumnHeader header : tvs.getHeaders()) {
			boolean bVisible = false;
			if (header.getLabel().equals(DMInterval.interval_mz.getLabel())) {
				bVisible = true;
			}
			if (header.getLabel().equals(MSGlycanAnnotationReportViewerPreference.SEL_CARTOON.getLabel())) {
				bVisible = true;
			}
			if (header.getLabel().equals(DMGlycanAnnotation.glycan_annotation_glycancartoon.getLabel())) {
				bVisible = true;
			}
			if (header.getLabel().equals(DMPeak.peak_intensity.getLabel())) {
				bVisible = true;
			}
			if (header.getLabel().equals(DMPeak.peak_relative_intensity.getLabel())) {
				bVisible = true;
			}
			if (header.getLabel().equals(DMPrecursorPeak.precursor_peak_mz.getLabel())) {
				bVisible = true;
			}
			if (!bVisible) {
				tvs.setVisColInx(header, -1);
			}
		}
	}

	/**
	 * @return the GRITSTableDataObject cast to MSGlycanAnnotationReportTableDataObject
	 * @see MassSpecTableDataObject
	 * @see GRITSTableDataObject
	 */
	private MSGlycanAnnotationReportTableDataObject getMySimianTableDataObject() {
		return (MSGlycanAnnotationReportTableDataObject) getSimianTableDataObject();
	}

	/**
	 * Creates a List of List of column header objects. The top row will contain the experiment names, the second row will contain
	 * the list of headers for each experiment.
	 * @param _columnSettings
	 * @return List of List of column header objects
	 * @throws Exception
	 */
	private ArrayList<ArrayList<GRITSColumnHeader>> getHeaderLines(TableViewerColumnSettings _columnSettings)
			throws Exception {
		ArrayList<ArrayList<GRITSColumnHeader>> alHeaders = new ArrayList<>();
		ArrayList<GRITSColumnHeader> alHeader = new ArrayList<>();
		GRITSColumnHeader colHeader = null;

		// do prefix columns first
		for (GRITSColumnHeader header : _columnSettings.keySet()) {
			if (!header.isGrouped()) {
				// null tells system to NOT create first column header
				alHeader.add(FIRST_GROUP);
			}
		}
		for (ExperimentAnnotation expAnnot : getMergeReportSettings().getExperimentList()) {
			colHeader = new GRITSColumnHeader(expAnnot.getAnnotationShortName(),
					Integer.toString(expAnnot.getAnnotationEntryId()));
			for (GRITSColumnHeader header : _columnSettings.keySet()) {
				if (header.isGrouped()) {
					alHeader.add(colHeader);
				}
			}
		}
		alHeaders.add(alHeader);
		alHeader = new ArrayList<>();
		// now add second row
		for (GRITSColumnHeader header : _columnSettings.keySet()) {
			if (!header.isGrouped()) {
				// alHeader.add(header);
				addHeaderLine(alHeader.size(), header, alHeader);
			}
		}
		MSGlycanAnnotationReportProperty msProp = (MSGlycanAnnotationReportProperty) getEntry().getProperty();
		HashMap<String, ExternalQuantFileToAlias> aliasInfo = msProp.getMsGlycanAnnotReportMetaData().getExpToExternalQuant();
		for (ExperimentAnnotation expAnnot : getMergeReportSettings().getExperimentList()) {
			ExternalQuantFileToAlias expAliasInfo = null;
			String sExpId = Integer.toString(expAnnot.getAnnotationEntryId());
			if( aliasInfo != null && aliasInfo.containsKey(sExpId))  {
				expAliasInfo = aliasInfo.get(sExpId);
			}
			for (GRITSColumnHeader header : _columnSettings.keySet()) {
				if (header.isGrouped()) {					
					// For external quant. Override the labels per experiment
					GRITSColumnHeader newHeader = header;					
					if( expAliasInfo != null && expAliasInfo.getSourceDataFileNameToAlias() != null && 
							expAliasInfo.getSourceDataFileNameToAlias().containsKey(header.getKeyValue()) ) {
						newHeader = new GRITSColumnHeader(expAliasInfo.getSourceDataFileNameToAlias().get(header.getKeyValue()).getAlias(), 
								header.getKeyValue());
					}
					addHeaderLine(alHeader.size(), newHeader, alHeader);
				}
			}
		}
		alHeaders.add(alHeader);
		return alHeaders;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#addHeaderLine(int, org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader, java.util.ArrayList)
	 */
	@Override
	protected void addHeaderLine(int iPrefColNum, GRITSColumnHeader colHeader,
			ArrayList<GRITSColumnHeader> alHeader) {
		if (colHeader.getKeyValue().equals(DMGlycanAnnotation.glycan_annotation_glycancartoon.name())) {
			this.getMySimianTableDataObject().addCartoonCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(MSGlycanAnnotationReportViewerPreference.SEL_CARTOON.getKeyValue())) {
			this.getMySimianTableDataObject().addCartoonCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMExtGlycanFeature.ext_glycan_feature_feature_id.name())) {
			this.getMySimianTableDataObject().addFeatureIdCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMExtGlycanFeature.ext_glycan_feature_sequence.name())) {
			this.getMySimianTableDataObject().addSequenceCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMExtGlycanFeature.ext_glycan_feature_annotation_id.name())) {
			this.getMySimianTableDataObject().addAnnotationIdCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMExtPeak.ext_peak_peak_id.name())) {
			this.getMySimianTableDataObject().addPeakIdCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMPeak.peak_mz.name())) {
			this.getMySimianTableDataObject().addMzCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMPeak.peak_intensity.name())) {
			this.getSimianTableDataObject().addPeakIntensityCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMPeak.peak_is_precursor.name())) {
			this.getSimianTableDataObject().addPeakIsPrecursorCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPrecursorPeak.precursor_peak_intensity.name()) ) {
			this.getSimianTableDataObject().addPrecursorIntensityCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMScan.scan_parentScan.name() ) ) {
			this.getMySimianTableDataObject().addParentNoCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMExtPeak.ext_peak_source_peak_id.name()) ) {
			this.getMySimianTableDataObject().addSourcePeakIdCol(iPrefColNum);
		}

		alHeader.add(colHeader);
	}

	/**
	 * @return a map of MS Annotations to peak id to external glycan features.
	 */
	private HashMap<Integer, HashMap<Integer, List<ExtGlycanFeature>>> getEntryToPeakIdToFeatures() {
		HashMap<Integer, HashMap<Integer, List<ExtGlycanFeature>>> htEntryToPeakIdToFeatures = new HashMap<>();
		for (ReportRow row : getMergeReportData().getRows()) {
			for (ExtGlycanFeature extFeature : row.getAnnotations()) {
				Integer iEntryId = extFeature.getExpAnotationId();
				Integer iPeakId = extFeature.getPeakId();
				HashMap<Integer, List<ExtGlycanFeature>> htPeakIdToFeatures = null;
				if (htEntryToPeakIdToFeatures.containsKey(iEntryId)) {
					htPeakIdToFeatures = htEntryToPeakIdToFeatures.get(iEntryId);
				} else {
					htPeakIdToFeatures = new HashMap<>();
					htEntryToPeakIdToFeatures.put(iEntryId, htPeakIdToFeatures);
				}
				List<ExtGlycanFeature> alFeatures = null;
				if (htPeakIdToFeatures.containsKey(iPeakId)) {
					alFeatures = htPeakIdToFeatures.get(iPeakId);
				} else {
					alFeatures = new ArrayList<>();
					htPeakIdToFeatures.put(iPeakId, alFeatures);
				}
				alFeatures.add(extFeature);

			}
		}
		return htEntryToPeakIdToFeatures;
	}

	/**
	 * Locates the closest interval external peak based on m/z
	 * @param peaks
	 * 		A list of merge extended peaks
	 * @param htPeakIdToFeatures
	 * 		A map of extended peak ids to extended glycan features
	 * @param annotId
	 * 		The ID of the MS Annotation that had the data change
	 * @param dIntervalMz
	 * 		The m/z of the Merge report interval
	 * @return the closest external peak, if found, null otherwise
	 */
	private static ExtPeak getClosestExtPeak( List<ExtPeak> peaks, HashMap<Integer, 
			List<ExtGlycanFeature>> htPeakIdToFeatures, int annotId, double dIntervalMz ) {
		ExtPeak closestExtPeak = null;
		double dMinDiff = Double.MAX_VALUE;
		for (ExtPeak extPeak : peaks) {
			if (htPeakIdToFeatures.containsKey(extPeak.getExtPeakId()) && extPeak.getExpAnnotationId() == annotId ) {
				double dDelta = Math.abs(extPeak.getMz() - dIntervalMz);
				if (dDelta < dMinDiff) {
					dMinDiff = dDelta;
					closestExtPeak = extPeak;
				}
			}
		}
		return closestExtPeak;
	}

	/**
	 * Counts the number of external peaks in the list that match to the specified MS Annotation entry. This occurs when multiple
	 * source peaks fall into the same interval. 
	 * @param peaks
	 * 		A list of merge extended peaks
	 * @param htPeakIdToFeatures
	 * 		A map of extended peak ids to extended glycan features
	 * @param annotId
	 * 		The ID of the MS Annotation that had the data change
	 * @return the number of matched peaks
	 */
	private static int getExternalPeakCnt( List<ExtPeak> peaks, HashMap<Integer, 
			List<ExtGlycanFeature>> htPeakIdToFeatures, int annotId ) {
		int iPeakCnt = 0;
		for (ExtPeak extPeak : peaks) {
			if (htPeakIdToFeatures.containsKey(extPeak.getExtPeakId()) && extPeak.getExpAnnotationId() == annotId) {
				iPeakCnt++;
			}
		}
		return iPeakCnt;
	}

	/**
	 * Determines a hashmap of Feature Annotation IDs to Glycoworkbench sequences (String) for the matching peaks based on extended peak id
	 * and the MS Annotation ID.
	 * @param peaks
	 * 		A list of merge extended peaks
	 * @param htPeakIdToFeatures
	 * 		A map of extended peak ids to extended glycan features
	 * @param annotId
	 * 		The ID of the MS Annotation that had the data change
	 * @param closestExtPeak
	 * 		The extended peak for the interval corresponding to the MS Annotation peak
	 * @return a HashMap mapping feature annotation IDs to sequence Strings
	 */
	private static HashMap<String, String> getSelectedFeatureSeqs( List<ExtPeak> peaks, 
			HashMap<Integer, List<ExtGlycanFeature>> htPeakIdToFeatures, int annotId, ExtPeak closestExtPeak ) {
		HashMap<String, String> featureToSeq = new HashMap<>();
		for (ExtPeak extPeak : peaks) {
			if (htPeakIdToFeatures.containsKey(extPeak.getExtPeakId()) && extPeak.getExpAnnotationId() == annotId) {
				List<ExtGlycanFeature> lFeatures = htPeakIdToFeatures.get(closestExtPeak.getExtPeakId());
				for (ExtGlycanFeature egf : lFeatures) {
					if( egf.getSelected()  ) {
						featureToSeq.put(egf.getStringAnnotationId(), egf.getSequenceGWB());
					}
				}
			}
		}
		return featureToSeq;
	}


	/**
	 * Locates the Merge extended glycan feature in the list of peaks that match to the specified source peak id
	 * in the message object (passed from a source GRITS table) and the specified annotation id.
	 * @param peaks
	 * 		A list of merge extended peaks
	 * @param htPeakIdToFeatures
	 * 		A map of extended peak ids to extended glycan features
	 * @param annotId
	 * 		The ID of the MS Annotation that had the data change
	 * @param message
	 * 		The message object sent from the changing MS Annotation table
	 * @return the extended glycan feature, if found, null otherwise.
	 */
	private static ExtGlycanFeature findExtGlycanFeature( List<ExtPeak> peaks, 
			HashMap<Integer, List<ExtGlycanFeature>> htPeakIdToFeatures, int annotId, 
			MSAnnotationTableDataChangedMessage message ) {
		for (ExtPeak extPeak : peaks) {
			if (! htPeakIdToFeatures.containsKey(extPeak.getExtPeakId()) ) {
				continue;
			}
			if( extPeak.getExpAnnotationId() != annotId ) {
				continue;
			}
			if( extPeak.getSourcePeakId() != message.getPeakId().intValue() ) {
				continue;
			}
			List<ExtGlycanFeature> lFeatures = htPeakIdToFeatures.get(extPeak.getExtPeakId());
			for (ExtGlycanFeature egf : lFeatures) {
				if( egf.getFeatureId().equals(message.getFeature().getId()) ) {
					return egf;
				}
			}
		}
		return null;
	}

	/**
	 * @param alRow
	 * 		A vector containing the all the data for a merge row
	 * @param iOffset
	 * 		The current column offset to keep up with which experiment values are being updated
	 * @param sExpSelectedSequence
	 * 		The concatenated strucure sequences
	 * @param htPeakIdToFeatures
	 * 		Mapping of peak ids to the features that match to it
	 * @param closestExtPeak
	 * 		The Merge extended peak representing the interval
	 */
	private void fillGRITSDataRow( GRITSListDataRow alRow, int iOffset, String sExpSelectedSequence,
			HashMap<Integer, List<ExtGlycanFeature>> htPeakIdToFeatures, ExtPeak closestExtPeak ) {

		if (closestExtPeak != null && htPeakIdToFeatures.containsKey(closestExtPeak.getExtPeakId())) {
			List<ExtGlycanFeature> lFeatures = htPeakIdToFeatures.get(closestExtPeak.getExtPeakId());

			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtPeakData(
					closestExtPeak, iOffset, alRow.getDataRow(),
					getTempPreference().getPreferenceSettings());
			Object extFeatureAnnotationId = ExtGlycanFeature.getCombinedData(lFeatures,
					ExtGlycanFeature.AnnotationId, ExtGlycanFeature.COMBO_DATA_SEPARATOR);
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMExtGlycanFeature.ext_glycan_feature_annotation_id.name(), extFeatureAnnotationId,
					iOffset, alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			Object extFeatureId = ExtGlycanFeature.getCombinedData(lFeatures, ExtGlycanFeature.FeatureId,
					ExtGlycanFeature.COMBO_DATA_SEPARATOR);
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMExtGlycanFeature.ext_glycan_feature_feature_id.name(), extFeatureId, iOffset,
					alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			Object extFeatureGlycanAnnotationId = ExtGlycanFeature.getCombinedData(lFeatures,
					ExtGlycanFeature.StringAnnotationId, ExtGlycanFeature.COMBO_DATA_SEPARATOR);
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMExtGlycanFeature.ext_glycan_feature_glycan_annotation_id.name(),
					extFeatureGlycanAnnotationId, iOffset, alRow.getDataRow(),
					getTempPreference().getPreferenceSettings());
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMExtGlycanFeature.ext_glycan_feature_sequenceGWB.name(), sExpSelectedSequence,
					iOffset, alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			Object extFeatureSequence = ExtGlycanFeature.getCombinedData(lFeatures,
					ExtGlycanFeature.Sequence, GlycanImageProvider.COMBO_SEQUENCE_SEPARATOR);
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMExtGlycanFeature.ext_glycan_feature_sequence.name(), extFeatureSequence, iOffset,
					alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			Object extFeatureSequenceFormat = ExtGlycanFeature.getCombinedData(lFeatures,
					ExtGlycanFeature.SequenceFormat, ExtGlycanFeature.COMBO_DATA_SEPARATOR);
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMExtGlycanFeature.ext_glycan_feature_sequenceFormat.name(), extFeatureSequenceFormat,
					iOffset, alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			Object extFeatureCharge = ExtGlycanFeature.getCombinedData(lFeatures, ExtGlycanFeature.Charge,
					ExtGlycanFeature.COMBO_DATA_SEPARATOR);
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMExtGlycanFeature.ext_glycan_feature_charge.name(), extFeatureCharge, iOffset,
					alRow.getDataRow(), getTempPreference().getPreferenceSettings());

			String sCartoonPng = ! sExpSelectedSequence.equals("") ? sExpSelectedSequence + ".png" : "";
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportEntryExtGlycanColumn(
					DMGlycanAnnotation.glycan_annotation_glycancartoon.name(),
					sCartoonPng, iOffset, alRow.getDataRow(),
					getTempPreference().getPreferenceSettings());

			for (CustomExtraData ced : getMergeReportData().getFeatureCustomExtraData()) {
				if (ced.getKey().equals(GlycanExtraInfo.GLYCAN_CHARGE)) {
					continue;
				}
				Object objVal = ExtGlycanFeature.getCombinedData(lFeatures, ced,
						ExtGlycanFeature.COMBO_DATA_SEPARATOR);
				MSGlycanAnnotationReportTableDataProcessorUtil
				.fillMSGlycanAnnotationReportEntryExtGlycanColumn(ced.getKey(), objVal, iOffset,
						alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			}
			for (CustomExtraData ced : getMergeReportData().getAnnotationCustomExtraData()) {
				if (ced.getKey().equals(GlycanExtraInfo.GLYCAN_CHARGE)) {
					continue;
				}
				Object objVal = ExtGlycanFeature.getCombinedData(lFeatures, ced,
						ExtGlycanFeature.COMBO_DATA_SEPARATOR);
				MSGlycanAnnotationReportTableDataProcessorUtil
				.fillMSGlycanAnnotationReportEntryExtGlycanColumn(ced.getKey(), objVal, iOffset,
						alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			}
			for (CustomExtraData ced : getMergeReportData().getPeakCustomExtraData()) {
				if (ced.getKey().equals(GlycanExtraInfo.GLYCAN_CHARGE)) {
					continue;
				}
				Object objVal = null;
				for (String sKey2 : closestExtPeak.getDoubleProp().keySet()) {
					if (ced.getKey().equals(sKey2)) {
						Double dOrigVal = closestExtPeak.getDoubleProp().get(sKey2);
						if (dOrigVal != null) {
							Double dFormatVal = new Double(ced.getDoubleFormat().format(dOrigVal));
							objVal = dFormatVal;
						} else {
							objVal = null;
						}
						break;
					}
				}
				if (objVal == null) {
					for (String sKey2 : closestExtPeak.getIntegerProp().keySet()) {
						if (ced.getKey().equals(sKey2)) {
							objVal = closestExtPeak.getIntegerProp().get(sKey2);
							break;
						}
					}
				}
				MSGlycanAnnotationReportTableDataProcessorUtil
				.fillMSGlycanAnnotationReportEntryExtGlycanColumn(ced.getKey(), objVal, iOffset,
						alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			}
		}
	}

	/**
	 * Uses the parameters in the message to update the selected structures for the specified experiment, row and selection value.
	 * 
	 * @param sExpAnnoId
	 * 		The String experiment annotation Id. This is equivalent to the entry Id so is unaffected by name change of the source entry.
	 * @param iRowInx
	 * 		The 0-based index in the merge table (probably determined via call to findMatchingRow())
	 * @param message
	 * 		The structure from the source MS Annotation Table with data to locate and update the corresponding data in the report
	 * @param bCurSelected
	 * 		Whether or not the specified structure should be selected or not
	 * @return true if selection value changed, false otherwise
	 */
	public boolean toggleFeatureSelection( String sExpAnnotId, int iRowInx, 
			MSAnnotationTableDataChangedMessage message, boolean bCurSelected ) {
		HashMap<Integer, HashMap<Integer, List<ExtGlycanFeature>>> htEntryToPeakIdToFeatures = getEntryToPeakIdToFeatures();
		ReportRow reportRow = getMergeReportData().getRows().get(iRowInx);
		GRITSListDataRow natRow = getSimianTableDataObject().getTableData().get(iRowInx);

		Interval interval = reportRow.getInterval();
		List<ExtPeak> peaks = interval.getPeaks();
		int iOffset = 0;
		HashMap<String, String> hmSelectedFeatures = new HashMap<>();
		int iColCnt = 0;
		// first locate the first grouped cell (the first ungrouped cells are for the interval
		// we have to find the duplicated cell headers for the individual experiments)
		for (GRITSColumnHeader header : getTempPreference().getPreferenceSettings().keySet()) {
			if (header.isGrouped() ) {
				break;
			}
			iColCnt++;
		}
		boolean bChanged = false;
		for (ExperimentAnnotation expAnnot : getMergeReportSettings().getExperimentList()) {
			Object oPeakId = null;
			for (GRITSColumnHeader header : getTempPreference().getPreferenceSettings().keySet()) {
				if( ! header.isGrouped() ) {
					continue; // skip the first ungrouped columns
				}
				if( header.getKeyValue() == DMExtPeak.ext_peak_source_peak_id.name()) {						
					oPeakId = natRow.getDataRow().get(iColCnt);
				}
				iColCnt++;
			}
			HashMap<Integer, List<ExtGlycanFeature>> htPeakIdToFeatures = htEntryToPeakIdToFeatures.get(expAnnot.getAnnotationEntryId());
			ExtPeak closestExtPeak = MSGlycanAnnotationReportTableDataProcessor.getClosestExtPeak(peaks, htPeakIdToFeatures, expAnnot.getAnnotationEntryId(), interval.getMz());
			if( closestExtPeak != null && htPeakIdToFeatures.containsKey(closestExtPeak.getExtPeakId()) ) {
				boolean bChangedThis = false;
				if( expAnnot.getAnnotationEntryId().toString().equals(sExpAnnotId) ) {
					ExtGlycanFeature extGlyFeature = findExtGlycanFeature(peaks, htPeakIdToFeatures, expAnnot.getAnnotationEntryId(), message);
					if( extGlyFeature == null ) { // need to add it!
						logger.debug("Feature not in report: " + message.getFeature().getId() + ". Adding it.");
						ExtGlycanFeature extGlycanFeature = ExtractRequiredData.getNewExtGlycanFeature(closestExtPeak, (GlycanAnnotation) message.getAnnotation(), message.getFeature());
						List<ExtGlycanFeature> lFeatures = htPeakIdToFeatures.get(closestExtPeak.getExtPeakId());
						lFeatures.add(extGlycanFeature);
						extGlycanFeature.setSelected(bCurSelected);
						reportRow.getAnnotations().add(extGlycanFeature);
						bChangedThis = true;
					} else {
						if( (extGlyFeature.getSelected() && ! bCurSelected) || ( ! extGlyFeature.getSelected() && bCurSelected ) ) {
							extGlyFeature.setSelected(bCurSelected);		
							bChangedThis = true;
						}
					}
				}
				HashMap<String, String> hmSelSeqs = MSGlycanAnnotationReportTableDataProcessor.getSelectedFeatureSeqs(peaks, htPeakIdToFeatures, expAnnot.getAnnotationEntryId(), closestExtPeak);
				hmSelectedFeatures.putAll(hmSelSeqs);
				if( bChangedThis ) {
					StringBuilder sbExpSelectedSequence = new StringBuilder();
					MSGlycanAnnotationReportTableDataProcessor.appendSequences(sbExpSelectedSequence, hmSelSeqs);
					if( ! sbExpSelectedSequence.toString().equals("")) {
						if( MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(sbExpSelectedSequence.toString()) == null ) {
							// an image doesn't exist for this combo image!
							try {
								MSGlycanAnnotationTableDataObject.glycanImageProvider.addMergeImageToProvider(sbExpSelectedSequence.toString(), sbExpSelectedSequence.toString() + ".png");
							} catch (ImageCreationException e) {
								logger.error(e.getMessage(), e);
							}
						}
					}
					fillGRITSDataRow(natRow, iOffset, sbExpSelectedSequence.toString(), htPeakIdToFeatures, closestExtPeak);
					bChanged = bChangedThis;
				}
			}
			iOffset += getLastVisibleCol();
		}
		if (bChanged) {
			StringBuilder sbSelectedSequence = new StringBuilder();
			MSGlycanAnnotationReportTableDataProcessor.appendSequences(sbSelectedSequence, hmSelectedFeatures);
			MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportRowPrefix(interval,
					sbSelectedSequence.toString(), natRow.getDataRow(),
					getTempPreference().getPreferenceSettings());
			if( ! sbSelectedSequence.toString().equals("")) {
				if( MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(sbSelectedSequence.toString()) == null ) {
					// an image doesn't exist for this combo image!
					try {
						MSGlycanAnnotationTableDataObject.glycanImageProvider.addMergeImageToProvider(sbSelectedSequence.toString(), sbSelectedSequence.toString() + ".png");
					} catch (ImageCreationException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}		

		return bChanged;
	}

	/**
	 * Used to automatically locate the row in the table for the specified experiment (by Id) and peak id (source peak id)
	 * @param sExpAnnoId
	 * 		The String experiment annotation Id. This is equivalent to the entry Id so is unaffected by name change of the source entry.
	 * @param iPeakId
	 * 		The peak id from the original MS annotation entry
	 * @return
	 */
	public int findMatchingRow(String sExpAnnoId, Integer iPeakId) {
		int iRowCnt = 0;
		for (ReportRow row : getMergeReportData().getRows()) {
			GRITSListDataRow alRow = getSimianTableDataObject().getTableData().get(iRowCnt);
			int iColCnt = 0;
			// first locate the first grouped cell (the first ungrouped cells are for the interval
			// we have to find the duplicated cell headers for the individual experiments)
			for (GRITSColumnHeader header : getTempPreference().getPreferenceSettings().keySet()) {
				if (header.isGrouped() ) {
					break;
				}
				iColCnt++;
			}

			// now find the experiment and peak id that match the parameters
			for (ExperimentAnnotation expAnnot : getMergeReportSettings().getExperimentList()) {
				if( ! expAnnot.getAnnotationEntryId().toString().equals(sExpAnnoId) ) {		
					iColCnt += getLastVisibleCol(); // not found, add the # of columns for each exp
					continue;
				}
				Object oPeakId = null;
				for (GRITSColumnHeader header : getTempPreference().getPreferenceSettings().keySet()) {
					if( ! header.isGrouped() ) {
						continue; // continue w/out incrementing column, the non-grouped count is already included
					}
					if (header.isGrouped() ) {
						if( header.getKeyValue() == DMExtPeak.ext_peak_source_peak_id.name()) {						
							oPeakId = alRow.getDataRow().get(iColCnt);
							break;
						}
					}
					iColCnt++;
				}
				if( oPeakId == null || ! (oPeakId instanceof Integer) ) {
					logger.error("Peak id not found for key: " + iPeakId + " in experiment: " + expAnnot.getAnnotationDisplayName());
					continue;
				}
				if( ! iPeakId.toString().equals(oPeakId.toString()) ) {
					continue;
				}	
				return iRowCnt;
			}
			iRowCnt++;
		}		

		logger.error("Unable to locate desired row to update the data.");
		return -1;
	}


	/**
	 * Adds the sequence in the list to the sequence builder.
	 * @param sbSeqBuilder
	 * 		A String that will represent the concatenation of the sequence for all structures
	 * @param hmSelSeqs
	 * 		A map of the feature annotation string IDs to the sequences
	 */
	private static void appendSequences( StringBuilder sbSeqBuilder, HashMap<String, String> hmSelSeqs) {
		Set<String> s = hmSelSeqs.keySet();
		for( String sFeatId : s ) {							
			String selSeq = hmSelSeqs.get(sFeatId);
			if (!sbSeqBuilder.toString().contains(selSeq)) {
				if (!sbSeqBuilder.toString().equals(""))
					sbSeqBuilder.append(GlycanImageProvider.COMBO_SEQUENCE_SEPARATOR);
				sbSeqBuilder.append(selSeq);
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#addPeaksTableData()
	 */
	@Override
	protected void addPeaksTableData() {
		try {
			HashMap<Integer, HashMap<Integer, List<ExtGlycanFeature>>> htEntryToPeakIdToFeatures = getEntryToPeakIdToFeatures();
			int iCnt = 1;
			for (ReportRow row : getMergeReportData().getRows()) {
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage(
						"Building table. Row: " + iCnt + " of " + getMergeReportData().getRows().size());
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressValue(iCnt++);
				if (bCancel) {
					setSimianTableDataObject(null);
					return;
				}
				Interval interval = row.getInterval();
				List<ExtPeak> peaks = interval.getPeaks();
				int iOffset = 0;
				GRITSListDataRow alRow = TableDataProcessor.getNewRow(
						getSimianTableDataObject().getLastHeader().size(),
						getSimianTableDataObject().getTableData().size());
				HashMap<String, String> hmSelectedFeatures = new HashMap<>();
				Map<String, String> intervalErrors = new HashMap<>();
				for (ExperimentAnnotation expAnnot : getMergeReportSettings().getExperimentList()) {
					if (!htEntryToPeakIdToFeatures.containsKey(expAnnot.getAnnotationEntryId())) {
						iOffset += getLastVisibleCol();  // is this an error condition??
						continue;
					}
					HashMap<Integer, List<ExtGlycanFeature>> htPeakIdToFeatures = htEntryToPeakIdToFeatures.get(expAnnot.getAnnotationEntryId());
					ExtPeak closestExtPeak = MSGlycanAnnotationReportTableDataProcessor.getClosestExtPeak(peaks, htPeakIdToFeatures, expAnnot.getAnnotationEntryId(), interval.getMz());
					if( closestExtPeak != null && htPeakIdToFeatures.containsKey(closestExtPeak.getExtPeakId()) ) {
						int iPeakCnt = MSGlycanAnnotationReportTableDataProcessor.getExternalPeakCnt(peaks, htPeakIdToFeatures, expAnnot.getAnnotationEntryId());
						List<ExtGlycanFeature> egfs = htPeakIdToFeatures.get(closestExtPeak.getExtPeakId());
						if( iPeakCnt > 1 ) {
							String closestMz = MassSpecTableDataProcessorUtil.formatDec4.format(closestExtPeak.getMz());
							intervalErrors.put(expAnnot.getAnnotationDisplayName(), closestMz);
						}
						HashMap<String, String> hmSelSeqs = MSGlycanAnnotationReportTableDataProcessor.getSelectedFeatureSeqs(peaks, htPeakIdToFeatures, expAnnot.getAnnotationEntryId(), closestExtPeak);
						StringBuilder sbExpSelectedSequence = new StringBuilder();
						MSGlycanAnnotationReportTableDataProcessor.appendSequences(sbExpSelectedSequence, hmSelSeqs);
						fillGRITSDataRow(alRow, iOffset, sbExpSelectedSequence.toString(), htPeakIdToFeatures, closestExtPeak);
						hmSelectedFeatures.putAll(hmSelSeqs);
					}
					iOffset += getLastVisibleCol();
				}

				if( ! intervalErrors.isEmpty() ) {
					String intMz = MassSpecTableDataProcessorUtil.formatDec4.format(interval.getMz());
					this.progressBarDialog.getMinorProgressBarListener(0).setError("Interval: " + intMz);
					for (ExperimentAnnotation expAnnot : getMergeReportSettings().getExperimentList()) {
						if( intervalErrors.containsKey(expAnnot.getAnnotationDisplayName())) {
							this.progressBarDialog.getMinorProgressBarListener(0).setError("     " + expAnnot.getAnnotationShortName() + ": " + 
									intervalErrors.get(expAnnot.getAnnotationDisplayName()) + " selected.");
						}
					}
				}
				StringBuilder sbSelectedSequence = new StringBuilder();
				if( ! hmSelectedFeatures.isEmpty() ) {
					MSGlycanAnnotationReportTableDataProcessor.appendSequences(sbSelectedSequence, hmSelectedFeatures);
				}
				MSGlycanAnnotationReportTableDataProcessorUtil.fillMSGlycanAnnotationReportRowPrefix(interval,
						sbSelectedSequence.toString(), alRow.getDataRow(),
						getTempPreference().getPreferenceSettings());


				getSimianTableDataObject().getTableData().add(alRow);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#addAnnotationColumns(org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings)
	 */
	@Override
	protected void addAnnotationColumns(TableViewerColumnSettings _settings) {
		int iNumCols = MSGlycanAnnotationReportTableDataProcessorUtil
				.fillMSGlycanAnnotationReportColumnSettingsGlycanAnnotation(_settings);
		if (getMergeReportData().getFeatureCustomExtraData() != null) {
			iNumCols += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(_settings,
					getMergeReportData().getFeatureCustomExtraData(), _settings.getUnrecognizedHeaders());
		}
		if (getMergeReportData().getAnnotationCustomExtraData() != null) {
			iNumCols += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(_settings,
					getMergeReportData().getAnnotationCustomExtraData(), _settings.getUnrecognizedHeaders());
		}
		if (getMergeReportData().getPeakCustomExtraData() != null) {
			iNumCols += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(_settings,
					getMergeReportData().getPeakCustomExtraData(), _settings.getUnrecognizedHeaders());
		}
		setLastVisibleCol(iNumCols);
	}
}