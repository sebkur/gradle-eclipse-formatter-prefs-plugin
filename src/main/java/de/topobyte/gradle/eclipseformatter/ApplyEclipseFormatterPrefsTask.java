package de.topobyte.gradle.eclipseformatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ApplyEclipseFormatterPrefsTask extends DefaultTask
{

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getFormatterXml();

	@Input
	@Optional
	public abstract Property<String> getProfileName();

	@Input
	public abstract Property<Boolean> getForceDefaultJavaFormatter();

	@OutputFile
	public abstract RegularFileProperty getOutputPrefsFile();

	@TaskAction
	public void applyPrefs() throws Exception
	{
		File formatterXml = getFormatterXml().get().getAsFile();
		File prefsFile = getOutputPrefsFile().get().getAsFile();

		File parent = prefsFile.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}

		Map<String, String> prefs = readPrefsIfExists(prefsFile);

		Map<String, String> formatterPrefs = readFormatterPrefs(formatterXml,
				getProfileName().getOrNull());
		for (Map.Entry<String, String> e : formatterPrefs.entrySet()) {
			prefs.put(e.getKey(), escapePropsValue(e.getValue()));
		}

		if (Boolean.TRUE.equals(getForceDefaultJavaFormatter().get())) {
			prefs.put("org.eclipse.jdt.core.javaFormatter",
					"org.eclipse.jdt.core.defaultJavaFormatter");
		}

		writeSortedPrefs(prefsFile, prefs);

		getLogger().lifecycle("Wrote {}", prefsFile);
	}

	private static Map<String, String> readPrefsIfExists(File prefsFile)
			throws IOException
	{
		Map<String, String> prefs = new HashMap<>();
		if (!prefsFile.exists()) {
			return prefs;
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(prefsFile), StandardCharsets.UTF_8))) {

			String line;
			while ((line = reader.readLine()) != null) {
				String t = line.trim();
				if (t.isEmpty() || t.startsWith("#")) {
					continue;
				}
				int idx = t.indexOf('=');
				if (idx <= 0) {
					continue;
				}
				String key = t.substring(0, idx);
				String val = t.substring(idx + 1);
				prefs.put(key, val);
			}
		}

		return prefs;
	}

	private static Map<String, String> readFormatterPrefs(File formatterXml,
			String desiredProfileName) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setExpandEntityReferences(false);
		dbf.setNamespaceAware(false);

		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc;
		try (InputStream in = new FileInputStream(formatterXml)) {
			doc = db.parse(in);
		}

		Element profile = selectProfile(doc, desiredProfileName);
		if (profile == null) {
			throw new IllegalStateException(
					"No <profile> found in formatter.xml");
		}

		NodeList settings = profile.getElementsByTagName("setting");
		Map<String, String> prefs = new HashMap<>();
		for (int i = 0; i < settings.getLength(); i++) {
			Node node = settings.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element s = (Element) node;
			String id = s.getAttribute("id");
			if (id == null || id.isEmpty()) {
				continue;
			}
			String value = s.getAttribute("value");
			prefs.put(id, value == null ? "" : value);
		}
		return prefs;
	}

	private static Element selectProfile(Document doc,
			String desiredProfileName)
	{
		NodeList profiles = doc.getElementsByTagName("profile");
		if (profiles.getLength() == 0) {
			return null;
		}

		if (desiredProfileName != null
				&& !desiredProfileName.trim().isEmpty()) {
			for (int i = 0; i < profiles.getLength(); i++) {
				Node n = profiles.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				Element p = (Element) n;
				String name = p.getAttribute("name");
				if (desiredProfileName.equals(name)) {
					return p;
				}
			}
		}

		Node n = profiles.item(0);
		return n.getNodeType() == Node.ELEMENT_NODE ? (Element) n : null;
	}

	private static void writeSortedPrefs(File prefsFile,
			Map<String, String> prefs) throws IOException
	{
		List<String> keys = new ArrayList<>(prefs.keySet());
		keys.remove("eclipse.preferences.version");
		Collections.sort(keys);

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(prefsFile), StandardCharsets.UTF_8))) {

			writer.write("eclipse.preferences.version=1");
			writer.newLine();

			for (String k : keys) {
				writer.write(k);
				writer.write('=');
				writer.write(prefs.getOrDefault(k, ""));
				writer.newLine();
			}
		}
	}

	private static String escapePropsValue(String v)
	{
		if (v == null) {
			return "";
		}
		return v.replace("\\", "\\\\").replace(":", "\\:").replace("=", "\\=");
	}

}
