package krasa.grepconsole.plugin;

import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.model.DomainObject;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.TailSettings;

import java.util.ArrayList;
import java.util.List;

public class PluginState extends DomainObject implements Cloneable {

	private List<Profile> profiles = new ArrayList<>();
	private TailSettings tailSettings;
	private boolean enabled;

	public Profile getDefaultProfile() {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.isDefaultProfile()) {
				result = profile;
			}
		}

		if (result == null) {
			Profile profile = profiles.get(0);
			profile.setDefaultProfile(true);
			result = profile;
		}
		return result;
	}

	public Profile getProfile(Profile oldProfile) {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.getId() == oldProfile.getId()) {
				result = profile;
			}
		}
		if (result == null) {
			result = getDefaultProfile();
		}
		return result;
	}

	public TailSettings getTailSettings() {
		if (tailSettings == null) {
			tailSettings = new TailSettings();
		}
		return tailSettings;
	}

	public void setTailSettings(TailSettings tailSettings) {
		this.tailSettings = tailSettings;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	@Override
	protected PluginState clone() {
		return krasa.grepconsole.Cloner.deepClone(this);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	@Transient
	public boolean isSynchronousHighlighting() {
		return getDefaultProfile().isSynchronous();
	}

}
