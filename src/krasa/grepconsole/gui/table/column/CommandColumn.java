package krasa.grepconsole.gui.table.column;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.gui.CommandSettingsForm;
import krasa.grepconsole.model.GrepExpressionItem;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * @author Vojtech Krasa
 */
public class CommandColumn extends ButtonColumnInfo<GrepExpressionItem>
{
	public static final Icon COMMAND_OFF = IconLoader.getIcon("Cog1.png", CommandColumn.class);
	public static final Icon COMMAND_ON = IconLoader.getIcon("Cog2.png", CommandColumn.class);
	private final SettingsDialog settingsDialog;
	protected CommandSettingsForm commandSettingsForm;

	public CommandColumn(String command, SettingsDialog settingsDialog)
	{
		super(command);
		this.settingsDialog = settingsDialog;
		commandSettingsForm = new CommandSettingsForm();
	}

	private boolean showDialog(GrepExpressionItem item)
	{
		DialogBuilder builder = new DialogBuilder(settingsDialog.getRootComponent());
		builder.setCenterPanel(commandSettingsForm.getRoot());
		builder.setDimensionServiceKey("GrepConsoleCommand");
		builder.setTitle("Command settings");
		builder.removeAllActions();
		builder.addOkAction();
		builder.addCancelAction();

		commandSettingsForm.setData(item.getConsoleCommand());
		boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
		if (isOk)
		{
			commandSettingsForm.getData(item.getConsoleCommand());
		}
		return isOk;
	}

	@Override
	void onButtonClicked(GrepExpressionItem item)
	{
		showDialog(item);
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(GrepExpressionItem o)
	{
		return new ButtonEditor<GrepExpressionItem>(new JCheckBox())
		{
			@Override
			protected void setStyle(GrepExpressionItem grepExpressionItem)
			{
				if (grepExpressionItem.getConsoleCommand().isEnabled())
				{
					button.setIcon(COMMAND_ON);
				}
				else
				{
					button.setIcon(COMMAND_OFF);
				}
			}

			@Override
			protected void onButtonClicked(GrepExpressionItem item)
			{
				CommandColumn.this.onButtonClicked(item);
			}
		};
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(GrepExpressionItem aVoid)
	{
		return new ButtonRenderer()
		{
			@Override
			protected void setStyle(Object value)
			{
				GrepExpressionItem grepExpressionItem = (GrepExpressionItem) value;
				if (grepExpressionItem.getConsoleCommand().isEnabled())
				{
					setIcon(COMMAND_ON);
				}
				else
				{
					setIcon(COMMAND_OFF);
				}
			}
		};
	}
}
