package krasa.grepconsole.gui;

import krasa.grepconsole.model.ConsoleCommand;

import javax.swing.*;

/**
 * @author Vojtech Krasa
 */
public class CommandSettingsForm
{
	private JTextField commandText;
	private JPanel root;

	public CommandSettingsForm()
	{

	}

	public JPanel getRoot()
	{
		return root;
	}

	public void setData(ConsoleCommand data)
	{
		commandText.setText(data.getCommand());
	}

	public void getData(ConsoleCommand data)
	{
		data.setCommand(commandText.getText());
	}

	public boolean isModified(ConsoleCommand data)
	{

		if (commandText.getText() != null ? !commandText.getText().equals(data.getCommand()) : data.getCommand() != null)
			return true;
		return false;
	}

}
