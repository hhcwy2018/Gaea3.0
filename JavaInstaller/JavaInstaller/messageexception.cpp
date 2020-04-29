#include "messageexception.h"

MessageException::MessageException(QString msg)
{
    ba = msg.toLocal8Bit();
}

MessageException::~MessageException() throw()
{
}

const char* MessageException::what() const throw()
{
    return ba.constData();
}
