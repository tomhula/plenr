package cz.tomashula.plenr.mail

interface MailService
{
    suspend fun sendMail(recipient: String, subject: String, body: String)

    suspend fun sendMail(recipients: Iterable<String>, subject: String, body: String)
}
