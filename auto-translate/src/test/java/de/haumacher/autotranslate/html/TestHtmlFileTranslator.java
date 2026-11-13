package de.haumacher.autotranslate.html;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HtmlFileTranslator}.
 */
public class TestHtmlFileTranslator {

	@Test
	public void testEnsureAllTagsWithMissingNestedTags() {
		String original = "foo <x1><x2></x2></x1> bar";
		String translated = "foo bar";

		String result = HtmlFileTranslator.ensureAllTags(original, translated);

		assertEquals("foo bar<x1><x2></x2></x1>", result);
	}

	@Test
	public void testEnsureAllTagsWithNoMissingTags() {
		String original = "foo <x1>bar</x1> baz";
		String translated = "foo <x1>bar</x1> baz";

		String result = HtmlFileTranslator.ensureAllTags(original, translated);

		assertEquals("foo <x1>bar</x1> baz", result);
	}

	@Test
	public void testEnsureAllTagsWithPartiallyMissingTags() {
		String original = "text <x1>with <x2>nested</x2> tags</x1> and <x3>more</x3>";
		String translated = "text <x1>avec tags</x1> et";

		String result = HtmlFileTranslator.ensureAllTags(original, translated);

		// Tags x2 and x3 are missing, should be appended
		assertEquals("text <x1>avec tags</x1> et<x2></x2><x3></x3>", result);
	}

	@Test
	public void testEnsureAllTagsWithCompletelyMissingTags() {
		String original = "<x1>hello</x1> <x2>world</x2>";
		String translated = "bonjour monde";

		String result = HtmlFileTranslator.ensureAllTags(original, translated);

		assertEquals("bonjour monde<x1></x1><x2></x2>", result);
	}

	@Test
	public void testEnsureAllTagsWithEmptyOriginalTags() {
		String original = "foo <x1></x1> bar";
		String translated = "foo bar";

		String result = HtmlFileTranslator.ensureAllTags(original, translated);

		assertEquals("foo bar<x1></x1>", result);
	}
	@Test
	public void testToDeepLXmlEscapesTextAndNumbersMarkersByAttribute() {
		assertEquals("A &amp; B &lt; C &gt; D <x i=\"1\">Link</x> <x i=\"2\">E</x>",
			HtmlFileTranslator.toDeepLXml("A & B < C > D <x1>Link</x1> <x2>E</x2>"));
	}

	@Test
	public void testFromDeepLXmlResolvesEntitiesAndMarkers() {
		assertEquals("A & B < C > \"D\" 'E' \u00e9 \u00e9 <x1>Link</x1>",
			HtmlFileTranslator.fromDeepLXml("A &amp; B &lt; C &gt; &quot;D&quot; &apos;E&apos; &#233; &#xe9; <x i=\"1\">Link</x>"));
	}

	@Test
	public void testFromDeepLXmlTakesNumberFromAttribute() {
		// DeepL reorders and capitalizes, the attribute keeps the number.
		assertEquals("<x2>b <x3>c</x3></x2> <x1>a</x1><x4></x4>",
			HtmlFileTranslator.fromDeepLXml("<X i=\"2\">b <x i='3'>c</x></X> <x i=\"1\">a</x><x i=\"4\"/>"));
	}

	@Test
	public void testDeepLXmlRoundTrip() {
		String text = "Fritz!Box &amp; Co. <x1>a < b</x1> & <x2>c</x2>";
		assertEquals(text, HtmlFileTranslator.fromDeepLXml(HtmlFileTranslator.toDeepLXml(text)));
	}

}
