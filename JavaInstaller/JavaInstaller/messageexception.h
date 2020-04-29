#ifndef MESSAGEEXCEPTION_H
#define MESSAGEEXCEPTION_H
#include "QString"
#include "QByteArray"

using namespace std;

class MessageException : public exception
{
private:
    QByteArray ba;
public:
    MessageException(QString msg);
    ~MessageException() throw();
    const char* what() const throw();
};


#endif // MESSAGEEXCEPTION_H
