package org.grits.toolbox.entry.ms.annotation.glycan.report.command;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.glycan.report.handler.ViewMSGlycanAnnotationResultsFromMerge;

@SuppressWarnings("restriction")
public class ViewMSOverviewFromMergeCommandExecutor  {
	public static void showMSOverview(IEclipseContext context, Entry entry ) {
		ECommandService commandService = context.get(ECommandService.class);
		EHandlerService handlerService = context.get(EHandlerService.class);
		
		context.set(ViewMSGlycanAnnotationResultsFromMerge.PARAMETER_ID, entry);
		handlerService.executeHandler(
			commandService.createCommand(ViewMSGlycanAnnotationResultsFromMerge.COMMAND_ID, null));
	}
}
