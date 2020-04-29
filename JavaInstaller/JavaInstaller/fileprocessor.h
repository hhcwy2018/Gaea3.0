#ifndef FILEPROCESSOR_H
#define FILEPROCESSOR_H

#define JAVA_EXECUTE_FAILED -4
#define JAVA_NOT_EXISTS -3
#define JDK_NOT_EXISTS -2
#define JDK_SETUP_FAILED -1
#define DO_SUCC 0

#include "QProcess"
#include "QTextStream"
#include "QFile"
#include "QDir"


class JavaFileProcessor
{
private:
    const QString JDK_DIR_NAME = "jdk";
    const QString JAVA_DIR_NAME = "java";

    QDir currentDir;
    QDir getCurrentDir();
    QString getJDKPath(QString *name);
    QString getJAVAPath(QString name);
public:
    JavaFileProcessor();

    int setupJDK(QString name);

    int runJAVACommand(QString javaName, QString *params);
};

#endif // FILEPROCESSOR_H
