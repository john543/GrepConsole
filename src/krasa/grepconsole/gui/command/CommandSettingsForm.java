package krasa.grepconsole.gui.command;

import krasa.grepconsole.model.ConsoleCommand;

import javax.swing.*;

/**
 * @author Vojtech Krasa
 */
public class CommandSettingsForm
{
	private JCheckBox enabledCheckBox;
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
		enabledCheckBox.setSelected(data.isEnabled());
		commandText.setText(data.getCommand());
	}

	public void getData(ConsoleCommand data)
	{
		data.setEnabled(enabledCheckBox.isSelected());
		data.setCommand(commandText.getText());
	}

	public boolean isModified(ConsoleCommand data)
	{
		if (enabledCheckBox.isSelected() != data.isEnabled())
			return true;
		if (commandText.getText() != null ? !commandText.getText().equals(data.getCommand()) : data.getCommand() != null)
			return true;
		return false;
	}

}
