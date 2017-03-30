package krasa.grepconsole.buildlistener;

import com.intellij.compiler.impl.CompileContextImpl;
import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.ui.UIUtil;
import krasa.grepconsole.model.ConsoleCommand;
import krasa.grepconsole.model.Sound;

public class TaskFinishedListener implements CompilationStatusListener
{
	private final int minBuildTime;
	private final Project project;
	private final Sound sound;
	private final ConsoleCommand command;
	private final boolean claimFocus;

	public TaskFinishedListener(Project project, int minBuildTime, Sound sound, ConsoleCommand command, boolean claimFocus)
	{
		this.minBuildTime = minBuildTime;
		this.sound = sound;
		this.project = project;
		this.claimFocus = claimFocus;
		this.command = command;

	}

	@Override
	public void compilationFinished(boolean aborted, int errors, int warnings, CompileContext compileContext)
	{
		CompileContextImpl impl = (CompileContextImpl) compileContext;
		long duration = System.currentTimeMillis() - impl.getStartCompilationStamp();
		duration = duration / 1000; //milli to seconds
		if (duration >= minBuildTime)
		{
			if (claimFocus)
			{
				focus();
			}
			if (sound.isEnabled())
			{
				sound.play();
			}
			if (command.isEnabled())
			{
				command.run();
			}
		}


	}


	private void focus()
	{
		UIUtil.toFront(WindowManager.getInstance().suggestParentWindow(project));
	}


}
