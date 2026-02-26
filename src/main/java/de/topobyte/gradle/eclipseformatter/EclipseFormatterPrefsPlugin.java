package de.topobyte.gradle.eclipseformatter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class EclipseFormatterPrefsPlugin implements Plugin<Project>
{

	@Override
	public void apply(Project project)
	{
		EclipseFormatterExtension ext = project.getExtensions().create(
				"eclipseFormatter", EclipseFormatterExtension.class,
				project.getObjects());

		TaskProvider<ApplyEclipseFormatterPrefsTask> task = project.getTasks()
				.register("applyEclipseFormatterPrefs",
						ApplyEclipseFormatterPrefsTask.class, t -> {
							t.getFormatterXml().set(ext.getFormatterXml());
							t.getProfileName().set(ext.getProfileName());
							t.getForceDefaultJavaFormatter()
									.set(ext.getForceDefaultJavaFormatter());

							t.getOutputPrefsFile().set(project.getLayout()
									.getProjectDirectory()
									.file(".settings/org.eclipse.jdt.core.prefs"));
						});

		project.getPlugins().withId("eclipse", ignored -> project.getTasks()
				.named("eclipse").configure(e -> e.finalizedBy(task)));
	}

}
