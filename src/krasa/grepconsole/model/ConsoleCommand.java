package krasa.grepconsole.model;

import com.intellij.openapi.application.ApplicationManager;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang.StringUtils;

/**
 * @author jonathan - 3/23/17.
 */
public class ConsoleCommand extends DomainObject
{
	private String command;

	public void run()
	{
		ApplicationManager.getApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					CommandLine cmdLine = CommandLine.parse(command);
					DefaultExecutor executor = new DefaultExecutor();
					executor.execute(cmdLine);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public String getCommand()
	{
		return command;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

	public Boolean isEnabled()
	{
		return StringUtils.isNotBlank(command);
	}

}
