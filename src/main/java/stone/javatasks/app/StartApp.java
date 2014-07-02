



package stone.javatasks.app;

import javax.swing.UIManager;

import ch.tatool.app.App;

public class StartApp {

	public static void main(String[] args) {
		
		
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e)
		{}
		
		App.main(args);
	}
	
	//"javax.swing.plaf.metal.MetalLookAndFeel"//

}
