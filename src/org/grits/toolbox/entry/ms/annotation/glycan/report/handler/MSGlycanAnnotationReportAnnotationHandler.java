package org.grits.toolbox.entry.ms.annotation.glycan.report.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.GeneralSettings;
import org.grits.toolbox.core.datamodel.SettingEntry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.core.utils.SettingsHandler;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.dialog.MSGlycanAnnotationReportDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.report.process.loader.MSGlycanAnnotationReportTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.datamodel.MSGlycanAnnotationReportMetaData;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantAlias;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantFileToAlias;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.merge.action.MergeReportGenerator;
import org.grits.toolbox.merge.om.data.ExperimentAnnotation;
import org.grits.toolbox.merge.om.data.MergeReport;
import org.grits.toolbox.merge.xml.Deserialize;
import org.grits.toolbox.merge.xml.Serializer;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.widgets.processDialog.ProgressDialog;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.progress.ProgressThread;

public class MSGlycanAnnotationReportAnnotationHandler {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationReportAnnotationHandler.class);
	private static final String WARNINGDIALOGSETTING = "Merge Report Warning Dialog";
	@Inject private static IGritsDataModelService gritsDataModelService = null;
	@Inject static IGritsUIService gritsUIService = null;
	private MergeReport report = null; // to be used for processing external quant after creating the archvie
	boolean canceled = false;
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IEventBroker eventBroker, @Named (IServiceConstants.ACTIVE_SHELL) Shell shell, EPartService partService) {

		StructuredSelection to = null;
		Entry selectedEntry = null;
		if(object instanceof Entry)
		{
			selectedEntry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				to = (StructuredSelection) object;
			}
		}
		// try getting the last selection from the data model
		if(selectedEntry == null
				&& gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
		{
			to = gritsDataModelService.getLastSelection();
		}

		if(to == null)
		{
			if (selectedEntry != null 
//					&& selectedEntry.getProperty().getType().equals(MSGlycanAnnotationProperty.TYPE)) {
					&& selectedEntry.getProperty().getType().equals(getAnnotationPropertyType())) {
				List<Entry> entries = new ArrayList<Entry>();
				entries.add(selectedEntry);
				createSimianMergeDialog(shell, entries, eventBroker, partService);
			}
			else {
				//then create dialog with empty list
				createSimianMergeDialog(shell, null, eventBroker, partService);
			}
		} else {
			//if more than one entry is selected, check if all entries are correctly chosen
			//Path contains all entries that are selected and other entries along the path to the its project parent.
			boolean correctEntries = true;
			List<Entry> entries = new ArrayList<Entry>();
			Entry projectEntry = null;
			List<Entry> selList = to.toList();
			for(int i=0; i < selList.size(); i++) {
				Entry simEntry = selList.get(i);
				//				Entry simEntry = EclipseLegacyUtils.getCurrentEntry(curSelection);
				//if the right property
//				if(simEntry.getProperty().getType().equals(MSGlycanAnnotationProperty.TYPE)) {
				if(simEntry.getProperty().getType().equals(getAnnotationPropertyType())) {
					//and need to be under a same project
					if(projectEntry == null) {
						projectEntry = DataModelSearch.findParentByType(simEntry, ProjectProperty.TYPE);
						entries.add(simEntry);
					}
					else {
						//otherwise entries are from different projects.
						if(!projectEntry.equals(DataModelSearch.findParentByType(simEntry, ProjectProperty.TYPE))) {
							correctEntries = false;
							break;
						}
						else {
							//if correct then add that into the list
							entries.add(simEntry);
						}
					}
				}
				else {
					//if not SimGlycanProperty then set the flag false;
					correctEntries = false;
					//and get out of for loop
					break;
				}
			}

			//check the flag
			if(correctEntries) {
				//then create SimianMergeDialog with all the correctly selected entries.
				createSimianMergeDialog(shell, entries, eventBroker, partService);
			}
			else {
				//then create SimianMergeDialog with empty list
				createSimianMergeDialog(shell, null, eventBroker, partService);
			}
		}
	}

	protected String getAnnotationPropertyType() {
		return MSGlycanAnnotationProperty.TYPE;
	}

	/**
	 * Reads the MergeReport so the CustomExtraData information can be accessed.
	 * 
	 * @param activeShell
	 * @param sSourceFile the name of the report file
	 * @return the MergeReport object if read successfully
	 */
	public MergeReport readDataFromFile( final Shell activeShell, final String sSourceFile) {

		ProgressDialog dialog = new ProgressDialog(activeShell, SWT.INDETERMINATE);
		ProgressThread worker = new ProgressThread() {
			@Override
			public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
				((ProgressDialog) a_progressThreadHandler).setProcessMessageLabel("Reading Merge report");
				Job job = new Job("Reading archive...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							Deserialize des = new Deserialize();
							logger.debug("Reading archive: " + sSourceFile);
							report = des.deserialize(sSourceFile);

							return Status.OK_STATUS;
						} catch( Exception ex ) {
							logger.error(ex.getMessage(), ex);
							return Status.CANCEL_STATUS;
						}
					}
				};
				job.schedule();
				while( (job.getState() == Job.WAITING || job.getState() == Job.RUNNING) && ! a_progressThreadHandler.isCanceled() ) {
					Thread.sleep(1000);
				}
				return job.getResult() == Status.OK_STATUS;
			}
			@Override
			public void cancelWork() {
				//				return false;

			}
		};			
		dialog.setWorker(worker);	
		int iRes = dialog.open();
		return report;
	}

	/**
	 * Using the meta data info for the individual experiments, determine the user-specified column labels for external
	 * quant and store them in the report meta data.
	 * 
	 * @param expEntries the list of experiments in the report 
	 * @param peakCustomExtraData the list of CustomExtraData for the peaks (where external quant data is stored)
	 * @param metaData the report metadata
	 */
	private void addExternalQuantAliasInfo( List<Entry> expEntries, 
			List<CustomExtraData> peakCustomExtraData, MSGlycanAnnotationReportMetaData metaData ) {
		/* expToExternalQuant:
		 * key1: experiment id
		 * 
		 * key2: peak custom extra data key
		 * value: ExternalQuantAlias
		 * 
		 *
		  private HashMap<String, ExternalQuantFileToAlias> expToExternalQuant = null; 
		 */

		HashMap<String, HashMap<String,String>> mExtQuantToKnownKeyToAliasPrefixes = new HashMap<>();
		for( int i = 0; i < expEntries.size(); i++ ) {
			Entry entry = expEntries.get(i);
			MSGlycanAnnotationProperty t_property = null;
			t_property = (MSGlycanAnnotationProperty)entry.getProperty();
			MSAnnotationMetaData expMetaData = t_property.getMSAnnotationMetaData();
			for( String sQuantType : expMetaData.getQuantTypeToExternalQuant().keySet() ) {
				HashMap<String,String> mKnownKeyToAliasPrefixes = new HashMap<>();
				mExtQuantToKnownKeyToAliasPrefixes.put(sQuantType, mKnownKeyToAliasPrefixes);
				ExternalQuantFileToAlias extQuantInfo = expMetaData.getExternalQuantToAliasByQuantType(sQuantType);
				for( String sFileName : extQuantInfo.getSourceDataFileNameToAlias().keySet() ) {
					ExternalQuantAlias alias = extQuantInfo.getSourceDataFileNameToAlias().get(sFileName);
					for( CustomExtraData ced : peakCustomExtraData ) {
						if( ced.getKey().startsWith(alias.getKey() + "_quant") && ced.getLabel().startsWith(alias.getAlias() + " ") ) {
							mKnownKeyToAliasPrefixes.put(ced.getKey(), alias.getAlias());
						}
					}
				}
			}
		}

		if( mExtQuantToKnownKeyToAliasPrefixes.isEmpty() ) { // no aliases
			return;
		}
		for( int i = 0; i < expEntries.size(); i++ ) {
			Entry entry = expEntries.get(i);
			MSGlycanAnnotationProperty t_property = null;
			t_property = (MSGlycanAnnotationProperty)entry.getProperty();
			MSAnnotationMetaData expMetaData = t_property.getMSAnnotationMetaData();
			String sKey1 = expMetaData.getAnnotationId();
			ExternalQuantFileToAlias eqfa = null;
			if( metaData.getExpToExternalQuant().containsKey(sKey1) ) {
				eqfa = metaData.getExpToExternalQuant().get(sKey1);
			} else {
				eqfa = new ExternalQuantFileToAlias();
				metaData.getExpToExternalQuant().put(sKey1, eqfa);
			}
			if( eqfa.getSourceDataFileNameToAlias() == null ) {
				eqfa.setSourceDataFileNameToAlias(new HashMap<>());
			}
			for( CustomExtraData ced : peakCustomExtraData ) {
				for( String sExtQuantType : mExtQuantToKnownKeyToAliasPrefixes.keySet() ) {
					HashMap<String,String> mKnownKeyToAliasPrefixes = mExtQuantToKnownKeyToAliasPrefixes.get(sExtQuantType);
					if( ! mKnownKeyToAliasPrefixes.containsKey(ced.getKey()) ) {
						continue;
					}
					CustomExtraData defKey = MassSpecUISettings.getDefaultExternalQuantCED(sExtQuantType, ced.getKey());
					// a legacy annotation that wasn't opened won't have these data, so don't apply any aliasing!
					if( ! expMetaData.getQuantTypeToExternalQuant().isEmpty() ) {
						boolean bFound = false;		
						if( expMetaData.getQuantTypeToExternalQuant().containsKey(sExtQuantType) ) {
							//						for( String sQuantType : expMetaData.getQuantTypeToExternalQuant().keySet() ) {
							ExternalQuantFileToAlias extQuantInfo = expMetaData.getExternalQuantToAliasByQuantType(sExtQuantType);
							for( String sHeaderKey : extQuantInfo.getSourceDataFileNameToAlias().keySet() ) {
								ExternalQuantAlias alias = extQuantInfo.getSourceDataFileNameToAlias().get(sHeaderKey);
								// try to match the known CustomExtraData column key to what is in this experiment
								if( alias != null && ced.getKey().startsWith(alias.getKey() + "_quant") ) {
									String sFixedKey = ced.getKey();
									String sFixedAlias = mKnownKeyToAliasPrefixes.get(sFixedKey);

									String sNewAlias = ced.getLabel().replace(sFixedAlias, alias.getAlias());
									ExternalQuantAlias newEQA = new ExternalQuantAlias();
									newEQA.setAlias(sNewAlias);
									newEQA.setKey(sFixedKey);
									eqfa.getSourceDataFileNameToAlias().put(sFixedKey, newEQA);	
									bFound = true;
								} 
							}
						}
						if( ! bFound ) { // extra empty columns!
							//							String sFixedAlias = mKnownKeyToAliasPrefixes.get(sFixedKey);
							//							String sNewAlias = ced.getLabel().replace(sFixedAlias, "Unspecified ");
							String sFixedKey = ced.getKey();
							ExternalQuantAlias newEQA = new ExternalQuantAlias();
							newEQA.setAlias("Unused " + defKey.getLabel());
							newEQA.setKey(sFixedKey);
							eqfa.getSourceDataFileNameToAlias().put(sFixedKey, newEQA);								
						}
					} else {
						String sFixedKey = ced.getKey();
						ExternalQuantAlias newEQA = new ExternalQuantAlias();
						newEQA.setAlias(defKey.getLabel());
						newEQA.setKey(sFixedKey);
						eqfa.getSourceDataFileNameToAlias().put(sFixedKey, newEQA);								
					}
				}		
			}
		}
	}


	private Object createSimianMergeDialog(Shell activeShell, List<Entry> entries, IEventBroker eventBroker, EPartService partService) {
		//save "do not show" settings in the configuration directory
		GeneralSettings settings = null;
		try {
			settings = SettingsHandler.readSettings();
		} catch (Exception ex) {
			logger.warn("Settings file does not exist yet");
		}
		if (settings == null || !settings.isHiddenDialog(WARNINGDIALOGSETTING)) {
			MessageDialogWithToggle warningDialog = MessageDialogWithToggle.openWarning(activeShell, "Warning", "Some quantitative information may be unavailable for experiments if you haven't already applied external quantification files "
					+ "for them through \"Tools->MS Glycan Annotation->External Quantification\"", "Do not show again", false, null, null);
			if (warningDialog.getToggleState()) {
				if (settings == null)
					settings = new GeneralSettings();
				SettingEntry se = new SettingEntry();
				se.setId(WARNINGDIALOGSETTING);
				se.setName(WARNINGDIALOGSETTING);
				se.setDescription("Dialog warning about merging experiments without applying external quantification first");
				settings.addHiddenDialog(se);
				
				try {
					SettingsHandler.writeSettings(settings);
				} catch (Exception e) {
					logger.warn("Could not update settings file", e);
				}
			}
		}
//		MSGlycanAnnotationReportDialog dialog = new MSGlycanAnnotationReportDialog(PropertyHandler.getModalDialog(activeShell),entries);
		MSGlycanAnnotationReportDialog dialog = getReportDialog(PropertyHandler.getModalDialog(activeShell),entries);
		if(dialog.open() == 0)
		{
			//activeShell is closed already. Thus create a new shell for errors
			Shell modalDialog = PropertyHandler.getModalDialog(new Shell());

			//get the workspace location 
			String workspaceLocation = PropertyHandler.getVariable("workspace_location");
			//get the project name//at least one entry has to be there
			Entry projectEntry = DataModelSearch.findParentByType(dialog.getAnnotationEntryList().get(0), ProjectProperty.TYPE);
			String projectName = projectEntry.getDisplayName();


			//for progressbar dialog
			int totalScans = 0;

			Entry reportsEntry = null;
			//look for reports entry if not, then create a new one.
			for(Entry child: projectEntry.getChildren())
			{
				//reports is right under project entry
				if(child.getProperty().getType().equals(ReportsProperty.TYPE))
				{
					reportsEntry = child;
				}
			}

			//no reportsEntry is found
			if(reportsEntry == null)
			{
				//create a new reports entry
				reportsEntry = new Entry();
				reportsEntry.setDisplayName("reports");
				reportsEntry.setProperty(new ReportsProperty());

				//then create a new Reports Entry 
				try
				{
					PropertyHandler.getDataModel().setShow(false);
					gritsDataModelService.addEntry(projectEntry, reportsEntry);
					try
					{
						ProjectFileHandler.saveProject(projectEntry);
					} catch (IOException e)
					{
						logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
						logger.fatal("Closing project entry \""
								+ projectEntry.getDisplayName() + "\"");
						gritsDataModelService.closeProject(projectEntry);
						throw e;
					}
				} catch(IOException e)
				{
					//need to log
					logger.error(e.getMessage(),e);
					//need to show an error msg
					ErrorUtils.createErrorMessageBox(modalDialog, e.getMessage(), e);
					return null;
				}
			}

			//check if reports folder is in the project or not
			//if not create a new one
			File reportsFolder = new File(workspaceLocation+projectName+"/"+ReportsProperty.getFolder());
			if(!reportsFolder.exists())
			{
				if(!reportsFolder.mkdirs())
				{
					//need to log also
					logger.error("Cannot create the folder: " + reportsFolder.getAbsolutePath());
					//need to show en error msg
					ErrorUtils.createWarningMessageBox(modalDialog, "Error While Creating A Folder", "Cannot create the folder: " + reportsFolder.getAbsolutePath());
					return null;
				}
			}

			//need to create merge folder for this merge..
//			File mergeFolder = new File(reportsFolder+"/merge");
			File mergeFolder = new File(reportsFolder+"/"+getMergeFolder());
			if(!mergeFolder.exists())
			{
				if(!mergeFolder.mkdirs())
				{
					//need to log also
					logger.error("Cannot create the folder: " + mergeFolder.getAbsolutePath());
					//need to show en error msg
					ErrorUtils.createWarningMessageBox(modalDialog, "Error While Creating A Folder", "Cannot create the folder: " + mergeFolder.getAbsolutePath());
					return null;
				}
			}

			//need to get an id from new entry simGlycanMerge
			String entryId = MSGlycanAnnotationReportProperty.getRandomId();
			File imgFolder = new File(mergeFolder+"/"+ entryId);
			while(imgFolder.exists())
			{
				entryId = MSGlycanAnnotationReportProperty.getRandomId();
				imgFolder = new File(mergeFolder+"/"+ entryId);
			}

			// then create a new simGlycanMerge img folder
			if(!imgFolder.mkdir())
			{
				//need to log 
				logger.error("cannot create the folder: " + imgFolder.getAbsolutePath());
				//need to show en error msg
				ErrorUtils.createWarningMessageBox(modalDialog, "Error", "cannot create the folder: " + imgFolder.getAbsolutePath());
				return null;
			}


			//then create a new SimGlycanMerge Entry 
			try
			{
				List<ExperimentAnnotation> experimentAnnotations = new ArrayList<>();
				for( int i = 0; i < dialog.getAnnotationEntryList().size(); i++ ) {
					Entry entry = dialog.getAnnotationEntryList().get(i);
					MSGlycanAnnotationProperty t_property = null;
					t_property = (MSGlycanAnnotationProperty)entry.getProperty();
					String msAnnotationFolder = workspaceLocation+projectName+File.separator+t_property.getArchiveFolder();
					String reportFile = msAnnotationFolder+File.separator+t_property.getMSAnnotationMetaData().getAnnotationId()+t_property.getArchiveExtension();
					ExperimentAnnotation expAnnotation = new ExperimentAnnotation();
					expAnnotation.setAnnotationEntryId(Integer.parseInt(t_property.getMSAnnotationMetaData().getAnnotationId()));
					expAnnotation.setAnnotationFileArchive(reportFile);
					String displayName = MSGlycanAnnotationReportTableDataProcessorUtil.getDisplayNameByEntryName( t_property.getMSAnnotationMetaData().getAnnotationId());
					String sUserInfo = dialog.getAnnotationInfo(displayName);

					expAnnotation.setAnnotationDisplayName(displayName);
					expAnnotation.setAnnotationShortName(sUserInfo); // TODO: need the alias!
					experimentAnnotations.add(expAnnotation);

				}
				int iResult = generateMergeReport(activeShell, experimentAnnotations, dialog.getInterval(), dialog.getAccuracyType(), imgFolder+File.separator+entryId+".xml");
				if (iResult == SWT.CANCEL || canceled) {
					// remove mergeFolder
					deleteDirectory (imgFolder);
				}
				else if( iResult == SWT.OK ) {
					//then set the new id to the SimGlycanMergeProperty 
					Entry repEntry = dialog.createEntry();
					MSGlycanAnnotationReportProperty repProperty = ((MSGlycanAnnotationReportProperty)repEntry.getProperty());
					MSGlycanAnnotationReportMetaData metaData = repProperty.getMsGlycanAnnotReportMetaData();
					metaData.setReportId(entryId);


					String metaDataFileName = metaData.getReportId() + File.separator + repProperty.getMetaDataFileName();
					PropertyDataFile msMetaData = MSGlycanAnnotationReportProperty.getNewSettingsFile(metaDataFileName, metaData);
					repProperty.getDataFiles().add(msMetaData);

					String sReportFile = repProperty.getFullyQualifiedXMLFileName(projectEntry);
					String reportFileName = metaData.getReportId() + File.separator + repProperty.getArchiveFile();

					readDataFromFile(activeShell, sReportFile);
					if( report != null && report.getPeakCustomExtraData() != null ) {
						addExternalQuantAliasInfo(dialog.getAnnotationEntryList(), report.getPeakCustomExtraData(), metaData);
					}

					String sFileName = repProperty.getFullyQualifiedMetaDataFileName(projectEntry);					
					MSGlycanAnnotationReportProperty.marshallSettingsFile(sFileName, metaData);

					File file = new File( sReportFile );
					PropertyDataFile pdf = null;
					if( file.exists() ) { // archive is a single zip file
						pdf = new PropertyDataFile(reportFileName, MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FILE);
					} 

					if( pdf == null ) {
						throw new UnsupportedVersionException("Expecting an archive file or folder. Not found.", "Preversion");
					}
					repProperty.getDataFiles().add(pdf);

					PropertyHandler.getDataModel().setShow(true);	
					gritsDataModelService.addEntry(reportsEntry, repEntry);
					try
					{
						ProjectFileHandler.saveProject(projectEntry);
					} catch (IOException e)
					{
						logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
						logger.fatal("Closing project entry \""
								+ projectEntry.getDisplayName() + "\"");
						gritsDataModelService.closeProject(projectEntry);
						throw e;
					}
					PropertyHandler.getDataModel().setShow(true);	
					if(repEntry != null)
					{
						// post does not work, we have to make sure the last selection is set before we try to open the part
						eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, repEntry);
						gritsUIService.setPartService(partService);
						gritsUIService.openEntryInPart(repEntry);
					}
				} else if ( iResult != SWT.CANCEL ) {
					logger.error("Sorry, but an error occurred merging the reports.");
					ErrorUtils.createErrorMessageBox(modalDialog, "Sorry, but an error occurred merging the reports.");				
				}
			} catch(Exception e)
			{
				//need to log
				logger.error(e.getMessage(),e);
				//need to show an error msg
				ErrorUtils.createErrorMessageBox(modalDialog, e.getMessage(), e);
				return null;
			}
		}
		return null;
	}

	protected MSGlycanAnnotationReportDialog getReportDialog(Shell parentShell, List<Entry> entries) {
		return new MSGlycanAnnotationReportDialog(parentShell, entries);
	}

	protected String getMergeFolder() {
		return MSGlycanAnnotationReportProperty.ARCHIVE_FOLDER;
	}

	/**
	 * recursively empty and delete the folder
	 * @param file folder/file to be deleted
	 */
	private void deleteDirectory(File file) {
		if (file.isDirectory()) {
			for (File f: file.listFiles()) {
				deleteDirectory(f);
			}
			file.delete();
		} else {
			file.delete();
		}	
	}

	protected int generateMergeReport(final Shell activeShell, final List<ExperimentAnnotation> _lExperimentAnnotations, 
			final double _dInterval, final String _sAccuracyType, final String _sResultFile) {

		ProgressDialog dialog = new ProgressDialog(activeShell, SWT.INDETERMINATE);
		ProgressThread worker = new ProgressThread() {

			@Override
			public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
				((ProgressDialog) a_progressThreadHandler).setProcessMessageLabel("Generating Merge report");
				Job job = new Job("Generating merge report.") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							MergeReport report = new MergeReport();
							MergeReportGenerator generator = null;
							Serializer ser = new Serializer();
							generator = new MergeReportGenerator(_lExperimentAnnotations, _dInterval, _sAccuracyType);
							report = generator.generateReport();
							ser.serialize(report, _sResultFile);
							return Status.OK_STATUS;
						} catch( Exception ex ) {
							logger.error(ex.getMessage(), ex);
							return Status.CANCEL_STATUS;
						}
					}
				};
				job.schedule();

				while( (job.getState() == Job.WAITING || job.getState() == Job.RUNNING) && ! a_progressThreadHandler.isCanceled() ) {
					Thread.sleep(1000);
				}
				return job.getResult() == Status.OK_STATUS;
			}

			@Override
			public void cancelWork() {
				canceled = true;
			}
		};			
		dialog.setWorker(worker);	
		int iRes = dialog.open();
		return iRes;
	}
}
