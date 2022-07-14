package org.grits.toolbox.entry.ms.annotation.glycan.report.views;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportMetaData;
import org.grits.toolbox.merge.om.data.ExperimentAnnotation;
import org.grits.toolbox.merge.om.data.MergeSettings;
import org.grits.toolbox.ms.om.data.Method;

/**
 * SimGlycan Editor
 * @author dbrentw
 *
 */
public class MSGlycanAnnotationReportPropertyView extends EntryEditorPart {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportPropertyView.class);
	private Composite parent = null;
	private Composite container = null;
	protected Method msAnnotationMethod = null;
	private MergeSettings mergeSettings = null;

	private Label massIntervalLabel = null;
	private Text massIntervalText = null;
	
	private Label massIntervalTypeLabel = null;
	
	private Label descriptionLabel = null;
	private Text descriptionText = null;
	
	private Label creationDateLabel = null;
	private Text creationDateText = null;
	
	private Label updateDateLabel = null;
	private Text updateDateText = null;
	
	private MPart part;
	
	@Inject
	public MSGlycanAnnotationReportPropertyView(Entry entry) {
		this.entry = entry;
	}
	
	@PostConstruct 
	public void postConstruct(MPart part) {
		this.part = part;
	}
		
	@Override
	public void createPartControl(Composite parent) {
		
		ModifyListener modListener = new ModifyListener()
		{
			public void modifyText(ModifyEvent event) 
			{
				setDirty(true);
			}
		};

		parent.setLayout(new FillLayout());
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		final Composite c = new Composite(sc, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.verticalSpacing = 10;
		layout.numColumns = 6;
		c.setLayout(layout);

		this.parent = parent;
		this.container = c;

		MSGlycanAnnotationReportProperty msgrp = (MSGlycanAnnotationReportProperty) this.entry.getProperty();
		part.setLabel(this.entry.getDisplayName() + " Properties" );
		try {
			addDescriptionLine(msgrp, modListener);		
			addIntervalControls();
			addDateControls(msgrp);
			addAnnotationEntries();
		} catch( Exception e) {
			logger.error("Error adding property elements.", e);
		}
		sc.setContent(c);
		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void addDescriptionLine(MSGlycanAnnotationReportProperty pp, ModifyListener modListener) {
		descriptionLabel = new Label(getContainer(), SWT.NONE);
		descriptionLabel.setText("Description");
		GridData descriptionLabelData = new GridData();
		descriptionLabelData.horizontalSpan = 4;
		descriptionLabel.setLayoutData(descriptionLabelData);

		GridData descriptionTextData = new GridData();
		descriptionTextData.horizontalAlignment = GridData.FILL;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionText = new Text(getContainer(),SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		descriptionText.setText(pp.getMsGlycanAnnotReportMetaData().getDescription());
		GridData gdTxtGridData = new GridData();
		gdTxtGridData.grabExcessHorizontalSpace = true;
		gdTxtGridData.horizontalAlignment = GridData.FILL;
		gdTxtGridData.horizontalSpan = 2;
		gdTxtGridData.heightHint = 100;
		descriptionText.setLayoutData(gdTxtGridData);
	}
	
	private void addDateControls(MSGlycanAnnotationReportProperty pp) {
		if( this.mergeSettings != null ) {
			String sCreateDate = pp.getMsGlycanAnnotReportMetaData().getCreationDate() == null ? "" : pp.getMsGlycanAnnotReportMetaData().getCreationDate().toString();			
			this.creationDateLabel = new Label(getContainer(), SWT.NONE);
			this.creationDateLabel.setText("Report Creation Date");
			GridData creationDateLabelData = new GridData();
			creationDateLabelData.horizontalSpan = 4;
			creationDateLabel.setLayoutData(creationDateLabelData);

			creationDateText = new Text(getContainer(), SWT.BORDER); 
			creationDateText.setText(sCreateDate);
			creationDateText.setEnabled(false);
			
			GridData creationDateTextData = new GridData();
			creationDateTextData.grabExcessHorizontalSpace = true;
			creationDateTextData.horizontalAlignment = GridData.FILL;
			creationDateTextData.horizontalSpan = 2;
			creationDateText.setLayoutData(creationDateTextData);
			
			String sUpdateDate = pp.getMsGlycanAnnotReportMetaData().getUpdateDate() == null ? "" : pp.getMsGlycanAnnotReportMetaData().getUpdateDate().toString();
			this.updateDateLabel = new Label(getContainer(), SWT.NONE);
			this.updateDateLabel.setText("Report Refresh Date");
			GridData updateDateLabelData = new GridData();
			updateDateLabelData.horizontalSpan = 4;
			updateDateLabel.setLayoutData(updateDateLabelData);

			updateDateText = new Text(getContainer(), SWT.BORDER); 
			updateDateText.setText(sUpdateDate);
			updateDateText.setEnabled(false);
			
			GridData updateDateTextData = new GridData();
			updateDateTextData.grabExcessHorizontalSpace = true;
			updateDateTextData.horizontalAlignment = GridData.FILL;
			updateDateTextData.horizontalSpan = 2;
			updateDateText.setLayoutData(updateDateTextData);
			
			
		}
	}
	
	private void addIntervalControls() {
		if( this.mergeSettings != null ) {
			double dMassTolerance = mergeSettings.getTolerance();
			String sTolType = mergeSettings.getToleranceType() != null ? mergeSettings.getToleranceType() : "";
			
			this.massIntervalLabel = new Label(getContainer(), SWT.NONE);
			this.massIntervalLabel.setText("Mass Interval for Merge");
			GridData massIntervalLabelData = new GridData();
			massIntervalLabelData.horizontalSpan = 3;
			massIntervalLabel.setLayoutData(massIntervalLabelData);

			this.massIntervalTypeLabel = new Label(getContainer(), SWT.NONE);
			this.massIntervalTypeLabel.setText("(" + sTolType + ")");
			GridData massIntervalTypeData = new GridData();
			massIntervalTypeData.horizontalSpan = 1;
			massIntervalTypeLabel.setLayoutData(massIntervalTypeData);

			massIntervalText = new Text(getContainer(), SWT.BORDER); 
			massIntervalText.setText(Double.toString(dMassTolerance));
			massIntervalText.setEnabled(false);
			
			GridData massIntervalTextData = new GridData();
			massIntervalTextData.grabExcessHorizontalSpace = true;
			massIntervalTextData.horizontalAlignment = GridData.FILL;
			massIntervalTextData.horizontalSpan = 2;
			massIntervalText.setLayoutData(massIntervalTextData);
			
		}
	}
	
	private void addAnnotationEntries() {
		if( this.mergeSettings != null && this.mergeSettings.getExperimentList() != null ) {
			Label lblIons = new Label(getContainer(), SWT.LEFT);
			lblIons.setText("MS Glycan Annotations in this merge report: ");
			GridData gdIonsData = new GridData();
			gdIonsData.horizontalSpan = 6;
			lblIons.setLayoutData(gdIonsData);

			for( int i = 0; i < this.mergeSettings.getExperimentList().size(); i++ ) {
				ExperimentAnnotation expAnnot = this.mergeSettings.getExperimentList().get(i);
				Label lSpacer = new Label(getContainer(), SWT.NONE);	// column 1	
				lSpacer.setText("     ");
				Label lblAnnotation = new Label(getContainer(), SWT.NONE);	// column 2-6	
				lblAnnotation.setText("MS Glycan Annotation " + (i+1));
				GridData gdIon = new GridData();
				gdIon.horizontalSpan = 5;
				lblAnnotation.setLayoutData(gdIon);

				lSpacer = new Label(getContainer(), SWT.NONE);	// column 1	
				lSpacer.setText("     ");
				lSpacer = new Label(getContainer(), SWT.NONE);	// column 2	
				lSpacer.setText("     ");
				Label lblResultName = new Label(getContainer(), SWT.LEFT); // columns 3-5
				lblResultName.setText("Result name");
				GridData gdResultNameData = new GridData();
				gdResultNameData.horizontalSpan = 2;
				lblResultName.setLayoutData(gdResultNameData);

				Text txtResultName = new Text(getContainer(), SWT.BORDER); // column 6
				String sText = expAnnot.getAnnotationDisplayName()  != null ? expAnnot.getAnnotationDisplayName()  : "";
				txtResultName.setText( sText );
				txtResultName.setEditable(false);
				GridData gdTxtGridData1 = new GridData();
				gdTxtGridData1.grabExcessHorizontalSpace = true;
				gdTxtGridData1.horizontalAlignment = GridData.FILL;
				gdTxtGridData1.horizontalSpan = 2;
				txtResultName.setLayoutData(gdTxtGridData1);

				lSpacer = new Label(getContainer(), SWT.NONE);	// column 1	
				lSpacer.setText("     ");
				lSpacer = new Label(getContainer(), SWT.NONE);	// column 2	
				lSpacer.setText("     ");
				Label lblAlias = new Label(getContainer(), SWT.LEFT); // columns 3-5
				lblAlias.setText("Alias");
				GridData gdAliasNameData = new GridData();
				gdAliasNameData.horizontalSpan = 2;
				lblAlias.setLayoutData(gdAliasNameData);

				Text txtAlias = new Text(getContainer(), SWT.BORDER); // column 6
				sText = expAnnot.getAnnotationShortName()  != null ? expAnnot.getAnnotationShortName()  : "";
				txtAlias.setText( sText );
				txtAlias.setEditable(false);
				GridData gdTxtGridData2 = new GridData();
				gdTxtGridData2.grabExcessHorizontalSpace = true;
				gdTxtGridData2.horizontalAlignment = GridData.FILL;
				gdTxtGridData2.horizontalSpan = 2;
				txtAlias.setLayoutData(gdTxtGridData2);

				lSpacer = new Label(getContainer(), SWT.NONE);	// column 1	
				lSpacer.setText("     ");
				lSpacer = new Label(getContainer(), SWT.NONE);	// column 2	
				lSpacer.setText("     ");
				Label lblArchive = new Label(getContainer(), SWT.LEFT); // columns 3-5
				lblArchive.setText("Result Archive");
				GridData gdArchiveData = new GridData();
				gdArchiveData.horizontalSpan = 2;
				lblArchive.setLayoutData(gdArchiveData);

				Text txtArchive = new Text(getContainer(), SWT.BORDER); // column 6
				sText = expAnnot.getAnnotationFileArchive() != null ? expAnnot.getAnnotationFileArchive()  : "";
				txtArchive.setText( sText );
				txtArchive.setEditable(false);
				GridData gdTxtGridData3 = new GridData();
				gdTxtGridData3.grabExcessHorizontalSpace = true;
				gdTxtGridData3.horizontalAlignment = GridData.FILL;
				gdTxtGridData3.horizontalSpan = 2;
				txtArchive.setLayoutData(gdTxtGridData3);

				lSpacer = new Label(getContainer(), SWT.NONE);	// column 1	
				lSpacer.setText("     ");
				lSpacer = new Label(getContainer(), SWT.NONE);	// column 2	
				lSpacer.setText("     ");
				Label lblEntry = new Label(getContainer(), SWT.LEFT); // columns 3-5
				lblEntry.setText("Entry ID");
				GridData gdEntryData = new GridData();
				gdEntryData.horizontalSpan = 2;
				lblEntry.setLayoutData(gdEntryData);

				Text txtEntry = new Text(getContainer(), SWT.BORDER); // column 6
				sText = expAnnot.getAnnotationEntryId() != null ? Integer.toString(expAnnot.getAnnotationEntryId())  : "";
				txtEntry.setText( sText );
				txtEntry.setEditable(false);
				GridData gdTxtGridData4 = new GridData();
				gdTxtGridData4.grabExcessHorizontalSpace = true;
				gdTxtGridData4.horizontalAlignment = GridData.FILL;
				gdTxtGridData4.horizontalSpan = 2;
				txtEntry.setLayoutData(gdTxtGridData4);
				
			}
		}
	}

	
	protected void updateProjectProperty() {
		Entry projectEntry = DataModelSearch.findParentByType(this.entry, ProjectProperty.TYPE);
		MSGlycanAnnotationReportProperty pp = (MSGlycanAnnotationReportProperty)this.entry.getProperty();
		pp.getMsGlycanAnnotReportMetaData().setDescription(descriptionText.getText());
		String settingsFile = pp.getFullyQualifiedMetaDataFileName(projectEntry);
		MSGlycanAnnotationReportProperty.marshallSettingsFile(settingsFile, pp.getMsGlycanAnnotReportMetaData());
	}
	
	public void setMergeSettings(MergeSettings mergeSettings) {
		this.mergeSettings = mergeSettings;
	}
	
	public MergeSettings getMergeSettings() {
		return mergeSettings;
	}
			
	@Focus
	public void setFocus() {
		descriptionLabel.setFocus();
	}

	@Override
	protected Composite getParent() {
		return this.parent;
	}

	@Override
	protected void savePreference() {
		// nothing to do
	}
	
	protected Composite getContainer() {
		return this.container;
	}
	
}
