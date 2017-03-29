package krasa.grepconsole.filter.support;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.ui.UIUtil;
import krasa.grepconsole.model.GrepExpressionItem;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrepProcessorImpl implements GrepProcessor {
	private static final Logger log = Logger.getInstance(GrepProcessorImpl.class.getName());

	private GrepExpressionItem grepExpressionItem;
	private int matches;

	public GrepProcessorImpl(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
	}

	@Override
	public GrepExpressionItem getGrepExpressionItem() {
		return grepExpressionItem;
	}

	@Override
	public int getMatches() {
		return matches;
	}

	@Override
	public void resetMatches() {
		this.matches = 0;
	}

	@SuppressWarnings("Duplicates")
	@Override
	public FilterState process(FilterState state) {
		if (grepExpressionItem.isEnabled() && !StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			CharSequence input = state.getCharSequence();
			if (grepExpressionItem.isHighlightOnlyMatchingText()) {
				Pattern pattern = grepExpressionItem.getPattern();
				if (pattern != null) {
					final Matcher matcher = pattern.matcher(input);
					while (matcher.find()) {
						matches++;
						final int start = matcher.start();
						final int end = matcher.end();
						state.setNextOperation(grepExpressionItem.getOperationOnMatch());
						state.setExclude(grepExpressionItem.isInputFilter());
						state.setMatchesSomething(true);
						MyResultItem resultItem = new MyResultItem(state.getOffset() + start, state.getOffset() + end,
								null, grepExpressionItem.getConsoleViewContentType(null));

						state.add(resultItem);
						if (grepExpressionItem.getSound().isEnabled()) {
							grepExpressionItem.getSound().play();
						}
						if (grepExpressionItem.isClaimFocus())
						{
							claim();
						}
						if (grepExpressionItem.getConsoleCommand().isEnabled())
						{
							grepExpressionItem.getConsoleCommand().run();
						}

					}
				}
			} else if (matches(input) && !matchesUnless(input)) {
				matches++;
				state.setNextOperation(grepExpressionItem.getOperationOnMatch());
				state.setConsoleViewContentType(
						grepExpressionItem.getConsoleViewContentType(state.getConsoleViewContentType()));
				state.setExclude(grepExpressionItem.isInputFilter());
				state.setMatchesSomething(true);
				if (grepExpressionItem.getSound().isEnabled()) {
					grepExpressionItem.getSound().play();
				}
				if (grepExpressionItem.isClaimFocus())
				{
					claim();
				}
				if (grepExpressionItem.getConsoleCommand().isEnabled())
				{
					grepExpressionItem.getConsoleCommand().run();
				}
			}


		}
		return state;
	}

	private void claim()
	{
		UIUtil.toFront(WindowManager.getInstance().suggestParentWindow(ProjectManager.getInstance().getOpenProjects()[0]));

	}


	private boolean matches(CharSequence input) {
		Pattern pattern = grepExpressionItem.getPattern();
		boolean matches = false;
		if (pattern != null) {
			matches = pattern.matcher(input).matches();
		}
		return matches;
	}

	private boolean matchesUnless(CharSequence input) {
		boolean matchUnless = false;
		Pattern unlessPattern = grepExpressionItem.getUnlessPattern();
		if (unlessPattern != null) {
			Matcher unlessMatcher = unlessPattern.matcher(input);
			if (unlessMatcher.matches()) {
				matchUnless = true;
			}
		}
		return matchUnless;
	}

	@Override
	public String toString() {
		String grepExpression = grepExpressionItem.getGrepExpression();
		String unless = grepExpressionItem.getUnlessGrepExpression();
		return "pattern='" + grepExpression + "', unlessPattern='" + unless + "'";
	}
}
