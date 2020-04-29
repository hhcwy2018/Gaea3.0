#include <QCoreApplication>
#include "qtextstream.h"
#include "qdir.h"
#include "fileprocessor.h"

int main(int argc, char *argv[])
{
    QCoreApplication a(argc, argv);

    QTextStream output(stdout);

    output << QDir::current().absolutePath() << endl;

    QString jdk;
    if (argc > 1){
        jdk = argv[1];
    }

    QString javaCommand;
    if (argc > 2){
        javaCommand = argv[2];
    }

    QString params;
    if (argc > 3){
        params = argv[3];
    }

    QString workPath = QCoreApplication::applicationDirPath();
    if (argc > 4){
        workPath = argv[4];
    }

    bool setupJDK = true;
    if (argc > 5){
        QString tmp = argv[5];
        setupJDK = tmp.compare("true", Qt::CaseInsensitive) == 0;
    }
//    output << "JDK => " << jdk << endl << "JAVA => " << javaCommand << endl << "CUR_DIR => " << workPath << endl;


    output << "setup jdk ..." << endl;

    JavaFileProcessor javaFileProcessor;
    int ret = DO_SUCC;
    if (setupJDK){
        ret = javaFileProcessor.setupJDK(jdk);
    }else{
        output << "setup jdk is cancel!";
    }

    switch (ret) {
    case DO_SUCC:{
        QDir dir(workPath);
        if (!dir.exists()){
            output << "workdir is not exist!" << endl;
            return 0;
        }

        if (!QDir::setCurrent(dir.absolutePath())){
            output << "set current dir is failed!" << endl;
            return 0;
        }

        output << QDir::current().absolutePath() << endl;

        switch(javaFileProcessor.runJAVACommand(javaCommand, &params)){
        case JAVA_NOT_EXISTS:
            output << "run command failed: java file not found!" << endl;
            break;
        case JAVA_EXECUTE_FAILED:
            output << "run command failed: execute java failed!" << endl;
            break;
        case DO_SUCC:
            break;
        }
        break;
    }
    case JDK_NOT_EXISTS:
        output << "setup jdk failed : file not found!" << endl;
        break;
    case JDK_SETUP_FAILED:
        output << "setup jdk failed: execute jdk failed!" << endl;
        break;
    }

    return 0;
}
