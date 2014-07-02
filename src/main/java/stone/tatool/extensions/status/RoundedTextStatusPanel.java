package stone.tatool.extensions.status;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import ch.tatool.core.display.swing.status.TextStatusPanel;


/**
 * Another possible implementation of a TextStatusPanel. These panels will 
 * have rounded corners. 
 * 
 * @author James Stone
 *
 */
public class RoundedTextStatusPanel extends TextStatusPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1446720480851012886L;
	protected int _strokeSize = 2;
    protected Color _shadowColor = Color.BLACK;
    protected boolean _shadowed = false;
    protected boolean _highQuality = true;
    protected Dimension _arcs = new Dimension(30, 30);
    protected int _shadowGap = 5;
    protected int _shadowOffset = 4;
    protected int _shadowAlpha = 150;

    protected Color _backgroundColor = Color.LIGHT_GRAY;
   

    public RoundedTextStatusPanel()
    {
        super();
        setOpaque(false);
        setBorder(null);
    }

    @Override
    public void setBackground(Color c)
    {
        _backgroundColor = c;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int shadowGap = this._shadowGap;
        Color shadowColorA = new Color(_shadowColor.getRed(), _shadowColor.getGreen(), _shadowColor.getBlue(), _shadowAlpha);
        Graphics2D graphics = (Graphics2D) g;

        if(_highQuality)
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if(_shadowed)
        {
            graphics.setColor(shadowColorA);
            graphics.fillRoundRect(_shadowOffset, _shadowOffset, width - _strokeSize - _shadowOffset,
                    height - _strokeSize - _shadowOffset, _arcs.width, _arcs.height);
        }
        else
        {
            _shadowGap = 1;
        }

        graphics.setColor(_backgroundColor);
        graphics.fillRoundRect(0,  0, width - shadowGap, height - shadowGap, _arcs.width, _arcs.height);
        graphics.setStroke(new BasicStroke(_strokeSize));
        graphics.setColor(getForeground());
        graphics.drawRoundRect(0,  0, width - shadowGap, height - shadowGap, _arcs.width, _arcs.height);
        graphics.setStroke(new BasicStroke());
    }
    
}