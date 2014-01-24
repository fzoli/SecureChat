package org.dyndns.fzoli.ui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * A JRE6-ban jól működő sortörés JRE7-től nem működik jól.
 * Erre lett írva ez a javítás, ami az eredeti forrás alapján készült.
 * Az egész egyetlen utasítás miatt készült: if (axis == View.X_AXIS) return 0;
 * @author zoli
 */
public class FixedStyledEditorKit extends StyledEditorKit {
    
    private static final ViewFactory defaultFactory = new StyledViewFactory();

    protected static class WrapLabelView extends LabelView {

        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (axis == View.X_AXIS) return 0;
            return super.getMinimumSpan(axis);
        }
        
    }
    
    protected static class StyledViewFactory implements ViewFactory {

        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new javax.swing.text.ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new WrapLabelView(elem);
        }

    };

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

}
