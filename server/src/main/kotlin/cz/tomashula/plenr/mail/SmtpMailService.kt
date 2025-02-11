package cz.tomashula.plenr.mail

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.mail2.jakarta.DefaultAuthenticator
import org.apache.commons.mail2.jakarta.SimpleEmail


class SmtpMailService(
    private val smtpHost: String,
    private val smtpPort: Int,
    private val smtpUsername: String,
    private val smtpPassword: String,
    val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MailService
{
    override suspend fun sendMail(recipient: String, subject: String, body: String)
    {
        sendMail(listOf(recipient), subject, body)
    }

    override suspend fun sendMail(recipients: Iterable<String>, subject: String, body: String)
    {
        withContext(coroutineDispatcher) {
            val email = SimpleEmail()
            email.hostName = smtpHost
            email.setSmtpPort(smtpPort)
            email.authenticator = DefaultAuthenticator(smtpUsername, smtpPassword)
            email.setSSLOnConnect(false)
            email.setFrom(smtpUsername)
            email.setSubject(subject)
            email.setMsg(body)
            recipients.forEach { email.addTo(it) }
            email.send()
        }
    }
}
