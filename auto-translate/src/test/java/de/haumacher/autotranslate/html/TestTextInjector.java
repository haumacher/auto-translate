package de.haumacher.autotranslate.html;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.haumacher.autotranslate.html.extract.TextInjector;

/**
 * Test case for {@link TextInjector}.
 */
public class TestTextInjector {

	/**
	 * A translated text may reference more {@code <xN>} markers than the current
	 * source element has children — e.g. a stale cached translation produced for
	 * a source structure that has since lost placeholders. The injector must not
	 * crash on the surplus marker (it used to throw
	 * {@code IndexOutOfBoundsException}, aborting the whole translation run);
	 * instead it drops the orphan marker and keeps the surrounding text.
	 */
	@Test
	public void testSurplusMarkerDoesNotCrash() throws SAXException, IOException, ParserConfigurationException {
		Document doc = parse("<p>Hello <b>world</b></p>");
		Element p = doc.getDocumentElement();

		// <x1> maps to the single child <b>; <x2> has no counterpart.
		new TextInjector(p).inject("Hallo <x1>Welt</x1> und <x2>mehr</x2> Text");

		String text = p.getTextContent();
		Assertions.assertTrue(text.contains("Hallo"), text);
		Assertions.assertTrue(text.contains("Welt"), text);
		Assertions.assertTrue(text.contains("und"), text);
		// The orphan marker's content is preserved as plain text, not lost.
		Assertions.assertTrue(text.contains("mehr"), text);
		Assertions.assertTrue(text.contains("Text"), text);
	}

	/**
	 * DeepL may return a marker capitalized ({@code <X1>}); it must still be
	 * restored to the source element instead of ending up as literal text.
	 */
	@Test
	public void testCapitalizedMarker() throws SAXException, IOException, ParserConfigurationException {
		Document doc = parse("<p>Du kannst es <a href=\"#x\">selbst hosten</a>.</p>");
		Element p = doc.getDocumentElement();

		new TextInjector(p).inject("Tu peux <X1>l'héberger toi-même</X1>.");

		Assertions.assertFalse(p.getTextContent().contains("X1"), p.getTextContent());
		Element a = (Element) p.getElementsByTagName("a").item(0);
		Assertions.assertEquals("l'héberger toi-même", a.getTextContent());
		Assertions.assertEquals("#x", a.getAttribute("href"));
	}

	private static Document parse(String html) throws SAXException, IOException, ParserConfigurationException {
		return TestHtmlAnalyzer.parse(html);
	}
}
