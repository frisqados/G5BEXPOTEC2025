package util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class LimitDocumentFilter extends DocumentFilter {
    private int limit;

    public LimitDocumentFilter(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("El límite debe ser un número positivo.");
        }
        this.limit = limit;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        }
        if ((fb.getDocument().getLength() + string.length()) <= limit) {
            super.insertString(fb, offset, string, attr);
        } else {
            // Si el texto a insertar excede el límite, inserta solo la parte que quepa
            if (fb.getDocument().getLength() < limit) {
                string = string.substring(0, limit - fb.getDocument().getLength());
                super.insertString(fb, offset, string, attr);
            }
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) {
            return;
        }
        // Calcula la longitud resultante después de la operación de reemplazo
        int currentLength = fb.getDocument().getLength();
        int newLength = currentLength - length + text.length();

        if (newLength <= limit) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            // Si el texto de reemplazo excede el límite, reemplaza solo con la parte que quepa
            if (currentLength - length < limit) {
                text = text.substring(0, limit - (currentLength - length));
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
