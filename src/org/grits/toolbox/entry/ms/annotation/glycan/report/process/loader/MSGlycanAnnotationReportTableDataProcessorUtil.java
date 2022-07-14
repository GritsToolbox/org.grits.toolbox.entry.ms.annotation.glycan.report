package org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;

import org.grits.toolbox.datamodel.ms.annotation.glycan.report.preference.MSGlycanAnnotationReportViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.dmtranslate.DMExtGlycanFeature;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.dmtranslate.DMExtPeak;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.dmtranslate.DMInterval;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanAnnotation;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPrecursorPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMScan;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessorUtil;
import org.grits.toolbox.merge.om.data.ExtPeak;
import org.grits.toolbox.merge.om.data.Interval;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;

public class MSGlycanAnnotationReportTableDataProcessorUtil
{
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportTableDataProcessorUtil.class);

	public static int fillMSGlycanAnnotationReportColumnSettingsGlycanAnnotation( TableViewerColumnSettings _columnSettings ) {		
		GRITSColumnHeader header = new GRITSColumnHeader(DMInterval.interval_mz.getLabel(), DMInterval.interval_mz.name());
		header.setIsGrouped(false);
		_columnSettings.addColumn( header );
		header = new GRITSColumnHeader(MSGlycanAnnotationReportViewerPreference.SEL_CARTOON.getLabel(), 
				MSGlycanAnnotationReportViewerPreference.SEL_CARTOON.getKeyValue());
		header.setIsGrouped(false);
		_columnSettings.addColumn( header );
		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_glycancartoon.getLabel(), DMGlycanAnnotation.glycan_annotation_glycancartoon.name());
		_columnSettings.addColumn( DMPeak.peak_intensity.getLabel(), DMPeak.peak_intensity.name() ); 
		_columnSettings.addColumn( DMPeak.peak_relative_intensity.getLabel(), DMPeak.peak_relative_intensity.name() ); 
		_columnSettings.addColumn( DMPrecursorPeak.precursor_peak_mz.getLabel(), DMPrecursorPeak.precursor_peak_mz.name() );

		_columnSettings.addColumn( DMExtPeak.ext_peak_source_peak_id.getLabel(), DMExtPeak.ext_peak_source_peak_id.name() );
		_columnSettings.addColumn( DMExtPeak.ext_peak_peak_id.getLabel(), DMExtPeak.ext_peak_peak_id.name() );
		_columnSettings.addColumn( DMPeak.peak_mz.getLabel(), DMPeak.peak_mz.name() );
		_columnSettings.addColumn( DMPrecursorPeak.precursor_peak_intensity.getLabel(), DMPrecursorPeak.precursor_peak_intensity.name() ); 
		_columnSettings.addColumn( DMPrecursorPeak.precursor_peak_charge.getLabel(), DMPrecursorPeak.precursor_peak_charge.name() );
		_columnSettings.addColumn( DMScan.scan_parentScan.getLabel(), DMScan.scan_parentScan.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_feature_id.getLabel(), DMExtGlycanFeature.ext_glycan_feature_feature_id.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_annotation_id.getLabel(), DMExtGlycanFeature.ext_glycan_feature_annotation_id.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_glycan_annotation_id.getLabel(), DMExtGlycanFeature.ext_glycan_feature_glycan_annotation_id.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_sequenceGWB.getLabel(), DMExtGlycanFeature.ext_glycan_feature_sequenceGWB.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_sequence.getLabel(), DMExtGlycanFeature.ext_glycan_feature_sequence.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_sequenceFormat.getLabel(), DMExtGlycanFeature.ext_glycan_feature_sequenceFormat.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_charge.getLabel(), DMExtGlycanFeature.ext_glycan_feature_charge.name() );
		_columnSettings.addColumn( DMExtGlycanFeature.ext_glycan_feature_score.getLabel(), DMExtGlycanFeature.ext_glycan_feature_score.name() );
		return 18;
	}

	public static void fillMSGlycanAnnotationReportRowPrefix(Interval _interval, String _sMergeFeatureId,
			ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings )
	{
		MassSpecTableDataProcessorUtil.setRowValue( _columnSettings.getColumnPosition( DMInterval.interval_mz.name() ), 
				new Double(MassSpecTableDataProcessorUtil.formatDec4.format(_interval.getMz())), _tableRow);    	
		String sCartoonPng = ! _sMergeFeatureId.equals("") ? _sMergeFeatureId + ".png" : "";
		MassSpecTableDataProcessorUtil.setRowValue( _columnSettings.getColumnPosition( MSGlycanAnnotationReportViewerPreference.SEL_CARTOON.getKeyValue() ), 
				sCartoonPng, _tableRow);  
	}

	public static void fillMSGlycanAnnotationReportEntryExtPeakData(ExtPeak _extPeak, int _iOffset, ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings )
	{
		MassSpecTableDataProcessorUtil.setRowValue( _iOffset +
				_columnSettings.getColumnPosition( DMExtPeak.ext_peak_source_peak_id.name() ), 
				_extPeak.getSourcePeakId(), _tableRow);    	
		MassSpecTableDataProcessorUtil.setRowValue( _iOffset +
				_columnSettings.getColumnPosition( DMExtPeak.ext_peak_peak_id.name() ), 
				_extPeak.getExtPeakId(), _tableRow);    	
		MassSpecTableDataProcessorUtil.setRowValue(_iOffset +
				_columnSettings.getColumnPosition(DMPeak.peak_mz.name()), 
				new Double(MassSpecTableDataProcessorUtil.formatDec4.format(_extPeak.getMz())), _tableRow);
		if( _extPeak.getIntensity() != null )
			MassSpecTableDataProcessorUtil.setRowValue(_iOffset +
					_columnSettings.getColumnPosition(DMPeak.peak_intensity.name()), 
					new Double(MassSpecTableDataProcessorUtil.formatDec1.format(_extPeak.getIntensity())), _tableRow);
		if( _extPeak.getRelativeIntensity() != null )
			MassSpecTableDataProcessorUtil.setRowValue(_iOffset +
					_columnSettings.getColumnPosition(DMPeak.peak_relative_intensity.name()), 
					new Double(MassSpecTableDataProcessorUtil.formatDec2.format(_extPeak.getRelativeIntensity())), _tableRow);

		// the precursor info
		if( _extPeak.getPrecursorMz() != null ) {
			MassSpecTableDataProcessorUtil.setRowValue(_iOffset +
					_columnSettings.getColumnPosition(DMPrecursorPeak.precursor_peak_mz.name()), 
					new Double(MassSpecTableDataProcessorUtil.formatDec4.format(_extPeak.getPrecursorMz())), _tableRow);
		}
		if( _extPeak.getPrecursorIntensity() != null ) {
			MassSpecTableDataProcessorUtil.setRowValue(_iOffset +
					_columnSettings.getColumnPosition(DMPrecursorPeak.precursor_peak_intensity.name()), 
					new Double(MassSpecTableDataProcessorUtil.formatDec1.format(_extPeak.getPrecursorIntensity())), _tableRow);
		}
		if( _extPeak.getPrecursorCharge() != null ) {
			MassSpecTableDataProcessorUtil.setRowValue(_iOffset +
					_columnSettings.getColumnPosition(DMPrecursorPeak.precursor_peak_charge.name()), 
					_extPeak.getPrecursorCharge(), _tableRow);
		}
		if( _extPeak.getScanId() != null ) {
			MassSpecTableDataProcessorUtil.setRowValue(_iOffset +
					_columnSettings.getColumnPosition(DMScan.scan_parentScan.name()), 
					_extPeak.getScanId(), _tableRow);
		}

	}

	public static void fillMSGlycanAnnotationReportEntryExtGlycanColumn( String _sColumnKey, Object _objVal, 
			int _iOffset, ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings ) {
		MassSpecTableDataProcessorUtil.setRowValue( _iOffset + _columnSettings.getColumnPosition( _sColumnKey ), _objVal, _tableRow);		
	}


	public static void updateMSGlycanAnnotationData(GlycanAnnotation a_annotation, ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings, int _iMSLevel )
	{
		//    	MSAnnotationTableDataProcessorUtil.updateAnnotationData(a_annotation, _tableRow, _columnSettings);
		if ( a_annotation != null )
		{
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glycanId.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glycanId.name()) ).equals( a_annotation.getStringId()) ) {
				a_annotation.setGlycanId( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glycanId.name()) ));
			}
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glytoucanid.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glytoucanid.name()) )
					.equals( a_annotation.getGlytoucanId()) ) {
				a_annotation.setGlytoucanId( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glytoucanid.name()) ));
			}
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name()) ).equals( a_annotation.getPerDerivatisationType()) ) {
				a_annotation.setPerDerivatisationType( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name()) ));
			}
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_composition.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_composition.name()) ).equals( a_annotation.getComposition()) ) {
				a_annotation.setComposition( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_composition.name()) ));
			}
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_sequenceGWB.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_sequenceGWB.name()) ).equals( a_annotation.getSequenceGWB()) ) {
				a_annotation.setSequenceGWB( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_sequenceGWB.name()) ));
			}
		}
	}  

	public static String getDisplayNameByEntryName( String _sEntryId, Entry root ) {
		List<Entry> alStack = new ArrayList<>();
		alStack.add(root);
		while( ! alStack.isEmpty() ) {
			Entry curEntry = alStack.remove(0);
			if( curEntry.getProperty() instanceof MSGlycanAnnotationProperty ) {
				if( ((MSGlycanAnnotationProperty) curEntry.getProperty()).getMSAnnotationMetaData().getAnnotationId().equals(_sEntryId) ) {
					Entry msEntry = curEntry.getParent();
					Entry sampleEntry = msEntry.getParent();
					String sName = sampleEntry.getDisplayName() + "." +
							msEntry.getDisplayName() + "." + 
							curEntry.getDisplayName();

					return sName;
				}
			}
			List<Entry> children = curEntry.getChildren();
			for(Entry child : children) {
				alStack.add(child);
			}
		}
		return null;		
	}

	public static String getDisplayNameByEntryName( String _sEntryId ) {
		Entry root = PropertyHandler.getDataModel().getRoot();
		return getDisplayNameByEntryName(_sEntryId, root);

	}
}
