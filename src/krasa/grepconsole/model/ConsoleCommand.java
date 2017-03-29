package krasa.grepconsole.model;

import com.intellij.openapi.application.ApplicationManager;

/**
 * @author jonathan - 3/23/17.
 */
public class ConsoleCommand extends DomainObject
{
	private String command;
	private boolean enabled;

	public void run()
	{
		ApplicationManager.getApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Process p = Runtime.getRuntime().exec(command);
					p.waitFor();
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
		return this.enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
