package krasa.grepconsole.buildlistener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerTopics;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.messages.MessageBus;
import krasa.grepconsole.model.ConsoleCommand;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.Sound;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class BuildListenerRegistrator implements ApplicationComponent
{
	@Override
	public void initComponent()
	{
		setupEventListeners();
	}

	@Override
	public void disposeComponent()
	{

	}

	private void setupEventListeners()
	{
		Profile profile = ApplicationManager.getApplication().getComponent(GrepConsoleApplicationComponent.class).getState().getDefaultProfile();
		if (profile.isClaimFocusAfterBuild() || profile.getSound().isEnabled() || profile.getCommand().isEnabled())
		{
			int minBuildTime = profile.getMinCompilationTimeAsInt();
			Sound sound = profile.getSound();
			ConsoleCommand command = profile.getCommand();
			boolean claimFocus = profile.isClaimFocusAfterBuild();
			ApplicationManager.getApplication().invokeLater(new Runnable()
			{
				public void run()
				{
					Project project = ProjectManager.getInstance().getOpenProjects()[0];
					MessageBus bus = project.getMessageBus();
					bus.connect().subscribe(CompilerTopics.COMPILATION_STATUS, new TaskFinishedListener(project, minBuildTime, sound, command, claimFocus));

				}
			});

		}
	}

	@NotNull
	@Override
	public String getComponentName()
	{
		return "buildlistenerregistrator";
	}
}