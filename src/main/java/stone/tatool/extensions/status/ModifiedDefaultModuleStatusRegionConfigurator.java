package stone.tatool.extensions.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import stone.javatasks.helperclasses.swing.util.Theme;
import ch.tatool.core.display.swing.status.DefaultModuleStatusRegionConfigurator;
import ch.tatool.core.display.swing.status.StatusPanel;

/**
 * Modified version of the DMSRC class provided as part of the Tatool project
 * 
 * @author James Stone
 *
 */

public class ModifiedDefaultModuleStatusRegionConfigurator extends DefaultModuleStatusRegionConfigurator{

	private List<String> panels;
	
	/**
	 * Constructor, this is called to setup whatever status panels 
	 * have been requested by the user.
	 */
	public ModifiedDefaultModuleStatusRegionConfigurator() {
		super();
	}
	
	private void setupStatusPanels() {
		Map<String, StatusPanel> statusPanels = new LinkedHashMap<String, StatusPanel>(); // used a linked map to retain order!

		// Add level panel
		if (panels.contains(StatusPanel.STATUS_PANEL_LEVEL)) {
			CurvedGlossyThemedTextStatusPanel levelPanel = new CurvedGlossyThemedTextStatusPanel(Theme.GLOSSY_DARKRED_THEME);
			levelPanel.setProperty(StatusPanel.PROPERTY_TITLE, "LEVEL");
			statusPanels.put(StatusPanel.STATUS_PANEL_LEVEL, levelPanel);
		}
		
		// Add trial count
		if (panels.contains(StatusPanel.STATUS_PANEL_TRIAL)) {
			CurvedGlossyThemedTextStatusPanel trialPanel = new CurvedGlossyThemedTextStatusPanel(Theme.GLOSSY_DARKRED_THEME);
			trialPanel.setProperty(StatusPanel.PROPERTY_TITLE, "TRIAL");
			statusPanels.put(StatusPanel.STATUS_PANEL_TRIAL, trialPanel);
		}
		
		// Add task feedback
		if (panels.contains(StatusPanel.STATUS_PANEL_OUTCOME)) {
			//CorrectWrongStatusPanel feedbackPanel = new CorrectWrongStatusPanel();
			CurvedGlossyThemedFeedbackStatusPanel feedbackPanel = new CurvedGlossyThemedFeedbackStatusPanel(Theme.GLOSSY_RED_THEME);
			feedbackPanel.setProperty(StatusPanel.PROPERTY_TITLE, "FEEDBACK");
			statusPanels.put(StatusPanel.STATUS_PANEL_OUTCOME, feedbackPanel);
		}
		
		// Add timer
		if (panels.contains(StatusPanel.STATUS_PANEL_TIMER)) {
			CurvedGlossyThemedTimerStatusPanel timerPanel = new CurvedGlossyThemedTimerStatusPanel(Theme.GLOSSY_DARKRED_THEME);
			timerPanel.setProperty(StatusPanel.PROPERTY_TITLE, "TIMER");
			statusPanels.put(StatusPanel.STATUS_PANEL_TIMER, timerPanel);
		}
		
		// Add block
		if (panels.contains(StatusPanel.STATUS_PANEL_BLOCK)) {
			CurvedGlossyThemedTextStatusPanel blockPanel = new CurvedGlossyThemedTextStatusPanel(Theme.GLOSSY_DARKRED_THEME);
			blockPanel.setProperty(StatusPanel.PROPERTY_TITLE, "BLOCK");
			statusPanels.put(StatusPanel.STATUS_PANEL_BLOCK, blockPanel);
		}
		
		// add Custom 
		if (panels.contains(StatusPanel.STATUS_PANEL_CUSTOM_ONE)) {
			CurvedGlossyThemedTextStatusPanel customPanel1 = new CurvedGlossyThemedTextStatusPanel(Theme.GLOSSY_DARKRED_THEME);
			customPanel1.setProperty(StatusPanel.PROPERTY_TITLE, "CUSTOM");
			statusPanels.put(StatusPanel.STATUS_PANEL_CUSTOM_ONE, customPanel1);
		}
		
		// add Custom 
		if (panels.contains(StatusPanel.STATUS_PANEL_CUSTOM_TWO)) {
			CurvedGlossyThemedTextStatusPanel customPanel2 = new CurvedGlossyThemedTextStatusPanel(Theme.GLOSSY_DARKRED_THEME);
			customPanel2.setProperty(StatusPanel.PROPERTY_TITLE, "CUSTOM");
			statusPanels.put(StatusPanel.STATUS_PANEL_CUSTOM_TWO, customPanel2);
		}
		
		// add Custom 
		if (panels.contains(StatusPanel.STATUS_PANEL_CUSTOM_THREE)) {
			CurvedGlossyThemedTextStatusPanel customPanel3 = new CurvedGlossyThemedTextStatusPanel(Theme.GLOSSY_DARKRED_THEME);
			customPanel3.setProperty(StatusPanel.PROPERTY_TITLE, "CUSTOM");
			statusPanels.put(StatusPanel.STATUS_PANEL_CUSTOM_THREE, customPanel3);
		}
		
		// DUAL N BACK FEEDBACK PANELS
		if (panels.contains(StatusPanel.STATUS_PANEL_DNB_FEEDBACK_LOCI)) {
			CurvedGlossyThemedFeedbackStatusPanel dnbLociPanel = new CurvedGlossyThemedFeedbackStatusPanel(Theme.GLOSSY_RED_THEME);
			dnbLociPanel.setProperty(StatusPanel.PROPERTY_TITLE, "GRID");
			statusPanels.put(StatusPanel.STATUS_PANEL_DNB_FEEDBACK_LOCI, dnbLociPanel);
		}

		if (panels.contains(StatusPanel.STATUS_PANEL_DNB_FEEDBACK_AUDIO)) {
			CurvedGlossyThemedFeedbackStatusPanel dnbAudioPanel = new CurvedGlossyThemedFeedbackStatusPanel(Theme.GLOSSY_RED_THEME);
			dnbAudioPanel.setProperty(StatusPanel.PROPERTY_TITLE, "LETTER");
			statusPanels.put(StatusPanel.STATUS_PANEL_DNB_FEEDBACK_AUDIO, dnbAudioPanel);
		}	

		setStatusPanels(statusPanels);
	}
	
	public void setPanels(List<String> panels) {
		this.panels = panels;
		setupStatusPanels();
	}
}
