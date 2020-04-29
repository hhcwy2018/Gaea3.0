#include "fileprocessor.h"
#include "QCoreApplication"
#include "exception"
#include "QTextCodec"
#include "QProcess"
#include "QStringList"

#define LOCAL(string) QTextCodec::codecForName("UTF8")->toUnicode((string))

QDir JavaFileProcessor::getCurrentDir()
{
    return QDir(QCoreApplication::applicationDirPath());
}

QString JavaFileProcessor::getJDKPath(QString *name)
{
    if (name == NULL)
        return getCurrentDir().absoluteFilePath(JDK_DIR_NAME);
    else
        return QDir(getCurrentDir().absoluteFilePath(JDK_DIR_NAME)).absoluteFilePath(*name);
}

QString JavaFileProcessor::getJAVAPath(QString name)
{
    return QDir(getCurrentDir().absoluteFilePath(JAVA_DIR_NAME)).absoluteFilePath(name);
}

JavaFileProcessor::JavaFileProcessor()
{

}

int JavaFileProcessor::setupJDK(QString name)
{
    QFile jdk(getJDKPath(&name));
    if (!jdk.exists())
        return JDK_NOT_EXISTS;

    QDir cur = QDir::current();
    QDir::setCurrent(getJDKPath(NULL));
    QString command = jdk.fileName()+ " INSTALL_SILENT=1";

    int ret = system(command.toUtf8());
//    int ret = QProcess::execute(command, QStringList() << "INSTALL_SILENT=1");
    if (ret != 0)
        return JDK_SETUP_FAILED;
    QDir::setCurrent(cur.absolutePath());

    return DO_SUCC;
}

int JavaFileProcessor::runJAVACommand(QString javaName, QString *params)
{
    QFile javaCommand(getJAVAPath(javaName));
    if (!javaCommand.exists())
        return JAVA_NOT_EXISTS;

    QString command = javaName;
    if (params != NULL && !(*params).isEmpty())
        command = command + " " + *params;

    int ret = system(command.toUtf8());

    if (ret != 0)
        return JDK_SETUP_FAILED;

    return DO_SUCC;
}
