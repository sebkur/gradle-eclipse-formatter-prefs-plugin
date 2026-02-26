package de.topobyte.gradle.eclipseformatter;

import javax.inject.Inject;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public abstract class EclipseFormatterExtension
{

	private final RegularFileProperty formatterXml;
	private final Property<String> profileName;
	private final Property<Boolean> forceDefaultJavaFormatter;

	@Inject
	public EclipseFormatterExtension(ObjectFactory objects)
	{
		this.formatterXml = objects.fileProperty();
		this.profileName = objects.property(String.class);
		this.forceDefaultJavaFormatter = objects.property(Boolean.class);

		this.profileName.convention((String) null);
		this.forceDefaultJavaFormatter.convention(true);
	}

	public RegularFileProperty getFormatterXml()
	{
		return formatterXml;
	}

	public Property<String> getProfileName()
	{
		return profileName;
	}

	public Property<Boolean> getForceDefaultJavaFormatter()
	{
		return forceDefaultJavaFormatter;
	}

}
