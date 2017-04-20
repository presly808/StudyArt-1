package ua.artcode.utils.IO_utils;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.artcode.dao.StudyDB;
import ua.artcode.exceptions.CourseNotFoundException;
import ua.artcode.exceptions.DirectoryCreatingException;
import ua.artcode.exceptions.InvalidIDException;
import ua.artcode.exceptions.LessonNotFoundException;
import ua.artcode.model.Course;
import ua.artcode.model.Lesson;
import ua.artcode.utils.RunUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by v21k on 20.04.17.
 */
@Component
public class CourseIOUtils {

    @Value("${pathForGitProjects}")
    private String localPathForProjects;
    @Value("${pathForExternalCodeCompiling}")
    private String localPathForExternalCode;

    @Autowired
    private CommonIOUtils commonIOUtils;

    /**
     * Downloading project from Git and save it locally
     * <p>
     * 1.Create directory for project.
     * 2.Clone git project to dir.
     * 3.Return path.
     *
     * @return path where project has been saved
     */
    public String saveLocally(Course course) throws DirectoryCreatingException, GitAPIException {
        String projectPath = generatePath(course);
        File projectDirectory = new File(projectPath);
        try {
            if (projectDirectory.exists()) {
                FileUtils.cleanDirectory(new File(projectPath));
            }
            Files.createDirectories(Paths.get(projectPath));
            Git.cloneRepository()
                    .setURI(course.getUrl())
                    .setDirectory(projectDirectory)
                    .call();
        } catch (IOException e) {
            throw new DirectoryCreatingException("Unable to create a directory for course: " + course.getName());
        }
        return projectPath;
    }

    /**
     * Parse lessons from project folder and save them in corresponding course field (as List)
     * <p>
     * 1.Walk through project directory and look for folders contains "lesson"
     * 3.Generate lessons from results (parse name and all .java files)
     * 2.Save lessons as list
     *
     * @return List of lessons
     **/

    public List<Lesson> getLessons(Course course) throws IOException {
        String courseLocalPath = course.getLocalPath();

        return Files.walk(Paths.get(courseLocalPath))
                .map(Path::toString)
                .filter(path -> path.endsWith("lesson"))
                .map(path -> {
                    String lessonName = path.substring(path.lastIndexOf("/") + 1);
                    return new Lesson(lessonName, path);
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Save code locally
     * 1. Parse className from code (3rd word - public class NAME {...}
     * 2. Create directories or delete everything from them if there are already something
     * 3. Write code into .java file
     *
     * @return path for created .java file as String
     */
    public String saveExternalCodeLocally(String code) throws IOException {
        String className = code.split(" ")[2];
        Path classPathDirectory = Paths.get(localPathForExternalCode);

        Files.createDirectories(classPathDirectory);
        FileUtils.cleanDirectory(new File(localPathForExternalCode));

        String javaClassName = className + ".java";
        File sourceFile = new File(localPathForExternalCode, javaClassName);

        Files.write(sourceFile.toPath(), code.getBytes(), StandardOpenOption.CREATE);

        return localPathForExternalCode + "/" + javaClassName;
    }

    public String[] getLessonClassPaths(int courseId, int lessonNumber, StudyDB<Course> db) throws InvalidIDException,
            CourseNotFoundException, LessonNotFoundException, IOException {
        Course course = db.getByID(courseId);
        Lesson lesson = RunUtils.getLesson(lessonNumber, course);
        return commonIOUtils.parseFilePaths(lesson.getLocalPath(), ".java");
    }

    private String generatePath(Course course) {
        return localPathForProjects + "/" + course.getId() + course.getName() + "/";
    }

}